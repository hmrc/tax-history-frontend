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

import javax.inject.Inject

import config.FrontendAuthConnector
import form.SelectClientForm.selectClientForm
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment}
import views.html.taxhistory.select_client

import scala.concurrent.Future

class SelectClientController @Inject()(
                                        override val authConnector: FrontendAuthConnector,
                                        override val config: Configuration,
                                        override val env: Environment,
                                        implicit val messagesApi: MessagesApi
                                      ) extends BaseController {

  //TODO Remove this as it is only included to support legacy url
  @Deprecated
  def getLegacySelectClientPage: Action[AnyContent] = Action.async { implicit request =>
    redirectToSelectClientPage
  }

  def getSelectClientPage: Action[AnyContent] = Action.async { implicit request =>
    authorisedAgent(AuthProviderAgents) {
      Future.successful(Ok(select_client(selectClientForm)))
    }
  }

  def submitSelectClientPage: Action[AnyContent] = Action.async { implicit request =>
    selectClientForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(select_client(formWithErrors))),
      validFormData =>
        authorisedAgent(AuthProviderAgents) {
          Future successful Redirect(routes.SelectTaxYearController.getSelectTaxYearPage())
            .addingToSession(ninoSessionKey -> s"${validFormData.clientId.toUpperCase}")
        }
    )
  }
}
