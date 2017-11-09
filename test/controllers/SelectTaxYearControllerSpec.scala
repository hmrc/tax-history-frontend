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
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class SelectTaxYearControllerSpec extends BaseControllerSpec {

  trait LocalSetup {

    lazy val controller = {

      val c = injected[SelectTaxYearController]
      val person = Some(Person(Some("first name"),Some("second name"), false))
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  "SelectTaxYearController" must {

    "load select tax year page" in new LocalSetup {
      val result = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.select.tax.year.title"))
    }

    "redirect to summary page successfully on valid data" in new LocalSetup {
      controller.submitSelectTaxYearPage()
      val validSelectTaxYearForm = Seq(
        "selectTaxYear" -> "2016"
      )

      val result = controller.submitSelectTaxYearPage().apply(FakeRequest().withFormUrlEncodedBody(validSelectTaxYearForm: _*))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmploymentSummaryController.getTaxHistory(2016).url)
    }

    "fail submission on invalid data" in new LocalSetup {
      val validSelectTaxYearForm = Seq(
        "selectTaxYear" -> ""
      )

      val result = controller.submitSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino).withFormUrlEncodedBody(validSelectTaxYearForm: _*))
      status(result) shouldBe Status.BAD_REQUEST
    }
  }
}



