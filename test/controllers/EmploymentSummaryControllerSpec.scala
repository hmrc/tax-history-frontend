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
import model.api.EmploymentPaymentType.OccupationalPension
import model.api._
import models.taxhistory.Person
import org.joda.time.LocalDate
import org.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import support.ControllerSpec
import support.fixtures.PersonFixture
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import views.TestAppConfig

import scala.concurrent.Future

class EmploymentSummaryControllerSpec extends ControllerSpec with PersonFixture with TestAppConfig {

  val startDate = new LocalDate("2016-01-21")

  val employment = Employment(
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

  val allowance = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "EarlierYearsAdjustment",
    amount = BigDecimal(32.00))

  val taxAccount = TaxAccount(taxAccountId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    outstandingDebtRestriction = Some(200),
    underpaymentAmount = Some(300),
    actualPUPCodedInCYPlusOneTaxYear = Some(400))

  val employments = List(employment, employment.copy(employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae4")))
  val allowances = List(allowance)

  val statePension = StatePension(100, "test")

  val payAndTaxFixedUUID = Map(
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

  val payAndTaxRandomUUID = Map(
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

  trait HappyPathSetup extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {

      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(allowances)))))
      when(c.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier])).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(taxAccount)))))
      when(c.taxHistoryConnector.getStatePension(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(statePension)))))
      when(c.taxHistoryConnector.getAllPayAndTax(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(payAndTaxFixedUUID)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
      c
    }
  }

  trait NoEmployments extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {

      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(NOT_FOUND, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(allowances)))))
      when(c.taxHistoryConnector.getAllPayAndTax(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(payAndTaxFixedUUID)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
      c
    }
  }

  trait NoAllowances extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    lazy val controller: EmploymentSummaryController = {

      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, Some(Json.arr()))))
      when(c.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier])).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(taxAccount)))))
      when(c.taxHistoryConnector.getStatePension(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(statePension)))))
      when(c.taxHistoryConnector.getAllPayAndTax(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(payAndTaxFixedUUID)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
      c
    }
  }

  trait NullAllowances extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {

      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(NOT_FOUND, null)))
      when(c.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier])).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(taxAccount)))))
      when(c.taxHistoryConnector.getStatePension(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(statePension)))))
      when(c.taxHistoryConnector.getAllPayAndTax(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(payAndTaxFixedUUID)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
      c
    }
  }

  trait NoTaxAccount extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {

      val person = Some(Person(Some("first name"), Some("second name"), deceased = Some(false)))
      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(allowances)))))
      when(c.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, None)))
      when(c.taxHistoryConnector.getStatePension(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(statePension)))))
      when(c.taxHistoryConnector.getAllPayAndTax(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(payAndTaxFixedUUID)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
      c
    }
  }

  trait NoPayAndTax extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {

      val person = Some(Person(Some("first name"), Some("second name"), deceased = Some(false)))
      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(allowances)))))
      when(c.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier])).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(taxAccount)))))
      when(c.taxHistoryConnector.getStatePension(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(statePension)))))
      when(c.taxHistoryConnector.getAllPayAndTax(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(NOT_FOUND, Some(Json.toJson(payAndTaxFixedUUID)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any))
        .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
      c
    }
  }

  trait NoCitizenDetails extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {
      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(allowances)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(NOT_FOUND, None)))
      c
    }
  }

  trait LockedCitizenDetails extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {

      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(allowances)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(LOCKED, None)))
      c
    }
  }

  trait DeceasedCitizenDetails extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {

      val person = Some(Person(Some("James"), Some("Bond"), Some(true)))

      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
      c
    }
  }

  trait NonMatchingPayAndTax extends MockitoSugar {
    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    lazy val controller: EmploymentSummaryController = {

      val c = new EmploymentSummaryController(mock[TaxHistoryConnector], mock[CitizenDetailsConnector], mock[AuthConnector],
        injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)(stubControllerComponents().executionContext)

      when(c.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(allowances)))))
      when(c.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier])).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(taxAccount)))))
      when(c.taxHistoryConnector.getStatePension(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(statePension)))))
      when(c.taxHistoryConnector.getAllPayAndTax(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(payAndTaxRandomUUID)))))
      when(c.citizenDetailsConnector.getPersonDetails(any)(any)).
        thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
      c
    }
  }

  "GET /tax-history" should {
    val taxYear = 2016
    "return 200" in new HappyPathSetup {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
    }

    "return 200 when empty list of allowances found" in new NoAllowances {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
    }

    "return 200 when null allowances found" in new NullAllowances {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
    }

    "return 200 when no tax account found" in new NoTaxAccount {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
    }

    "return 200 when no pay and tax found" in new NoPayAndTax {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
      bodyOf(await(result)) should include(Messages("employmenthistory.employment.table.error.no-values"))
      bodyOf(await(result)) should include(Messages("employmenthistory.pension.table.error.no-values"))
    }

    "return 200 when pay and tax records do not match employment records" in new NonMatchingPayAndTax {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
    }

    "return 200 and show technical error page when no citizen details available" in new NoCitizenDetails {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }

    "return not found error page when citizen details returns locked status 423" in new LockedCitizenDetails {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getMciRestricted().url)
    }

    "return not found error page when citizen details returns deceased indicator" in new DeceasedCitizenDetails {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getDeceased().url)
    }

    "show not authorised error page when 401 returned from connector" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(UNAUTHORIZED,
          Some(Json.toJson("{Message:Unauthorised}")))))
      val result: Future[Result] = controller.getTaxHistory(taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNotAuthorised().url)
    }

    "show technical error page when any response other than 200, 401, 404 returned from connector" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getEmploymentsAndPensions(any, any)(any)).
        thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR,
          Some(Json.toJson("{Message:InternalServerError}")))))
      val result: Future[Result] = controller.getTaxHistory(taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }

    "show select client page when no nino has been set in session" in new HappyPathSetup {
      val result: Future[Result] = controller.getTaxHistory(taxYear)(fakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SelectClientController.getSelectClientPage().url)
    }

    "redirect to no data available page when no employments found" in new NoEmployments {
      val result = controller.getTaxHistory(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNoData(taxYear).url)
    }

  }

  "GET /tax-history/logout" should {
    "redirect to gg and clear session data" in new HappyPathSetup {
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