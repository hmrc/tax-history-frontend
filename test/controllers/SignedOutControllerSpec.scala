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

import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.taxhistory.SignedOut

import java.net.URLEncoder
import scala.concurrent.Future

class SignedOutControllerSpec extends ControllerSpec with BaseSpec {

  trait LocalSetup {
    lazy val controller = new SignedOutController(
      injected[SignedOut],
      injected[AuthConnector],
      injected[Configuration],
      injected[Environment],
      messagesControllerComponents,
      appConfig,
      ec
    )
  }

  "keepAlive" must {
    "return a NoContent" in new LocalSetup {
      status(controller.keepAlive().apply(fakeRequest)) shouldBe NO_CONTENT
    }
  }

  "signedOut" must {
    "return a NoContent" in new LocalSetup {
      status(controller.signedOut().apply(fakeRequest)) shouldBe OK
    }
  }

  "logout" must {
    "redirect to feed back survey link" in new LocalSetup {
      val result: Future[Result] = controller.logout()(fakeRequest)
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controller.appConfig.signOutUrl + "?continue=" + URLEncoder.encode(controller.appConfig.exitSurveyUrl, "UTF-8")
      )
    }
  }

  "signOutNoSurvey" must {
    "redirect to custom IRV sign out page link" in new LocalSetup {
      val expectedContinue: String = controller.appConfig.host + routes.SignedOutController.signedOut().url
      val result: Future[Result]   = controller.signOutNoSurvey()(fakeRequest)
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controller.appConfig.signOutUrl + "?continue=" + URLEncoder.encode(expectedContinue, "UTF-8")
      )
    }
  }

}
