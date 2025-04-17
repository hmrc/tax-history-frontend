/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.taxhistory.SignedOut

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignedOutController @Inject() (
  signedOutView: SignedOut,
  override val authConnector: AuthConnector,
  override val config: Configuration,
  override val env: Environment,
  val cc: MessagesControllerComponents,
  implicit val appConfig: AppConfig,
  implicit val executionContext: ExecutionContext
) extends BaseController(cc) {

  val loginContinue: String          = appConfig.loginContinue
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  def logout(): Action[AnyContent] = Action.async {
    logger.info("[SignedOutController][logout] Sign out of the service")
    Future.successful(Redirect(appConfig.signOutUrl, Map("continue" -> Seq(appConfig.exitSurveyUrl))))
  }

  def signedOut: Action[AnyContent] = Action { implicit request =>
    Ok(signedOutView())
  }

  def signOutNoSurvey(): Action[AnyContent] = Action {
    val signOutServiceUrl = appConfig.host + routes.SignedOutController.signedOut().url
    Redirect(
      appConfig.signOutUrl,
      Map("continue" -> Seq(signOutServiceUrl))
    )
  }

  def keepAlive(): Action[AnyContent] = Action {
    NoContent
  }

}
