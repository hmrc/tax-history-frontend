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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ClientErrorControllerSpec extends BaseControllerSpec {

  trait HappyPathSetup {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = injected[ClientErrorController]
  }

  "ClientErrorController" should {
    "get Mci restricted page" in new HappyPathSetup {
      val result = controller.getMciRestricted().apply(FakeRequest())
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.mci.restricted.title"))
    }

    "get Not Authorised page" in new HappyPathSetup {
      val result = controller.getNotAuthorised().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.not.authorised.title"))
    }

    "get Not Authorised page without NINO" in new HappyPathSetup {
      val result = controller.getNotAuthorised().apply(FakeRequest())
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation (result) shouldBe Some(controllers.routes.SelectClientController.getSelectClientPage().url)
    }

    "get deceased page" in new HappyPathSetup {
      val result = controller.getDeceased().apply(FakeRequest())
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.deceased.title"))
    }
  }
}