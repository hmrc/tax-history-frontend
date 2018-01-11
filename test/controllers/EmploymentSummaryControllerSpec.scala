/*
 * Copyright 2018 HM Revenue & Customs
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
import support.fixtures.PersonFixture
import model.api.{Allowance, Employment, EmploymentStatus, TaxAccount}
import models.taxhistory.Person
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.ControllerSpec
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class EmploymentSummaryControllerSpec extends ControllerSpec with PersonFixture{

  val startDate = new LocalDate("2016-01-21")

  val employment =  Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = LocalDate.parse("2016-01-21"),
    endDate = Some(LocalDate.parse("2017-01-01")),
    companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
    payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
    employmentStatus = EmploymentStatus.Live
  )

  val allowance = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "EarlierYearsAdjustment",
    amount = BigDecimal(32.00))

  val taxAccount = TaxAccount(taxAccountId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    outstandingDebtRestriction = Some(200),
    underpaymentAmount = Some(300),
    actualPUPCodedInCYPlusOneTaxYear = Some(400))

  val employments = List(employment)
  val allowances =  List(allowance)

  trait HappyPathSetup {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val c = injected[EmploymentSummaryController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(allowances)))))
      when(c.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier])).
        thenReturn(Future.successful(HttpResponse(Status.OK, Some(Json.toJson(taxAccount)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  trait NoEmployments {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val c = injected[EmploymentSummaryController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(allowances)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  trait NoAllowances {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val c = injected[EmploymentSummaryController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,Some(Json.arr()))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  trait NoTaxAccount {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val person = Some(Person(Some("first name"),Some("second name"), deceased = Some(false)))
      val c = injected[EmploymentSummaryController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(allowances)))))
      when(c.taxHistoryConnector.getTaxAccount(any[Nino], any[Int])(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,Some(Json.arr()))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any()))
        .thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  trait NoCitizenDetails {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {
      val c = injected[EmploymentSummaryController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(allowances)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,None)))
      c
    }
  }

  trait LockedCitizenDetails {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val c = injected[EmploymentSummaryController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getEmploymentsAndPensions(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employments)))))
      when(c.taxHistoryConnector.getAllowances(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(allowances)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.LOCKED,None)))
      c
    }
  }

  trait DeceasedCitizenDetails {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val person = Some(Person(Some("James"),Some("Bond"),Some(true)))

      val c = injected[EmploymentSummaryController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  "GET /tax-history" should {
    val taxYear = 2016
    "return 200" in new HappyPathSetup {
      val result = controller.getTaxHistory(taxYear).apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
    }
    "return 200 when no allowances found" in new NoAllowances {
      val result = controller.getTaxHistory(taxYear).apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
    }

    "return 200 when no tax account found" in new NoTaxAccount {
      val result = controller.getTaxHistory(taxYear).apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.title"))
    }

    "return 200 and show technical error page when no citizen details available" in new NoCitizenDetails {
      val result = controller.getTaxHistory(taxYear).apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }

    "return not found error page when citizen details returns locked status 423" in new LockedCitizenDetails {
      val result = controller.getTaxHistory(taxYear).apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation (result) shouldBe Some(controllers.routes.ClientErrorController.getMciRestricted().url)
    }

    "return not found error page when citizen details returns deceased indicator" in new DeceasedCitizenDetails {
      val result = controller.getTaxHistory(taxYear).apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getDeceased().url)
    }

    "show not authorised error page when 401 returned from connector" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getEmploymentsAndPensions(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.UNAUTHORIZED,
        Some(Json.toJson("{Message:Unauthorised}")))))
      val result = controller.getTaxHistory(taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation (result) shouldBe Some(controllers.routes.ClientErrorController.getNotAuthorised().url)
    }

    "show technical error page when any response other than 200, 401, 404 returned from connector" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getEmploymentsAndPensions(any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR,
        Some(Json.toJson("{Message:InternalServerError}")))))
      val result = controller.getTaxHistory(taxYear)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation (result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }

    "show select client page when no nino has been set in session" in new HappyPathSetup {
      val result = controller.getTaxHistory(taxYear)(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation (result) shouldBe Some(controllers.routes.SelectClientController.getSelectClientPage().url)
    }

    "redirect to no data available page when no employments found" in new NoEmployments {
      val result = controller.getTaxHistory(taxYear).apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNoData().url)
    }

  }

  "GET /tax-history/logout" should {
    "redirect to gg and clear session data" in new HappyPathSetup{
      val result = controller.logout()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
    }
  }
}
