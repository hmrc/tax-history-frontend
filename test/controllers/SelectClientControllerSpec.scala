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

import config.FrontendAppConfig.getString
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

import scala.concurrent.Future

class SelectClientControllerSpec extends BaseControllerSpec {

  trait LocalSetup {

    lazy val controller = {

      val c = injected[SelectClientController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      c
    }
  }

  trait NoEnrolmentsSetup {

    lazy val controller = {
      val c = injected[SelectClientController]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(Set()))))
      c
    }
  }

  trait NoEnrolmentsAndNotAnAgentSetup {

    lazy val controller = {
      val c = injected[SelectClientController]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Individual) , Enrolments(Set()))))
      c
    }
  }

  trait NoEnrolmentsAndNoAffinityGroupSetup {

    lazy val controller = {
      val c = injected[SelectClientController]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Individual) , Enrolments(Set()))))
      c
    }
  }

  trait failureOnRetrievalOfEnrolment {

    lazy val controller = {
      val c = injected[SelectClientController]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.failed(new Exception))
      c
    }
  }


  "SelectClientController" must {

    "load select client page" in new LocalSetup {
      val result  = controller.getSelectClientPage()(fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.select.client.title"))
    }

    "redirect to afi-not-an-agent-page when there is no enrolment" in new NoEnrolmentsSetup {
      val result  = controller.getSelectClientPage()(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(getString("external-url.afi-not-an-agent-page.url"))
    }

    "redirect to afi-not-an-agent-page when there is no enrolment and is not an agent" in new NoEnrolmentsAndNotAnAgentSetup {
      val result  = controller.getSelectClientPage()(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(getString("external-url.afi-not-an-agent-page.url"))
    }

    "redirect to afi-not-an-agent-page when there is no enrolment and has no affinity group" in new NoEnrolmentsAndNoAffinityGroupSetup {
      val result  = controller.getSelectClientPage()(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(getString("external-url.afi-not-an-agent-page.url"))
    }

    "load error page when failed to fetch enrolment" in new failureOnRetrievalOfEnrolment {
      val result  = controller.getSelectClientPage()(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(getString("external-url.afi-error.url"))
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
      redirectLocation(result) shouldBe Some(routes.MainController.getTaxHistory().url)
    }
  }

}

