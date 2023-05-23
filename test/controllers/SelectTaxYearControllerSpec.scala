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

import connectors.TaxHistoryConnector
import form.SelectTaxYearForm.selectTaxYearForm
import model.api.IndividualTaxYear
import models.taxhistory.SelectTaxYear
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import views.html.taxhistory.select_tax_year

import scala.concurrent.{ExecutionContext, Future}

class SelectTaxYearControllerSpec extends ControllerSpec with BaseSpec {

  private trait LocalSetup {

    val taxYear: Int = 2015

    lazy val controller: SelectTaxYearController = new SelectTaxYearController(
      mock[TaxHistoryConnector],
      mock[AuthConnector],
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

    when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
      .thenReturn(
        Future.successful(
          HttpResponse(OK, json = Json.toJson(List(IndividualTaxYear(taxYear, "uri1", "uri2", "uri3"))), Map.empty)
        )
      )
  }

  "SelectTaxYearController" must {
    "load select tax year page" in new LocalSetup {
      val validForm: Form[SelectTaxYear] = selectTaxYearForm.bind(Map("selectTaxYear" -> "2015"))

      val expectedView: Html = inject[select_tax_year].apply(
        validForm,
        List("2015" -> "2015 to 2016"),
        noSelectedTaxYear
      )(
        fakeRequestWithNino,
        messages,
        appConfig
      )

      val result: Future[Result] = controller.getSelectTaxYearPage()(fakeRequestWithNino)

      status(result) shouldBe OK
      result.rendersTheSameViewAs(expectedView)
    }

    "redirect to technical error page" when {
      def test(errorStatus: Int): Unit =
        s"getTaxYears returns $errorStatus" in new LocalSetup {
          when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
            .thenReturn(Future.successful(HttpResponse(errorStatus, "")))

          val result: Future[Result] = controller.getSelectTaxYearPage()(fakeRequestWithNino)

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
        }

      Seq(INTERNAL_SERVER_ERROR, BAD_REQUEST, SEE_OTHER).foreach(test)
    }

    "redirect to summary page successfully on valid data" in new LocalSetup {
      override val taxYear: Int = 2016

      val validSelectTaxYearForm: Seq[(String, String)] = Seq(
        "selectTaxYear" -> "2016"
      )

      val result: Future[Result] = controller
        .submitSelectTaxYearPage()
        .apply(
          fakeRequestWithNino
            .withFormUrlEncodedBody(validSelectTaxYearForm: _*)
            .withMethod("POST")
        )

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmploymentSummaryController.getTaxHistory(taxYear).url)
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
      val invalidSelectTaxYearForm: Seq[(String, String)] = Seq(
        "selectTaxYear" -> ""
      )

      val result: Future[Result] = controller
        .submitSelectTaxYearPage()
        .apply(
          fakeRequestWithNino
            .withFormUrlEncodedBody(invalidSelectTaxYearForm: _*)
            .withMethod("POST")
        )
      status(result)        shouldBe BAD_REQUEST
      contentAsString(result) should include(Messages("employmenthistory.select.tax.year.error.message"))
    }

    "redirect to technical error page when getTaxYears return error on submission" in new LocalSetup {
      val invalidSelectTaxYearForm: Seq[(String, String)] = Seq(
        "selectTaxYear" -> ""
      )

      when(controller.taxHistoryConnector.getTaxYears(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val result: Future[Result] = controller
        .submitSelectTaxYearPage()
        .apply(
          fakeRequestWithNino
            .withFormUrlEncodedBody(invalidSelectTaxYearForm: _*)
            .withMethod("POST")
        )

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }
  }
}
