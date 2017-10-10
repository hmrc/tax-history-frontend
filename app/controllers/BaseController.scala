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

import config.ConfigDecorator
import controllers.auth.AgentAuth
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.urls.Link

import scala.concurrent.Future

trait BaseController extends FrontendController with I18nSupport with AgentAuth {
  val configDecorator: ConfigDecorator
  lazy val ggSignInRedirect: Result = toGGLogin(s"${configDecorator.loginContinue}")

  lazy val logoutLink = Link.toInternalPage(url = controllers.routes.MainController.logout.url, value = Some("Sign out")).toHtml

  def logout() = Action.async {
    implicit request => {
      Future.successful(ggSignInRedirect.withNewSession)
    }
  }
}
