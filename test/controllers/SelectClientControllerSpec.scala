/*
 * Copyright 2021 HM Revenue & Customs
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

import org.mockito.MockitoSugar
import play.api.Environment
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.ControllerSpec
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import views.TestAppConfig

import scala.concurrent.Future

class SelectClientControllerSpec extends ControllerSpec with TestAppConfig {

  trait LocalSetup extends MockitoSugar {

    lazy val controller = {

      val c = new SelectClientController(mock[AuthConnector],
        app.configuration, injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      c
    }
  }

  "SelectClientController" must {

    "load select client page" in new LocalSetup {
      val result  = controller.getSelectClientPage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.select.client.title"))
    }

    "return Status: 400 when invalid data is entered" in new LocalSetup {
      val invalidTestNINO = "9999999999999999"

      val invalidSelectClientForm = Seq(
        "clientId" -> invalidTestNINO
      )

      val result = controller.submitSelectClientPage().apply(FakeRequest()
        .withFormUrlEncodedBody(invalidSelectClientForm: _*))
      status(result) shouldBe Status.BAD_REQUEST
    }

    "successfully redirect to next page when valid nino is supplied as input" in new LocalSetup {

      val validSelectClientForm = Seq(
        "clientId" -> nino
      )

      val result = controller.submitSelectClientPage().apply(FakeRequest().withFormUrlEncodedBody(validSelectClientForm: _*))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SelectTaxYearController.getSelectTaxYearPage().url)
    }
  }

}



