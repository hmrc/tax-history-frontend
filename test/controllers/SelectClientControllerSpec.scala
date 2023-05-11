/*
 * Copyright 2023 HM Revenue & Customs
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

import form.SelectClientForm.selectClientForm
import org.mockito.ArgumentMatchers.any
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.taxhistory.select_client

import scala.concurrent.{ExecutionContext, Future}

class SelectClientControllerSpec extends ControllerSpec with BaseSpec {

  trait LocalSetup {

    lazy val controller: SelectClientController = {

      val c = new SelectClientController(
        mock[AuthConnector],
        app.configuration,
        environment,
        messagesControllerComponents,
        appConfig,
        injected[select_client]
      )(stubControllerComponents().executionContext)

      when(
        c.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(
          Future.successful(
            new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))
          )
        )
      c
    }
  }

  "SelectClientController" must {

    "load select client page" in new LocalSetup {
      implicit val request: Request[_] = FakeRequest()
      val result: Future[Result]       = controller.getSelectClientPage()(fakeRequest)
      val expectedView: select_client  = app.injector.instanceOf[select_client]
      status(result) shouldBe OK
      result rendersTheSameViewAs expectedView(selectClientForm)
    }

    "return Status: 400 when invalid data is entered" in new LocalSetup {
      val invalidTestNINO = "9999999999999999"

      val invalidSelectClientForm: Seq[(String, String)] = Seq(
        "clientId" -> invalidTestNINO
      )

      val result: Future[Result] = controller
        .submitSelectClientPage()
        .apply(
          FakeRequest()
            .withFormUrlEncodedBody(invalidSelectClientForm: _*)
            .withMethod("POST")
        )
      status(result) shouldBe BAD_REQUEST
    }

    "successfully redirect to next page when valid nino is supplied as input" in new LocalSetup {

      val validSelectClientForm: Seq[(String, String)] = Seq(
        "clientId" -> nino
      )

      val result: Future[Result] =
        controller
          .submitSelectClientPage()
          .apply(FakeRequest().withFormUrlEncodedBody(validSelectClientForm: _*).withMethod("POST"))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ConfirmDetailsController.getConfirmDetailsPage().url)
    }

    "successfully redirects to itself" in new LocalSetup {
      val result: Future[Result] = controller.root()(fakeRequest)

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SelectClientController.getSelectClientPage().url)
    }
  }

}
