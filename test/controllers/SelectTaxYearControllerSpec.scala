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

import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import model.api.IndividualTaxYear
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import play.api.Environment
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.ControllerSpec
import support.fixtures.PersonFixture
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.http.HttpResponse
import views.TestAppConfig

import scala.concurrent.Future
class SelectTaxYearControllerSpec extends ControllerSpec with PersonFixture with TestAppConfig {

  trait LocalSetup extends MockitoSugar with ArgumentMatchersSugar {

    lazy val controller = {

      val c = new SelectTaxYearController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        app.configuration, injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      when(c.taxHistoryConnector.getTaxYears(any)(any)).thenReturn(Future.successful(HttpResponse(Status.OK,
        Some(Json.toJson(List(IndividualTaxYear(2015, "uri1","uri2","uri3")))))))
      c
    }
  }

  "SelectTaxYearController" must {

    "load select tax year page" in new LocalSetup {
      val result = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.select.tax.year.title"))
    }

    "redirect to technical error page when getTaxYears reurn status internal server error" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxYears(any)(any))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR)))
      val result = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "return not found error page when citizen details returns locked status 423" in new LocalSetup {
      when(controller.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.LOCKED,None)))
      val result = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation (result) shouldBe Some(controllers.routes.ClientErrorController.getMciRestricted().url)
    }

    "redirect to summary page successfully on valid data" in new LocalSetup {
      controller.submitSelectTaxYearPage()
      val validSelectTaxYearForm = Seq(
        "selectTaxYear" -> "2016"
      )

      val result = controller.submitSelectTaxYearPage().apply(FakeRequest()
        .withSession("USER_NINO" -> nino).withFormUrlEncodedBody(validSelectTaxYearForm: _*))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmploymentSummaryController.getTaxHistory(2016).url)
    }

    "fail submission on invalid data" in new LocalSetup {
      val validSelectTaxYearForm = Seq(
        "selectTaxYear" -> ""
      )

      val result = controller.submitSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino)
        .withFormUrlEncodedBody(validSelectTaxYearForm: _*))
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include(Messages("employmenthistory.select.tax.year.error.message"))
    }

    "redirect to technical error page when getTaxYears return error on submission" in new LocalSetup {
      val validSelectTaxYearForm = Seq(
        "selectTaxYear" -> ""
      )
      when(controller.taxHistoryConnector.getTaxYears(any)(any))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR)))

      val result = controller.submitSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino)
        .withFormUrlEncodedBody(validSelectTaxYearForm: _*))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }
  }
}



