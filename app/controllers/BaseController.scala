/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import controllers.auth.AgentAuth
import models.taxhistory.Person
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, MissingBearerToken}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.urls.Link
import utils.TaxHistoryLogger

import scala.concurrent.Future

trait BaseController extends I18nSupport with AgentAuth with TaxHistoryLogger {

  /**
    * The URI to direct to for login.
    */
  val loginContinue: String

  /**
    * The URI to direct to for signout.
    */
  val serviceSignout: String

  lazy val ggSignInRedirect: Result = toGGLogin(loginContinue)

  lazy val logoutLink: Html =
    Link.toInternalPage(controllers.routes.EmploymentSummaryController.logout().url, Some("Sign out")).toHtml

  def logout(): Action[AnyContent] = Action.async {
    implicit request => {
      logger.info("Sign out of the service")
      Future.successful(Redirect(serviceSignout).withNewSession)
    }
  }


  /**
    * This function is designed to be wrapped around a block of code (`result`).
    * Checks the authorisation condition (`predicate`) and return `result` if authorised.
    * Otherwise, handles the authorisation failure.
    */
  protected def authorisedAgent(predicate: Predicate)
                               (result: => Future[Result])
                               (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    logger.info("Start authorisation check")
    authorised(predicate)
      .retrieve(affinityGroupAllEnrolls) {
        case Some(affinityG) ~ allEnrols =>
          (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
            case (`isAnAgent`, Some(_)) =>
              logger.info("Agent is authorised")
              result
            case (`isAnAgent`, None) =>
              logger.info("No enrolments available for the agent")
              redirectToSubPage
            case _ =>
              logger.info("No affinity group is not agent")
              redirectToExitPage
          }
        case _ =>
          logger.info("No affinity group provided")
          redirectToExitPage
      }.recoverWith {
      case i: InsufficientEnrolments =>
        logger.info(s"InsufficientEnrolments:${i.getMessage}")
        Future.successful(Redirect(controllers.routes.ClientErrorController.getNotAuthorised()))
      case b: BadGatewayException =>
        logger.warn(s"BadGatewayException:${b.getMessage}")
        Future.successful(Redirect(controllers.routes.ClientErrorController.getTechnicalError()))
      case m: MissingBearerToken =>
        logger.warn(s"MissingBearerToken:${m.getMessage}")
        Future.successful(ggSignInRedirect)
      case e =>
        logger.error(s"Exception thrown:${e.getMessage}")
        Future.successful(ggSignInRedirect)
    }
  }

  /**
    * This function is designed to be wrapped around a block of code (`result`).
    * Checks the authorisation of the NINO in the session and return the output of `result` if authorised.
    * Otherwise, handles the authorisation failure.
    */
  protected[controllers] def authorisedForAgent(result: (Nino) => Future[Result])
                                               (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    getNinoFromSession(request) match {
      case Some(nino) =>
        authorisedAgent(AgentEnrolmentForPAYE.withIdentifier("MTDITID", nino.toString) and AuthProviderAgents)(result(nino))
      case None =>
        logger.info("No nino supplied")
        Future.successful(Redirect(routes.SelectClientController.getSelectClientPage()))
    }
  }

  protected[controllers] def handleHttpFailureResponse(status: Int, nino: Nino)
                                                      (implicit request: Request[_]): Result = {
    logger.info(s"HttpFailure status is $status")
    status match {
      case NOT_FOUND =>
        Redirect(controllers.routes.ClientErrorController.getNoData())
      case UNAUTHORIZED =>
        Redirect(controllers.routes.ClientErrorController.getNotAuthorised())
      case _ =>
        Redirect(controllers.routes.ClientErrorController.getTechnicalError())
    }
  }

  def retrieveCitizenDetails(ninoField: Nino, citizenDetailsResponse: Future[HttpResponse])
                            (implicit hc: HeaderCarrier, request: Request[_]): Future[Either[Int, Person]] = {
    {
      citizenDetailsResponse map {
        personResponse =>
          personResponse.status match {
            case OK =>
              val person = personResponse.json.as[Person]
              person.deceased match {
                case Some(true) => Left(GONE)
                case _ => Right(person)
              }
            case status =>
              logger.warn(s"citizenDetails lookup returned with status $status")
              Left(status)
          }
      }
    }.recoverWith {
      case e =>
        logger.warn(s"citizenDetails lookup failed with ${e.getMessage}")
        Future.successful(Left(BAD_REQUEST))
    }
  }

  def getNinoFromSession(request: Request[_]): Option[Nino] =
    request.session.get(CustomSessionKeys.Nino).map(Nino(_))

  def redirectToSelectClientPage: Future[Result] = Future.successful(Redirect(controllers.routes.SelectClientController.getSelectClientPage()))

  def redirectToClientErrorPage(status: Int): Future[Result] = {
    logger.info(s"redirectToClientErrorPage status is $status")
    status match {
      case LOCKED => Future.successful(Redirect(controllers.routes.ClientErrorController.getMciRestricted()))
      case GONE => Future.successful(Redirect(controllers.routes.ClientErrorController.getDeceased()))
      case _ => Future.successful(Redirect(controllers.routes.ClientErrorController.getTechnicalError()))
    }
  }
}
