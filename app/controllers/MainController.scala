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

import config.{ConfigDecorator, FrontendAuthConnector}
import connectors.TaxHistoryConnector
import controllers.auth.AgentAuth
import form.SelectClientForm.selectClientForm
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
import views.html.select_client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait BaseController extends FrontendController with Actions with Redirects with I18nSupport

class MainController @Inject()(
                                val configDecorator: ConfigDecorator,
                                val taxHistoryConnector: TaxHistoryConnector,
                                val agentAuth: AgentAuth,
                                override val authConnector: FrontendAuthConnector,
                                override val config: Configuration,
                                override val env: Environment,
                                implicit val messagesApi: MessagesApi
                              ) extends BaseController with AuthorisedFunctions {

  lazy val ggSignInRedirect: Result = toGGLogin(s"${configDecorator.loginContinue}")

  def get() = Action.async {
    implicit request => {
      val nino: Option[Nino] = request.session.get("USER_NINO").map(Nino(_))
      authorised(Enrolment("HMRC-AS-AGENT") and AuthProviders(GovernmentGateway)) {
        val cy1 = TaxYearResolver.currentTaxYear - 1
        nino match {
          case Some(nino) =>
            taxHistoryConnector.getTaxHistory(nino, cy1) map {
              taxHistory =>
                Ok(views.html.taxhistory.employments_main("Test User", nino.nino, cy1, taxHistory)).removingFromSession("USER_NINO")
            }
          case None =>
            Future.successful(NotFound("User had no nino"))
        }
      }.recoverWith {
        case _: BadGatewayException => {
          val message = "Tax History Connector not available"
          Future.successful(Ok(views.html.error_template(message, message, message)).removingFromSession("USER_NINO"))
        }
        case _ => Future.successful(ggSignInRedirect)
      }
    }
  }

  def getSelectClientPage(): Action[AnyContent] =
    agentAuth.authorisedForAfi { implicit request => {
        Future.successful(Ok(select_client(selectClientForm)))
      }
    }

  def submitSelectClientPage(): Action[AnyContent] = agentAuth.authorisedForAfi { implicit request =>
    selectClientForm.bindFromRequest().fold(
      formWithErrors ⇒ Future.successful(BadRequest(select_client(formWithErrors))),
      validFormData => {
        Future successful Redirect(routes.MainController.get()).addingToSession("USER_NINO" -> s"${validFormData.clientId}")
      }
    )
  }


}
