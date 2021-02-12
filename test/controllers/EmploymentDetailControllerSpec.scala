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

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import model.api._
import org.joda.time.LocalDate
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import support.ControllerSpec
import support.fixtures.PersonFixture
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.http.HttpResponse
import views.TestAppConfig

import scala.concurrent.Future

class EmploymentDetailControllerSpec extends ControllerSpec with PersonFixture with TestAppConfig {

  trait HappyPathSetup extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentDetailController = {

      val c = new EmploymentDetailController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        app.configuration, injected[Environment], injected[MessagesControllerComponents])(stubControllerComponents().executionContext, appConfig)

      val cbUUID = UUID.randomUUID()
      val companyBenefits = List(
        CompanyBenefit(cbUUID, "EmployerProvidedServices", 1000.00, Some(1), isForecastBenefit = true),
        CompanyBenefit(cbUUID, "CarFuelBenefit", 1000, isForecastBenefit = true)
      )

      val payAndTax = PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(4896.80),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(979.36),
        studentLoan = None,
        studentLoanIncludingEYU  = None,
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = List.empty
      )

      val employment = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye-1",
        employerName = "employer-1",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(LocalDate.parse("2017-01-01")),
        companyBenefitsURI = None,
        payAndTaxURI = None,
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716"
      )

      val incomeSource = Some(new IncomeSource(1, 1, None, List.empty, List.empty, "", None, 1, ""))


      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))

      when(c.taxHistoryConnector.getPayAndTaxDetails(any, any, any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.OK, Some(Json.toJson(payAndTax)))))

      when(c.taxHistoryConnector.getEmployment(any, any, any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.OK, Some(Json.toJson(employment)))))

      when(c.taxHistoryConnector.getCompanyBenefits(any, any, any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.OK, Some(Json.toJson(companyBenefits)))))

      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.OK, Some(Json.toJson(person)))))

      when(c.taxHistoryConnector.getIncomeSource(any,any,any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.OK, Some(Json.toJson(incomeSource)))))

      c
    }
  }

  "EmploymentDetailController" must {
    "successfully load Employment details page" in new HappyPathSetup {
      val result: Future[Result] = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include("first name second name")

    }

    "redirect to /select-client page when there is no nino in session" in new HappyPathSetup {
      val result: Future[Result] = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SelectClientController.getSelectClientPage().url)
    }

    "redirect to /client-income-record/:year when employment is not found (getEmployment returns 404)" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getEmployment(any, any, any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,
          None)))
      val result: Future[Result] = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmploymentSummaryController.getTaxHistory(2014).url)
    }

    "show employment details page even if getPayAndTaxDetails returns 404" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getPayAndTaxDetails(any, any, any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,
          None)))
      val result: Future[Result] = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.employment.details.title"))
    }

    "show employment details page even if getCompanyBenefits returns 404" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getCompanyBenefits(any, any, any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,
          None)))
      val result: Future[Result] = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession(
        "USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.employment.details.title"))
    }

    "show employment details page even if getIncomeSource returns 404" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getIncomeSource(any, any, any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,
          None)))
      val result: Future[Result] = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession(
        "USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.employment.details.title"))
    }

    "show technical error page when getEmployment returns a status other than 200, 401, 404" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getEmployment(any, any, any)(any)).
        thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR,
          None)))
      val result: Future[Result] = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }
  }
}
