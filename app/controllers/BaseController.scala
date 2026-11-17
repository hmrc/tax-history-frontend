/*
 * Copyright 2026 HM Revenue & Customs
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

import config.AppConfig
import controllers.auth.AgentAuth
import models.taxhistory.{Person, SelectTaxYear}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthorisationException, InsufficientEnrolments, MissingBearerToken}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}
import utils.TaxHistoryLogger

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseController @Inject() (cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
    extends AgentAuth(cc)
    with I18nSupport
    with TaxHistoryLogger
    with TaxHistorySessionKeys {

  val loginContinue: String
  val appConfig: AppConfig

  private lazy val ggSignInRedirect: Result = toGGLogin(loginContinue)

  protected def authorisedAgent(
    predicate: Predicate
  )(eventualResult: Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    logger.info("Start authorisation check")
    authorised(predicate)
      .retrieve(affinityGroupAllEnrolls) {
        case Some(affinityG) ~ allEnrols =>
          (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
            case (`isAnAgent`, Some(_)) =>
              logger.info("Agent is authorised")
              eventualResult
            case (`isAnAgent`, None)    =>
              logger.info("No enrolments available for the agent")
              redirectToSubPage
            case _                      =>
              logger.info("Affinity group is not agent")
              redirectToExitPage
          }
        case _                           =>
          logger.info("No affinity group provided")
          redirectToExitPage
      }
      .recoverWith {
        case i: InsufficientEnrolments =>
          logger.warn(s"Agent authorisation failed with InsufficientEnrolments: ${i.getMessage}")
          Future.successful(Redirect(controllers.routes.ClientErrorController.getNotAuthorised()))
        case b: BadGatewayException    =>
          logger.error(s"Agent authorisation failed with BadGatewayException: ${b.getMessage}")
          Future.successful(Redirect(controllers.routes.ClientErrorController.getTechnicalError()))
        case m: MissingBearerToken     =>
          logger.warn(s"Agent authorisation failed with MissingBearerToken: ${m.getMessage}")
          Future.successful(ggSignInRedirect)
        case a: AuthorisationException =>
          logger.warn(s"Agent authorisation failed with AuthorisationException: ${a.getMessage}")
          Future.successful(ggSignInRedirect)
        case e                         =>
          logger.error(s"Agent authorisation failed with unexpected exception: ${e.getMessage}")
          Future.successful(ggSignInRedirect)
      }
  }

  protected[controllers] def authorisedForAgent(
    eventualResult: Nino => Future[Result]
  )(implicit hc: HeaderCarrier, request: Request[?]): Future[Result] =
    getNinoFromSession(request) match {
      case Some(nino) =>
        authorisedAgent(AgentEnrolmentForPAYE.withIdentifier("NI", nino.toString) and AuthProviderAgents)(
          eventualResult(nino)
        )
      case None       =>
        logger.info("No nino supplied in session for agent authorisation")
        Future.successful(Redirect(routes.SelectClientController.getSelectClientPage()))
    }

  protected[controllers] def handleHttpFailureResponse(status: Int, taxYears: Option[Int] = None): Result =
    status match {
      case NOT_FOUND    =>
        taxYears match {
          case Some(taxYears) => Redirect(controllers.routes.ClientErrorController.getNoData(taxYears))
          case None           => Redirect(controllers.routes.ClientErrorController.getTechnicalError())
        }
      case UNAUTHORIZED =>
        Redirect(controllers.routes.ClientErrorController.getNotAuthorised())
      case _            =>
        Redirect(controllers.routes.ClientErrorController.getTechnicalError())
    }

  def retrieveCitizenDetails(citizenDetailsResponse: Future[HttpResponse]): Future[Either[Int, Person]] = {
    citizenDetailsResponse map { personResponse =>
      personResponse.status match {
        case OK     =>
          val person = personResponse.json.as[Person]
          person.deceased match {
            case Some(true) => Left(GONE)
            case _          => Right(person)
          }
        case status =>
          logger.warn(s"CitizenDetails lookup returned with status $status")
          Left(status)
      }
    }
  }.recoverWith { case e =>
    logger.error(s"CitizenDetails lookup failed with ${e.getMessage}")
    Future.successful(Left(BAD_REQUEST))
  }

  def getNinoFromSession(request: Request[?]): Option[Nino] =
    request.session.get(ninoSessionKey).map(Nino(_))

  def getTaxYearFromSession(request: Request[?]): SelectTaxYear =
    SelectTaxYear(request.session.get(taxYearSessionKey))

  def redirectToSelectClientPage: Future[Result] =
    Future.successful(Redirect(controllers.routes.SelectClientController.getSelectClientPage()))

  def redirectToClientErrorPage(status: Int): Future[Result] = {
    logger.info(s"Redirect to Client Error Page status is $status")
    status match {
      case LOCKED => Future.successful(Redirect(controllers.routes.ClientErrorController.getMciRestricted()))
      case GONE   => Future.successful(Redirect(controllers.routes.ClientErrorController.getDeceased()))
      case _      => Future.successful(Redirect(controllers.routes.ClientErrorController.getTechnicalError()))
    }
  }
}
