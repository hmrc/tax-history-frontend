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

import javax.inject.{Inject, Singleton}

import config.{AppConfig, FrontendAuthConnector}
import form.FastTrackForm.fastTrackForm
import models.taxhistory.FastTrackInvitation
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment}
import services.FastTrackCache
import uk.gov.hmrc.play.bootstrap.controller.ActionWithMdc

import scala.concurrent.Future

@Singleton
class FastTrackController @Inject()(override val authConnector: FrontendAuthConnector,
                                    override val config: Configuration,
                                    override val env: Environment,
                                    cache: FastTrackCache,
                                    implicit val messagesApi: MessagesApi,
                                    implicit val appConfig: AppConfig) extends BaseController {

  val loginContinue: String = appConfig.loginContinue
  val serviceSignout: String = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  val createFastTrack: Action[AnyContent] = Action.async {implicit request =>
    getNinoFromSession(request) match {
      case Some(nino) =>
        fastTrackForm.fill(FastTrackInvitation("HMRC-NI", "ni", nino.value)).fold(
          _ => Future successful Redirect(routes.SelectClientController.getSelectClientPage()),
          validFormData =>
            cache.save(validFormData).map { _ =>
            Redirect(s"${appConfig.agentInvitation}fast-track")
          }
        )
      case _ => Future successful Redirect(routes.SelectClientController.getSelectClientPage())
    }
  }
}