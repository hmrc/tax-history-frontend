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

import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import model.api.IndividualTaxYear
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.fixtures.ControllerFixture
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.DateUtils
import views.html.taxhistory.select_tax_year

import scala.concurrent.{ExecutionContext, Future}

class SelectTaxYearControllerSpec extends ControllerSpec with ControllerFixture with BaseSpec {

  trait LocalSetup extends MockitoSugar {

    lazy val controller: SelectTaxYearController = {

      val c = new SelectTaxYearController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        app.configuration, environment, messagesControllerComponents, appConfig, injected[select_tax_year], injected[DateUtils])(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))

      when(c.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(person), Map.empty)))

      when(c.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(List(IndividualTaxYear(2015, "uri1","uri2","uri3"))), Map.empty)))
      c
    }
  }

  "SelectTaxYearController" must {

    "load select tax year page" in new LocalSetup {
      val result: Future[Result] = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.select.tax.year.title"))
    }

    "redirect to technical error page when getTaxYears returns status internal server error" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, null)))

      val result: Future[Result] = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "redirect to technical error page when getTaxYears returns 4xx status" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Status.BAD_REQUEST, null)))

      val result: Future[Result] = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "redirect to technical error page when getTaxYears returns 3xx status" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Status.SEE_OTHER, null)))

      val result: Future[Result] = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "return not found error page when citizen details returns locked status 423" in new LocalSetup {
      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Status.LOCKED, null)))

      val result: Future[Result] = controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation (result) shouldBe Some(controllers.routes.ClientErrorController.getMciRestricted().url)
    }

    "redirect to summary page successfully on valid data" in new LocalSetup {

      val validSelectTaxYearForm = Seq(
        "selectTaxYear" -> "2016"
      )

      val result: Future[Result] = controller.submitSelectTaxYearPage().apply(FakeRequest()
        .withSession("USER_NINO" -> nino).withFormUrlEncodedBody(validSelectTaxYearForm: _*))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmploymentSummaryController.getTaxHistory(2016).url)
    }

    "fail submission on invalid data" in new LocalSetup {
      val validSelectTaxYearForm = Seq(
        "selectTaxYear" -> ""
      )

      val result: Future[Result] = controller.submitSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino)
        .withFormUrlEncodedBody(validSelectTaxYearForm: _*))
      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) should include(Messages("employmenthistory.select.tax.year.error.message"))
    }

    "redirect to technical error page when getTaxYears return error on submission" in new LocalSetup {
      val validSelectTaxYearForm = Seq(
        "selectTaxYear" -> ""
      )
      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, null)))

      val result: Future[Result] = controller.submitSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino)
        .withFormUrlEncodedBody(validSelectTaxYearForm: _*))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }
  }
}



