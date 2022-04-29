/*
 * Copyright 2022 HM Revenue & Customs
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

import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import support.{BaseSpec, ControllerSpec}
import views.html.taxhistory.SignedOut

class SignedOutControllerSpec extends ControllerSpec with BaseSpec {

  trait LocalSetup extends MockitoSugar {
    lazy val controller: SignedOutController = {
      val c = new SignedOutController(injected[SignedOut], messagesControllerComponents, appConfig, stubControllerComponents().executionContext)
      c
    }
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

}

