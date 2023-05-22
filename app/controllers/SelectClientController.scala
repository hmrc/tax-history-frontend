/*
 * Copyright 2023 HM Revenue & Customs
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
import form.SelectClientForm.selectClientForm
import models.taxhistory.SelectClient
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelectClientController @Inject() (
  override val authConnector: AuthConnector,
  override val config: Configuration,
  override val env: Environment,
  val cc: MessagesControllerComponents,
  implicit val appConfig: AppConfig,
  selectClient: views.html.taxhistory.select_client
)(implicit val ec: ExecutionContext)
    extends BaseController(cc) {

  val loginContinue: String          = appConfig.loginContinue
  val serviceSignout: String         = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  val root: Action[AnyContent] = Action {
    Redirect(routes.SelectClientController.getSelectClientPage())
  }

  def getSelectClientPage: Action[AnyContent] = Action.async { implicit request =>
    authorisedAgent(AuthProviderAgents) {
      val sessionNino = getNinoFromSession(request).map(_.nino).getOrElse("")
      val filledForm  = selectClientForm.fill(SelectClient(clientId = sessionNino)).discardingErrors
      Future.successful(Ok(selectClient(filledForm)))
    }
  }

  def submitSelectClientPage: Action[AnyContent] = Action.async { implicit request =>
    selectClientForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(selectClient(formWithErrors))),
        validFormData =>
          authorisedAgent(AuthProviderAgents) {
            Future.successful(
              Redirect(routes.ConfirmDetailsController.getConfirmDetailsPage())
                .addingToSession(ninoSessionKey -> s"${validFormData.clientId.toUpperCase}")
            )
          }
      )
  }
}
