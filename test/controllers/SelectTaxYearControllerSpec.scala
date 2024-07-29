/*
 * Copyright 2024 HM Revenue & Customs
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
import form.SelectTaxYearForm.selectTaxYearForm
import model.api.IndividualTaxYear
import models.taxhistory.SelectTaxYear
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito.{mock, when}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
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
import views.html.taxhistory.select_tax_year

import scala.concurrent.{ExecutionContext, Future}

class SelectTaxYearControllerSpec extends ControllerSpec with ControllerFixture with BaseSpec {

  trait LocalSetup {

    val taxYear = 2015

    lazy val controller: SelectTaxYearController = new SelectTaxYearController(
      mock(classOf[TaxHistoryConnector]),
      mock(classOf[CitizenDetailsConnector]),
      mock(classOf[AuthConnector]),
      app.configuration,
      environment,
      messagesControllerComponents,
      appConfig,
      injected[select_tax_year]
    )(stubControllerComponents().executionContext)

    when(
      controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
        any[HeaderCarrier],
        any[ExecutionContext]
      )
    )
      .thenReturn(
        Future.successful(
          new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))
        )
      )

    when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
      .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(person), Map.empty)))

    when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
      .thenReturn(
        Future.successful(
          HttpResponse(OK, json = Json.toJson(List(IndividualTaxYear(taxYear, "uri1", "uri2", "uri3"))), Map.empty)
        )
      )
  }

  "SelectTaxYearController" must {

    "load select tax year page" in new LocalSetup {

      private val maxChar: Int           = 100
      val postData: JsObject             = Json.obj("selectTaxYear" -> "2015")
      val validForm: Form[SelectTaxYear] = selectTaxYearForm.bind(postData, maxChar)
      val name: Option[String]           = Some("Test Name")

      val view: select_tax_year = injected[select_tax_year]

      val result: Future[Result] =
        controller.getSelectTaxYearPage().apply(fakeRequestWithNino.withMethod("POST"))

      status(result)          shouldBe OK
      contentAsString(result) shouldBe
        view(validForm, List("2015" -> "2015 to 2016"), noSelectedTaxYear, name, nino)(
          fakeRequestWithNino,
          messages,
          appConfig
        ).toString()
    }

    "redirect to technical error page when getTaxYears returns status internal server error" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val result: Future[Result] =
        controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino).withMethod("POST"))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "redirect to technical error page when getTaxYears returns 4xx status" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))

      val result: Future[Result] =
        controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino).withMethod("POST"))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "redirect to technical error page when getTaxYears returns 3xx status" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(SEE_OTHER, "")))

      val result: Future[Result] =
        controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino).withMethod("POST"))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "return not found error page when citizen details returns locked status 423" in new LocalSetup {
      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(LOCKED, "")))

      val result: Future[Result] =
        controller.getSelectTaxYearPage().apply(FakeRequest().withSession("USER_NINO" -> nino).withMethod("POST"))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getMciRestricted().url)
    }

    "redirect to summary page successfully on valid data" in new LocalSetup {

      val taxYear2016 = 2016

      val validSelectTaxYearForm: Seq[(String, String)] = Seq(
        "selectTaxYear" -> "2016"
      )

      val result: Future[Result] = controller
        .submitSelectTaxYearPage()
        .apply(
          FakeRequest()
            .withSession("USER_NINO" -> nino)
            .withFormUrlEncodedBody(validSelectTaxYearForm: _*)
            .withMethod("POST")
        )

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmploymentSummaryController.getTaxHistory(taxYear2016).url)
    }

    "redirect to select client page when there is no nino in session and submission made with invalid data" in new LocalSetup {
      val invalidSelectTaxYearForm: Seq[(String, String)] = Seq(
        "selectTaxYear" -> ""
      )

      val result: Future[Result] = controller
        .submitSelectTaxYearPage()
        .apply(
          FakeRequest()
            .withFormUrlEncodedBody(invalidSelectTaxYearForm: _*)
            .withMethod("POST")
        )

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SelectClientController.getSelectClientPage().url)
    }

    "fail submission on invalid data" in new LocalSetup {
      val validSelectTaxYearForm: Seq[(String, String)] = Seq(
        "selectTaxYear" -> ""
      )

      val result: Future[Result] = controller
        .submitSelectTaxYearPage()
        .apply(
          FakeRequest()
            .withSession("USER_NINO" -> nino)
            .withFormUrlEncodedBody(validSelectTaxYearForm: _*)
            .withMethod("POST")
        )
      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(Messages("employmenthistory.select.tax.year.error.message"))
    }

    "redirect to technical error page when getTaxYears return error on submission" in new LocalSetup {
      val validSelectTaxYearForm: Seq[(String, String)] = Seq(
        "selectTaxYear" -> ""
      )
      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val result: Future[Result] = controller
        .submitSelectTaxYearPage()
        .apply(
          FakeRequest()
            .withSession("USER_NINO" -> nino)
            .withFormUrlEncodedBody(validSelectTaxYearForm: _*)
            .withMethod("POST")
        )

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }
  }
}
