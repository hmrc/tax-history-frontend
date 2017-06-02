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

package controllers.auth

import javax.inject.Inject

import config.ConfigDecorator
import play.api.mvc.Results._
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.frontend.auth.GovernmentGateway

import scala.concurrent.Future

class LocalAuthenticationProvider @Inject()(val configDecorator: ConfigDecorator) extends GovernmentGateway {

  override def redirectToLogin(implicit request: Request[_]) = ggRedirect
  override def continueURL: String = throw new RuntimeException("Unused")
  override def loginURL: String = throw new RuntimeException("Unused")


  private def ggRedirect(implicit request: Request[_]): Future[Result] = {
    lazy val ggSignIn = s"${configDecorator.companyAuthHost}/${configDecorator.gg_web_context}/sign-in"
    Future.successful(Redirect(ggSignIn, Map(
      "continue" -> Seq(configDecorator.taxHistoryFrontendHost + request.path),
      "accountType" -> Seq("individual"),
      "origin" -> Seq(defaultOrigin)
    )))
  }
}
