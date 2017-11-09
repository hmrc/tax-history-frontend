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
import connectors.CitizenDetailsConnector
import controllers.auth.AgentAuth
import models.taxhistory.Person
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, Request, Result}
import uk.gov.hmrc.auth.core.MissingBearerToken
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier}
import uk.gov.hmrc.urls.Link

import scala.concurrent.Future

trait BaseController extends I18nSupport with AgentAuth {
  lazy val ggSignInRedirect: Result = toGGLogin(s"${FrontendAppConfig.loginContinue}")

  lazy val logoutLink = Link.toInternalPage(url = controllers.routes.EmploymentSummaryController.logout().url,
    value = Some("Sign out")).toHtml

  def logout() = Action.async {
    implicit request => {
      Future.successful(ggSignInRedirect.withNewSession)
    }
  }

  protected[controllers] def authorisedForAgent(eventualResult: Future[Result])
                                               (implicit hc:HeaderCarrier, request:Request[_]) =  {
    authorised(AuthProviderAgents).retrieve(affinityGroupAllEnrolls) {
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

  def retrieveCitizenDetails(ninoField: Nino, citizenDetailsConnector:CitizenDetailsConnector)
                                    (implicit hc: HeaderCarrier, request: Request[_]): Future[Either[Int, Person]] = {
    val details = {
      citizenDetailsConnector.getPersonDetails(ninoField) map {
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
    details
  }

  def redirectToClientErrorPage(status: Int) = {
    status match {
      case LOCKED => Future.successful(Redirect(controllers.routes.ClientErrorController.getMciRestricted()))
      case GONE => Future.successful(Redirect(controllers.routes.ClientErrorController.getDeceased()))
      case _ => Future.successful(Redirect(controllers.routes.ClientErrorController.getTechnicalError()))
    }
  }
}
