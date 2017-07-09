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

import javax.inject.Inject

import config.{ConfigDecorator, FrontendAppConfig, FrontendAuthConnector}
import connectors.TaxHistoryConnector
import controllers.auth.AgentAuth
import form.SelectClientForm.selectClientForm
import models.taxhistory.Employment
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Result}
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.BadGatewayException
import uk.gov.hmrc.time.TaxYearResolver
import uk.gov.hmrc.urls.Link
import views.html.select_client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait BaseController extends FrontendController with Actions with Redirects with I18nSupport

class MainController @Inject()(
                                val configDecorator: ConfigDecorator,
                                val taxHistoryConnector: TaxHistoryConnector,
                                override val authConnector: FrontendAuthConnector,
                                override val config: Configuration,
                                override val env: Environment,
                                implicit val messagesApi: MessagesApi
                              ) extends BaseController with AgentAuth {

  lazy val ggSignInRedirect: Result = toGGLogin(s"${configDecorator.loginContinue}")

  lazy val logoutLink = Link.toInternalPage(url=controllers.routes.MainController.logout.url,value=Some("Sign out")).toHtml

  def logout() = Action.async {
    implicit request => {
       Future.successful(ggSignInRedirect.withNewSession)
    }
  }

  def get() = Action.async {
    implicit request => {
      val nino = request.session.get("USER_NINO").map(Nino(_))
      authorised(Enrolment("HMRC-AS-AGENT") and AuthProviders(GovernmentGateway)) {
        val cy1 = TaxYearResolver.currentTaxYear - 1
        nino match {
          case Some(nino) =>
            taxHistoryConnector.getTaxHistory(nino, cy1) map {
              historyResponse => historyResponse.status match {
                case OK => {
                  val taxHistory = historyResponse.json.as[Seq[Employment]]
                  val sidebarLink = Link.toExternalPage(
                    url=FrontendAppConfig.AfiHomePage,
                    value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
                  Ok(views.html.taxhistory.employments_main(nino.nino, cy1, taxHistory, Some(sidebarLink),headerNavLink=Some(logoutLink))).removingFromSession("USER_NINO")
                }
                case NOT_FOUND => {
                  Logger.warn(messagesApi("employmenthistory.notfound.message"))
                  val sidebarLink = Link.toInternalPage(
                    url=controllers.routes.MainController.getSelectClientPage().url,
                    value = Some(messagesApi("employmenthistory.notfound.linktext"))).toHtml
                  Ok(views.html.error_template(
                    messagesApi("employmenthistory.notfound.title"),
                    messagesApi("employmenthistory.notfound.title"),
                    messagesApi("employmenthistory.notfound.message"),
                    Some(sidebarLink),
                    headerNavLink=Some(logoutLink))).removingFromSession("USER_NINO")
                }
                case UNAUTHORIZED => {
                  Logger.warn(messagesApi("employmenthistory.unauthorised.message"))
                  val sidebarLink = Link.toExternalPage(
                    url=FrontendAppConfig.AfiHomePage,
                    value = Some(messagesApi("employmenthistory.unauthorised.linktext"))).toHtml
                  Ok(views.html.error_template(
                    messagesApi("employmenthistory.unauthorised.title"),
                    messagesApi("employmenthistory.unauthorised.title"),
                    messagesApi("employmenthistory.unauthorised.message"),
                    Some(sidebarLink),
                    headerNavLink=Some(logoutLink))).removingFromSession("USER_NINO")
                }
                case s => {
                  Logger.warn(messagesApi("employmenthistory.technicalerror.message")+": With status:"+s)
                  val sidebarLink = Link.toExternalPage(
                    url=FrontendAppConfig.AfiHomePage,
                    value = Some(messagesApi("employmenthistory.technicalerror.linktext"))).toHtml
                  Ok(views.html.error_template(
                    messagesApi("employmenthistory.technicalerror.title"),
                    messagesApi("employmenthistory.technicalerror.title"),
                    messagesApi("employmenthistory.technicalerror.message"),
                    Some(sidebarLink),
                    headerNavLink=Some(logoutLink))).removingFromSession("USER_NINO")
                }
              }
            }
          case _ =>
            Logger.warn("No nino supplied.")
            val sidebarLink = Link.toExternalPage(
              url=FrontendAppConfig.AfiHomePage,
              value = Some(messagesApi("employmenthistory.technicalerror.linktext"))).toHtml
            Future.successful(Ok(views.html.select_client(selectClientForm, Some(sidebarLink),
              headerNavLink=Some(logoutLink))))
        }
      }.recoverWith {
        case b: BadGatewayException => {
          Logger.warn(messagesApi("employmenthistory.technicalerror.message")+" : Due to BadGatewayException:"+b.getMessage)
          val sidebarLink = Link.toExternalPage(
            url=FrontendAppConfig.AfiHomePage,
            value = Some(messagesApi("employmenthistory.technicalerror.linktext"))).toHtml
          Future.successful(Ok(views.html.error_template(
            messagesApi("employmenthistory.technicalerror.title"),
            messagesApi("employmenthistory.technicalerror.title"),
            messagesApi("employmenthistory.technicalerror.message"),
            Some(sidebarLink),
            headerNavLink=Some(logoutLink))).removingFromSession("USER_NINO"))
        }
        case m: MissingBearerToken => {
          Logger.warn(messagesApi("employmenthistory.technicalerror.message")+" : Due to MissingBearerToken:"+m.getMessage)
          Future.successful(ggSignInRedirect)
        }
        case e =>
          Logger.warn("Exception thrown :"+e.getMessage)
          Future.successful(ggSignInRedirect)
      }
    }
  }

  def getSelectClientPage: Action[AnyContent] = Action.async { implicit request =>
    authorised(AuthProviderAgents).retrieve(affinityGroupAllEnrolls) {
      case Some(affinityG) ~ allEnrols ⇒
        (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
          case (`isAnAgent`, Some(_)) => {
            val sidebarLink = Link.toExternalPage(
              url=FrontendAppConfig.AfiHomePage,
              value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
            Future.successful(Ok(select_client(selectClientForm,
                                              Some(sidebarLink),
                                              headerNavLink=Some(logoutLink))))
          }
          case (`isAnAgent`, None) => redirectToSubPage
          case _ => redirectToExitPage
        }
      case _ =>
        redirectToExitPage
    } recover {
      case e ⇒
        handleFailure(e)
    }
  }

  def submitSelectClientPage(): Action[AnyContent] = Action.async { implicit request =>
    selectClientForm.bindFromRequest().fold(
      formWithErrors ⇒ {
        val sidebarLink = Link.toExternalPage(
          url=FrontendAppConfig.AfiHomePage,
          value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
        Future.successful(BadRequest(select_client(formWithErrors,
                                                   Some(sidebarLink),
                                                   headerNavLink=Some(logoutLink))))
      },
      validFormData => {
        authorised(AuthProviderAgents).retrieve(affinityGroupAllEnrolls) {
          case Some(affinityG) ~ allEnrols ⇒
            (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
              case (`isAnAgent`, Some(_)) => Future successful Redirect(routes.MainController.get())
                .addingToSession("USER_NINO" -> s"${validFormData.clientId}")
              case (`isAnAgent`, None) => redirectToSubPage
              case _ => redirectToExitPage
            }
          case _ =>
            redirectToExitPage
        } recover {
          case e ⇒
            handleFailure(e)
        }
      }
    )
  }

}
