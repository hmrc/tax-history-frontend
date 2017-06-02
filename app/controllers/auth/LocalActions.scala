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

import config.ConfigDecorator
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.play.frontend.auth.{AllowAll, AuthContext, UserActions}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait LocalActions extends ConfidenceLevelAndCredentialStrengthChecker { this: FrontendController with UserActions =>

  def configDecorator: ConfigDecorator

  def localRegime: LocalRegime

  def pageVisibilityPredicate = new LocalPageVisibilityPredicateFactory(configDecorator).build

  def ProtectedAction(block: LocalContext => Future[Result]): Action[AnyContent] = {

    AuthorisedFor(localRegime, pageVisibility = pageVisibilityPredicate).async {
      implicit authContext => implicit request =>
        createLocalContextAndExecute { implicit localContext =>
          block(localContext)
        }
    }
  }


  def createLocalContextAndExecute(block: LocalContext => Future[Result])(implicit authContext: AuthContext, request: Request[AnyContent]): Future[Result] = {

    block(LocalContext(request, authContext))
  }

}
