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
import model.api._
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import support.fixtures.ControllerFixture
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import views.html.taxhistory.employment_detail

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class EmploymentDetailControllerSpec extends ControllerSpec with ControllerFixture with BaseSpec {

  trait LocalSetup extends MockitoSugar {

    lazy val taxYear: Int       = 2014
    lazy val employmentId: UUID = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3")

    lazy val controller: EmploymentDetailController = {
      val c = new EmploymentDetailController(
        mock[TaxHistoryConnector],
        mock[CitizenDetailsConnector],
        mock[AuthConnector],
        app.configuration,
        environment,
        messagesControllerComponents,
        injected[employment_detail],
        dateUtils
      )(stubControllerComponents().executionContext, appConfig)

      val incomeSource = Some(new IncomeSource(1, 1, None, List.empty, List.empty, "", None, 1, ""))

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

      when(
        c.taxHistoryConnector.getPayAndTaxDetails(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(payAndTax), Map.empty)))

      when(
        c.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(employment), Map.empty)))

      when(
        c.taxHistoryConnector.getCompanyBenefits(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(companyBenefits), Map.empty)))

      when(c.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(person), Map.empty)))

      when(
        c.taxHistoryConnector.getIncomeSource(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(incomeSource), Map.empty)))

      c
    }
  }

  "EmploymentDetailController" must {
    "successfully load Employment details page" in new LocalSetup {

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include("first name second name")

    }

    "redirect to /select-client page when there is no nino in session" in new LocalSetup {
      val result: Future[Result] = controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest)
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SelectClientController.getSelectClientPage().url)
    }

    "redirect to /client-income-record/:year when employment is not found (getEmployment returns 404)" in new LocalSetup {
      when(
        controller.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmploymentSummaryController.getTaxHistory(taxYear).url)
    }

    "show employment details page even if getPayAndTaxDetails returns 404" in new LocalSetup {
      when(
        controller.taxHistoryConnector
          .getPayAndTaxDetails(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(any[HeaderCarrier])
      )
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.employment.details.employment.title"))
    }

    "show employment details page even if getCompanyBenefits returns 404" in new LocalSetup {
      when(
        controller.taxHistoryConnector
          .getCompanyBenefits(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(any[HeaderCarrier])
      )
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.employment.details.employment.title"))
    }

    "show employment details page even if getIncomeSource returns 404" in new LocalSetup {
      when(
        controller.taxHistoryConnector.getIncomeSource(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.employment.details.employment.title"))
    }

    "show technical error page when getEmployment returns a status other than 200, 401, 404" in new LocalSetup {
      when(
        controller.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "show technical error page when getEmployment returns a 4xx response" in new LocalSetup {
      when(
        controller.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(Status.BAD_REQUEST, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "show technical error page when getEmployment returns a 3xx response" in new LocalSetup {
      when(
        controller.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(Status.SEE_OTHER, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }
  }
}
