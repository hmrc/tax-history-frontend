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

import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import model.api._
import org.mockito.ArgumentMatchers.{any, eq => argEq}
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

  trait LocalSetup {

    lazy val taxYear: Int       = 2014
    lazy val employmentId: UUID = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3")

    val controller = new EmploymentDetailController(
      taxHistoryConnector = mock[TaxHistoryConnector],
      citizenDetailsConnector = mock[CitizenDetailsConnector],
      authConnector = mock[AuthConnector],
      config = app.configuration,
      env = environment,
      cc = messagesControllerComponents,
      employmentDetail = injected[employment_detail],
      dateUtils = dateUtils
    )(stubControllerComponents().executionContext, appConfig)

    val incomeSource: Some[IncomeSource] =
      Some(
        IncomeSource(
          employmentId = 1,
          employmentType = 1,
          actualPUPCodedInCYPlusOneTaxYear = None,
          deductions = List.empty,
          allowances = List.empty,
          taxCode = "",
          basisOperation = None,
          employmentTaxDistrictNumber = 1,
          employmentPayeRef = ""
        )
      )

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

    when(
      controller.taxHistoryConnector
        .getPayAndTaxDetails(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
    )
      .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(payAndTax), Map.empty)))

    when(
      controller.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
        any[HeaderCarrier]
      )
    )
      .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(employment), Map.empty)))

    when(
      controller.taxHistoryConnector
        .getCompanyBenefits(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
    )
      .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(companyBenefits), Map.empty)))

    when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
      .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(person), Map.empty)))

    when(
      controller.taxHistoryConnector.getIncomeSource(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
        any[HeaderCarrier]
      )
    )
      .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(incomeSource), Map.empty)))
  }

  "EmploymentDetailController" must {

    "calling .recoverWithEmptyDefault()" when {

      "A Future(value) throws an exception, catch the exception and return the given value" in new LocalSetup {
        private val inputValue = Some(5)
        val actual: Some[Int]  =
          await(
            Future(throw new Exception("Hello")).recoverWith(controller.recoverWithEmptyDefault("", inputValue))
          )

        val expected: Some[Int] = await(Future(inputValue))

        actual shouldBe expected
      }
    }

    "calling .getPayAndTax()" when {

      "the StudentLoanFlagFeature is DISABLED" should {

        "return a PayAndTax() with no studentLoan" in new LocalSetup {

          val actual: Option[PayAndTax] = await(
            controller.getPayAndTax(Nino(nino), taxYear, employmentId.toString, turnOnStudentLoanFlagTest = false)
          )
          val expected: Some[PayAndTax] = Some(payAndTax.copy(studentLoan = None))

          actual shouldBe expected
        }
      }

      "the StudentLoanFlagFeature is ENABLED" should {

        "return the original PayAndTax() with a studentLoan of 1337" in new LocalSetup {

          val actual: Option[PayAndTax] = await(controller.getPayAndTax(Nino(nino), taxYear, employmentId.toString))
          val expected: Some[PayAndTax] = Some(payAndTax)

          actual shouldBe expected
        }
      }

    }

    "successfully load Employment details page" in new LocalSetup {

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe OK
      contentAsString(result) should include("first name second name")
    }

    "redirect to /select-client page when there is no nino in session" in new LocalSetup {
      val result: Future[Result] = controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest)
      status(result)           shouldBe SEE_OTHER
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
      status(result) shouldBe SEE_OTHER
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
      status(result) shouldBe OK
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
      status(result) shouldBe OK
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
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.employment.details.employment.title"))
    }

    "show technical error page when getEmployment returns a status other than 200, 401, 404" in new LocalSetup {
      when(
        controller.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "show technical error page when getEmployment returns a 4xx response" in new LocalSetup {
      when(
        controller.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "show technical error page when getEmployment returns a 3xx response" in new LocalSetup {
      when(
        controller.taxHistoryConnector.getEmployment(argEq(Nino(nino)), argEq(taxYear), argEq(employmentId.toString))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(HttpResponse(SEE_OTHER, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }

    "show deceased error page when retrieveCitizenDetails returns Gone" in new LocalSetup {

      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(GONE, "")))

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getDeceased().url)
    }

    "show no data available error page when retrieveCitizenDetails returns Locked" in new LocalSetup {

      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier])).thenReturn(
        Future.successful(HttpResponse(LOCKED, ""))
      )

      val result: Future[Result] =
        controller.getEmploymentDetails(employmentId.toString, taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getMciRestricted().url)
    }
  }
}
