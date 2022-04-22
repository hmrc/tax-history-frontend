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

import akka.actor.ActorSystem
import akka.stream.Materializer
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import model.api.EmploymentPaymentType.OccupationalPension
import model.api._
import models.taxhistory.Person
import org.joda.time.LocalDate
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, _}
import support.fixtures.PersonFixture
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import views.html.taxhistory.employment_summary

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class EmploymentSummaryControllerSpec extends ControllerSpec with PersonFixture with BaseSpec with ScalaFutures {

  lazy val taxYear: Int = 2016

  private val employment: Employment = Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = Some(LocalDate.parse("2016-01-21")),
    endDate = Some(LocalDate.parse("2017-01-01")),
    companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
    payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
    employmentPaymentType = None,
    employmentStatus = EmploymentStatus.Live,
    worksNumber = "00191048716"
  )

  private val allowance: Allowance = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "EarlierYearsAdjustment",
    amount = BigDecimal(32.00))

  private val taxAccount: TaxAccount = TaxAccount(taxAccountId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    outstandingDebtRestriction = Some(200),
    underpaymentAmount = Some(300),
    actualPUPCodedInCYPlusOneTaxYear = Some(400))

  private val employments = List(employment, employment.copy(employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae4"), employmentPaymentType = Some(OccupationalPension)))
  private val allowances = List(allowance)

  private val statePension: StatePension = StatePension(100, "test")

  private val payAndTaxFixedUUID = Map(
    "01318d7c-bcd9-47e2-8c38-551e7ccdfae3" ->
      PayAndTax(
        payAndTaxId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(12.34),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(56.78),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = List.empty),
    "01318d7c-bcd9-47e2-8c38-551e7ccdfae4" ->
      PayAndTax(
        payAndTaxId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae4"),
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(90.12),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(34.56),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = List.empty))

  private val payAndTaxRandomUUID = Map(
    UUID.randomUUID().toString ->
      PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(12.34),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(56.78),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = List.empty),
    UUID.randomUUID().toString ->
      PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(90.12),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(34.56),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = List.empty))

  trait LocalSetup extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: Materializer = Materializer(actorSystem)
    lazy val controller: EmploymentSummaryController = {

      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        app.configuration, environment, messagesControllerComponents, appConfig, injected[employment_summary])(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))

      when(c.taxHistoryConnector.getEmploymentsAndPensions(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(employments), Map.empty)))

      when(c.taxHistoryConnector.getAllowances(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(allowances), Map.empty)))

      when(c.taxHistoryConnector.getTaxAccount(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(taxAccount), Map.empty)))

      when(c.taxHistoryConnector.getStatePension(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(statePension), Map.empty)))

      when(c.taxHistoryConnector.getAllPayAndTax(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(payAndTaxFixedUUID), Map.empty)))

      when(c.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(person), Map.empty)))
      c
    }
  }

  "GET /tax-history" should {
    "return 200" in new LocalSetup {
      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.title"))
    }

    "return 200 when empty list of allowances found" in new LocalSetup {
      when(controller.taxHistoryConnector.getAllowances(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, json = Json.arr(), Map.empty)))

      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.title"))
    }

    "return 200 when null allowances found" in new LocalSetup {
      when(controller.taxHistoryConnector.getAllowances(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, null)))

      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.title"))
    }

    "return 200 when no tax account found" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, null)))

      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.title"))
    }

    "return 200 when no pay and tax found" in new LocalSetup {
      when(controller.taxHistoryConnector.getAllPayAndTax(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, json = Json.toJson(payAndTaxFixedUUID), Map.empty)))

      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.title"))
      contentAsString(result) should include(Messages("employmenthistory.error.no-record"))
    }

    "return 200 when pay and tax records do not match employment records" in new LocalSetup {
      when(controller.taxHistoryConnector.getAllPayAndTax(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(payAndTaxRandomUUID), Map.empty)))

      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.title"))
    }

    "return 200 and show technical error page when no citizen details available" in new LocalSetup {
      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, null)))

      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }

    "return not found error page when citizen details returns locked status 423" in new LocalSetup {
      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(LOCKED, null)))

      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getMciRestricted().url)
    }

    "return not found error page when citizen details returns deceased indicator" in new LocalSetup {
      val deceasedPerson: Option[Person] = Some(Person(Some("James"), Some("Bond"), Some(true)))
      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(deceasedPerson), Map.empty)))

      val result: Future[Result] = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getDeceased().url)
    }

    "show not authorised error page when 401 returned from connector" in new LocalSetup {
      when(controller.taxHistoryConnector.getEmploymentsAndPensions(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier])).
        thenReturn(Future.successful(HttpResponse(UNAUTHORIZED, json = Json.toJson("{Message:Unauthorised}"), Map.empty)))

      val result: Future[Result] = controller.getTaxHistory(taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNotAuthorised().url)
    }

    "show technical error page when any response other than 200, 401, 404 returned from connector" in new LocalSetup {
      when(controller.taxHistoryConnector.getEmploymentsAndPensions(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier])).
        thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, json = Json.toJson("{Message:InternalServerError}"), Map.empty)))

      val result: Future[Result] = controller.getTaxHistory(taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }

    "show select client page when no nino has been set in session" in new LocalSetup {
      val result: Future[Result] = controller.getTaxHistory(taxYear)(fakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SelectClientController.getSelectClientPage().url)
    }

    "redirect to no data available page when no employments found" in new LocalSetup {
      when(controller.taxHistoryConnector.getEmploymentsAndPensions(argEq(Nino(nino)), argEq(taxYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, json = Json.toJson(employments), Map.empty)))

      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNoData(taxYear).url)
    }
  }

  "GET /tax-history/logout" should {
    "redirect to gg and clear session data" in new LocalSetup {
      val result: Future[Result] = controller.logout()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
    }
  }

  "buildIncomeTotals" should {
    val empl1 = employment.copy(employmentId = UUID.randomUUID(), employmentPaymentType = None)
    val empl2 = employment.copy(employmentId = UUID.randomUUID(), employmentPaymentType = None)
    val payAndTax1 = PayAndTax(
      payAndTaxId = empl1.employmentId,
      taxablePayTotal = Some(1),
      taxablePayTotalIncludingEYU = Some(100),
      taxTotal = Some(1000),
      taxTotalIncludingEYU = Some(10000),
      studentLoan = None,
      studentLoanIncludingEYU = None,
      paymentDate = None,
      earlierYearUpdates = Nil)
    val payAndTax2 = PayAndTax(
      payAndTaxId = empl2.employmentId,
      taxablePayTotal = Some(2),
      taxablePayTotalIncludingEYU = Some(200),
      taxTotal = Some(2000),
      taxTotalIncludingEYU = Some(20000),
      studentLoan = None,
      studentLoanIncludingEYU = None,
      paymentDate = None,
      earlierYearUpdates = Nil)

    "return Taxable Pay Totals and Tax Totals that are sums of all employment's Totals including Earlier Year Updates" in {
      val ctrlr = injected[EmploymentSummaryController]
      val totalIncome = await(ctrlr.buildIncomeTotals(
        List(empl1, empl2),
        List(empl1.employmentId.toString -> payAndTax1, empl2.employmentId.toString -> payAndTax2)
      ))

      val expectedTaxablePayTotal = payAndTax1.taxablePayTotalIncludingEYU.get + payAndTax2.taxablePayTotalIncludingEYU.get
      val expectedTaxTotal = payAndTax1.taxTotalIncludingEYU.get + payAndTax2.taxTotalIncludingEYU.get
      totalIncome shouldBe Some(
        TotalIncome(
          employmentIncomeAndTax = List(
            EmploymentIncomeAndTax(empl1.employmentId.toString,payAndTax1.taxablePayTotalIncludingEYU.get,payAndTax1.taxTotalIncludingEYU.get),
            EmploymentIncomeAndTax(empl2.employmentId.toString, payAndTax2.taxablePayTotalIncludingEYU.get, payAndTax2.taxTotalIncludingEYU.get)),
          employmentTaxablePayTotalIncludingEYU = expectedTaxablePayTotal,
          employmentTaxTotalIncludingEYU = expectedTaxTotal,
          pensionTaxablePayTotalIncludingEYU = BigDecimal(0),
          pensionTaxTotalIncludingEYU = BigDecimal(0)
        )
      )
    }

    "return Taxable Pay Totals and Tax Totals that are sums of all pensions's Totals including Earlier Year Updates" in {
      val ctrlr = injected[EmploymentSummaryController]
      val totalIncome = await(ctrlr.buildIncomeTotals(
        List(
          empl1.copy(employmentPaymentType = Some(OccupationalPension)),
          empl2.copy(employmentPaymentType = Some(OccupationalPension))
        ),
        List(empl1.employmentId.toString -> payAndTax1, empl2.employmentId.toString -> payAndTax2)
      ))

      val expectedTaxablePayTotal = payAndTax1.taxablePayTotalIncludingEYU.get + payAndTax2.taxablePayTotalIncludingEYU.get
      val expectedTaxTotal = payAndTax1.taxTotalIncludingEYU.get + payAndTax2.taxTotalIncludingEYU.get
      totalIncome shouldBe Some(
        TotalIncome(
          employmentIncomeAndTax = List(
            EmploymentIncomeAndTax(empl1.employmentId.toString,payAndTax1.taxablePayTotalIncludingEYU.get,payAndTax1.taxTotalIncludingEYU.get),
            EmploymentIncomeAndTax(empl2.employmentId.toString, payAndTax2.taxablePayTotalIncludingEYU.get, payAndTax2.taxTotalIncludingEYU.get)),
          employmentTaxablePayTotalIncludingEYU = BigDecimal(0),
          employmentTaxTotalIncludingEYU = BigDecimal(0),
          pensionTaxablePayTotalIncludingEYU = expectedTaxablePayTotal,
          pensionTaxTotalIncludingEYU = expectedTaxTotal
        )
      )
    }

    "return None if there's no employments and no PayAndTax details" in {
      val ctrlr = injected[EmploymentSummaryController]
      await(ctrlr.buildIncomeTotals(Nil, Nil)) shouldBe None
    }
  }
}
