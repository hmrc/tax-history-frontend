/*
 * Copyright 2017 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.auth.AgentAuth
import models.taxhistory.Person
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, Request, Result}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{InsufficientEnrolments, MissingBearerToken}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.urls.Link

import scala.concurrent.Future

trait BaseController extends I18nSupport with AgentAuth {
  lazy val ggSignInRedirect: Result = toGGLogin(s"${FrontendAppConfig.loginContinue}")

  lazy val logoutLink = Link.toInternalPage(url = controllers.routes.EmploymentSummaryController.logout().url,
    value = Some("Sign out")).toHtml

  def logout() = Action.async {
    implicit request => {
      Future.successful(Redirect(FrontendAppConfig.serviceSignOut).withNewSession)
    }
  }

  protected def authorisedAgent(predicate: uk.gov.hmrc.auth.core.authorise.Predicate)(
                              eventualResult :Future[Result])
                     (implicit hc:HeaderCarrier, request:Request[_]) = {
    authorised(predicate)
      .retrieve(affinityGroupAllEnrolls) {
      case Some(affinityG) ~ allEnrols =>
        (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
          case (`isAnAgent`, Some(_)) => {
            eventualResult
          }
          case (`isAnAgent`, None) => redirectToSubPage
          case _ => redirectToExitPage
        }
      case _ =>
        redirectToExitPage
    }.recoverWith {
      case i: InsufficientEnrolments =>
        Logger.error("Error thrown :" + i.getMessage)
        Future.successful(Redirect(controllers.routes.ClientErrorController.getNotAuthorised()))
      case b: BadGatewayException => {
        Logger.warn(s"BadGatewayException:${b.getMessage}")
        Future.successful(Redirect(controllers.routes.ClientErrorController.getTechnicalError()))
      }
      case m: MissingBearerToken => {
        Logger.warn(s"MissingBearerToken:${m.getMessage}")
        Future.successful(ggSignInRedirect)
      }
      case e =>
        Logger.error("Exception thrown :" + e.getMessage)
        Future.successful(ggSignInRedirect)
    }
  }

  protected[controllers] def authorisedForAgent(eventualResult:(Nino) =>Future[Result])
                                               (implicit hc:HeaderCarrier, request:Request[_]) =  {
    val maybeNino = getNinoFromSession(request)
    maybeNino match {
      case Some(nino) => authorisedAgent(AgentEnrolmentForPAYE.withIdentifier("MTDITID",
        nino.toString) and AuthProviderAgents)(eventualResult(nino))
      case None => {
        Logger.warn("No nino supplied.")
        Future.successful(Redirect(routes.SelectClientController.getSelectClientPage()))
      }
    }
  }

  protected[controllers] def handleHttpFailureResponse(status:Int, nino: Nino)
                                       (implicit request: Request[_]) = {
    status match {
      case NOT_FOUND =>
        Redirect(controllers.routes.ClientErrorController.getNoData())
      case UNAUTHORIZED =>
        Redirect(controllers.routes.ClientErrorController.getNotAuthorised())
      case s => {
        Logger.error("Error response returned with status:" + s)
        Redirect(controllers.routes.ClientErrorController.getTechnicalError())
      }
    }
  }

  def retrieveCitizenDetails(ninoField: Nino, citizenDetailsResponse:Future[HttpResponse])
                                    (implicit hc: HeaderCarrier, request: Request[_]): Future[Either[Int, Person]] = {
    {
      citizenDetailsResponse map {
        personResponse =>
          personResponse.status match {
            case OK => {
              val person = personResponse.json.as[Person]
              if (person.deceased) Left(GONE) else Right(person)
            }
            case status => Left(status)
          }
      }
    }.recoverWith {
      case _ => Future.successful(Left(BAD_REQUEST))
    }
  }

  def getNinoFromSession(request:Request[_]):Option[Nino] = {
    request.session.get("USER_NINO").map(Nino(_))
  }

  def redirectToSelectClientPage:Future[Result] = Future.successful(Redirect(controllers.routes.SelectClientController.getSelectClientPage()))

  def redirectToClientErrorPage(status: Int):Future[Result] = {
    status match {
      case LOCKED => Future.successful(Redirect(controllers.routes.ClientErrorController.getMciRestricted()))
      case GONE => Future.successful(Redirect(controllers.routes.ClientErrorController.getDeceased()))
      case _ => Future.successful(Redirect(controllers.routes.ClientErrorController.getTechnicalError()))
    }
  }
}
