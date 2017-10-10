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

import models.taxhistory.Person
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.test.FakeRequest
import support.BaseSpec
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

import scala.concurrent.Future

class SelectClientControllerSpec extends BaseSpec {

  trait LocalSetup {

    lazy val controller = {

      val person = Some(Person(Some("first name"),Some("second name"), false))
      val c = injected[SelectClientController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      c
    }
  }

  "return Status: 400 when invalid data is input" in new LocalSetup {
    val invalidTestNINO = "9999999999999999"

    val invalidSelectClientForm = Seq(
      "clientId" -> invalidTestNINO
    )

    val result = controller.submitSelectClientPage().apply(FakeRequest()
      .withFormUrlEncodedBody(invalidSelectClientForm: _*))
    status(result) shouldBe Status.BAD_REQUEST
  }

}
