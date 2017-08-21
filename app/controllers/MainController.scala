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
import models.taxhistory.{Allowance, CompanyBenefit, Employment, PayAsYouEarnDetails}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{AnyContent, _}
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{~, _}
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.Actions
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{BadGatewayException, HeaderCarrier}
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

  def getTaxHistory() = Action.async {
    implicit request => {
      val nino = request.session.get("USER_NINO").map(Nino(_))

      authorised(AuthProviderAgents).retrieve(affinityGroupAllEnrolls) {
        case Some(affinityG) ~ allEnrols =>
          (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
            case (`isAnAgent`, Some(_)) => {
              retrieveTaxHistoryData(nino)
            }
            case (`isAnAgent`, None) => redirectToSubPage
            case _ => redirectToExitPage
          }
        case _ =>
          redirectToExitPage
      }.recoverWith {
          case b: BadGatewayException => {
            Logger.warn(messagesApi("employmenthistory.technicalerror.message") + s" : Due to BadGatewayException:${b.getMessage}")
            Future.successful( handleHttpResponse("technicalerror",FrontendAppConfig.AfiHomePage,None))
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
  }

  //TODO need to remove this code
  val testEmployment = Employment("AA12341234", "Test Employer Name", Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0),
    List(CompanyBenefit("Benifit1", 1000.00), CompanyBenefit("Benifit2", 2000.00)))
  val paye = PayAsYouEarnDetails(List(testEmployment, testEmployment), List(Allowance("desc", 222.00),Allowance("desc1", 333.00)))


  private def retrieveTaxHistoryData(ninoField:Option[Nino])(implicit hc:HeaderCarrier, request:Request[_]): Future[Result] = ninoField match{
      case Some(nino) =>
        val cy1 = TaxYearResolver.currentTaxYear - 1
        taxHistoryConnector.getTaxHistory(nino, cy1) map {
          historyResponse => historyResponse.status match {
            case OK => {
              val taxHistory = historyResponse.json.as[PayAsYouEarnDetails]
              val sidebarLink = Link.toInternalPage(
                url=FrontendAppConfig.AfiHomePage,
                value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
              Ok(views.html.taxhistory.employments_main(nino.nino, cy1, taxHistory, Some(sidebarLink),headerNavLink=Some(logoutLink))).removingFromSession("USER_NINO")
            }
            case NOT_FOUND => {
              handleHttpResponse("notfound",FrontendAppConfig.AfiHomePage,Some(nino.toString()))
            }
            case UNAUTHORIZED => {
              handleHttpResponse("unauthorised",controllers.routes.MainController.getSelectClientPage().url,Some(nino.toString()))
            }
            case s => {
              Logger.warn("Error response returned with status:"+s)
              handleHttpResponse("technicalerror",FrontendAppConfig.AfiHomePage,None)
            }
          }
        }
      case _ =>
        Logger.warn("No nino supplied.")
        val sidebarLink = Link.toInternalPage(
          url=FrontendAppConfig.AfiHomePage,
          value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
        Future.successful(Ok(views.html.select_client(selectClientForm, Some(sidebarLink),
          headerNavLink=Some(logoutLink))))
  }

  private def handleHttpResponse(message:String,sideBarUrl:String,nino:Option[String])(implicit request:Request[_]) = {
    Logger.warn(messagesApi(s"employmenthistory.${message}.message"))
    val sidebarLink = Link.toInternalPage(
      url = sideBarUrl,
      value = Some(messagesApi(s"employmenthistory.${message}.linktext"))).toHtml
    Ok(views.html.error_template(
      messagesApi(s"employmenthistory.${message}.title"),
      if(nino.isDefined) messagesApi(s"employmenthistory.${message}.header", nino.getOrElse("")) else messagesApi(s"employmenthistory.${message}.header"),
      "",
      Some(sidebarLink),
      headerNavLink = Some(logoutLink),
      gaEventId = Some(message))).removingFromSession("USER_NINO")
  }

  def getSelectClientPage: Action[AnyContent] = Action.async { implicit request =>
    authorised(AuthProviderAgents).retrieve(affinityGroupAllEnrolls) {
      case Some(affinityG) ~ allEnrols =>
        (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
          case (`isAnAgent`, Some(_)) => {
            val sidebarLink = Link.toInternalPage(
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
      case e =>
        handleFailure(e)
    }
  }

  def submitSelectClientPage(): Action[AnyContent] = Action.async { implicit request =>
    selectClientForm.bindFromRequest().fold(
      formWithErrors ⇒ {
        val sidebarLink = Link.toInternalPage(
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
              case (`isAnAgent`, Some(_)) => Future successful Redirect(routes.MainController.getTaxHistory())
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
