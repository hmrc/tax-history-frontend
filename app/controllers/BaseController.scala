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
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, Request, Result}
import uk.gov.hmrc.auth.core.MissingBearerToken
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier}
import uk.gov.hmrc.urls.Link

import scala.concurrent.Future

trait BaseController extends I18nSupport with AgentAuth {
  lazy val ggSignInRedirect: Result = toGGLogin(s"${FrontendAppConfig.loginContinue}")

  lazy val logoutLink = Link.toInternalPage(url = controllers.routes.EmploymentSummaryController.logout.url, value = Some("Sign out")).toHtml

  def logout() = Action.async {
    implicit request => {
      Future.successful(ggSignInRedirect.withNewSession)
    }
  }

  protected[controllers] def authorisedForAgent(eventualResult: Future[Result])(implicit hc:HeaderCarrier, request:Request[_]) =  {
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
        Logger.warn(messagesApi("employmenthistory.technicalerror.message") + s" : Due to BadGatewayException:${b.getMessage}")
        Future.successful(handleHttpResponse("technicalerror", FrontendAppConfig.AfiHomePage, None))
      }
      case m: MissingBearerToken => {
        Logger.warn(messagesApi("employmenthistory.technicalerror.message") + s" : Due to MissingBearerToken:${m.getMessage}")
        Future.successful(ggSignInRedirect)
      }
      case e =>
        Logger.warn("Exception thrown :" + e.getMessage)
        Future.successful(ggSignInRedirect)
    }
  }


  protected[controllers] def handleHttpResponse(message: String, sideBarUrl: String, nino: Option[String])(implicit request: Request[_]) = {
    Logger.warn(messagesApi(s"employmenthistory.$message.message"))
    val sidebarLink = Link.toInternalPage(
      url = sideBarUrl,
      value = Some(messagesApi(s"employmenthistory.$message.linktext"))).toHtml

    Ok(views.html.error_template(
      messagesApi(s"employmenthistory.$message.title"),
      messagesApi(s"employmenthistory.$message.header", nino.getOrElse("")),
      "",
      Some(sidebarLink),
      gaEventId = Some(message))).removingFromSession("USER_NINO")
  }
}
