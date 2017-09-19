/*
 * Copyright 2017 HM Revenue & Customs
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
import akka.stream.ActorMaterializer
import config.{ConfigDecorator, FrontendAppConfig, FrontendAuthConnector}
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import models.taxhistory.{Allowance, Employment, PayAsYouEarnDetails, Person}
import org.mockito.Matchers
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.Application
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import org.joda.time.LocalDate
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.{BadGatewayException, HttpResponse, SessionKeys}
import utils.TestUtil

import scala.concurrent.Future

class MainControllerSpec extends BaseSpec with MockitoSugar with Fixtures with TestUtil{

  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(FakeRequest())

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance( mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[TaxHistoryConnector].toInstance(mock[TaxHistoryConnector]))
    .overrides(bind[CitizenDetailsConnector].toInstance(mock[CitizenDetailsConnector]))
    .build()

  val invalidTestNINO = "9999999999999999"
  val startDate = new LocalDate("2016-01-21")
  lazy val nino =randomNino.toString()

  val invalidSelectClientForm = Seq(
    "clientId" -> invalidTestNINO
  )

  lazy val authority = buildFakeAuthority(true)

  lazy val newEnrolments = Set(
    Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "TestArn")), confidenceLevel = ConfidenceLevel.L200,
      state="",delegatedAuthRule = None)
  )

  lazy val fakeRequest = FakeRequest("GET", "/").withSession(
    SessionKeys.sessionId -> "SessionId",
    SessionKeys.token -> "Token",
    SessionKeys.userId -> "/auth/oid/tuser",
    SessionKeys.authToken -> ""
  )

  trait LocalSetup {

    lazy val controller = {
      val employment = Employment(payeReference = "ABC", employerName = "Fred West Shoes", taxTotal = Some(BigDecimal.valueOf(123.12)), taxablePayTotal = Some(BigDecimal.valueOf(45.32)), startDate = startDate, endDate = None)

      val paye = PayAsYouEarnDetails(List(employment), List(Allowance("desc", 222.00, "ProfessionalSubscriptions"),Allowance("desc1", 333.00,"FlatRateJobExpenses")))
      val person = Some(Person(Some("Barry"),Some("Evans"), false))
      val c = injected[MainController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(paye)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  trait NoCitizenDetails {

    lazy val controller = {
      val employment = Employment(payeReference = "ABC", employerName = "Fred West Shoes", taxTotal = Some(BigDecimal.valueOf(123.12)), taxablePayTotal = Some(BigDecimal.valueOf(45.32)), startDate = startDate, endDate = None)
      val paye = PayAsYouEarnDetails(List(employment), List(Allowance("desc", 222.00, "FlatRateJobExpenses"),Allowance("desc1", 333.00, "ProfessionalSubscriptions")))
      val c = injected[MainController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(paye)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,None)))
      c
    }
  }

  trait LockedCitizenDetails {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {
      val employment = Employment(payeReference = "ABC", employerName = "Fred West Shoes", taxTotal = Some(BigDecimal.valueOf(123.12)), taxablePayTotal = Some(BigDecimal.valueOf(45.32)), startDate = startDate, endDate = None)
      val paye = PayAsYouEarnDetails(List(employment), List(Allowance("desc", 222.00, "FlatRateJobExpenses"),Allowance("desc1", 333.00 ,"ProfessionalSubscriptions")))
      val c = injected[MainController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(paye)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.LOCKED,None)))
      c
    }
  }

  trait DeceasedCitizenDetails {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {
      val employment = Employment(payeReference = "ABC", employerName = "Fred West Shoes", taxTotal = Some(BigDecimal.valueOf(123.12)), taxablePayTotal = Some(BigDecimal.valueOf(45.32)), startDate = startDate, endDate = None)
      val person = Some(Person(Some("James"),Some("Bond"),true))
      val paye = PayAsYouEarnDetails(List(employment), List(Allowance("desc", 222.00, "FlatRateJobExpenses"),Allowance("desc1", 333.00, "ProfessionalSubscriptions")))
      val c = injected[MainController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.LOCKED,Some(Json.toJson(paye)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  trait NotAgentSetup {

    lazy val controller = {
      val employment = Employment(payeReference = "ABC", employerName = "Fred West Shoes", taxTotal = Some(BigDecimal.valueOf(123.12)), taxablePayTotal = Some(BigDecimal.valueOf(45.32)), startDate = startDate, endDate = None)
      val paye = PayAsYouEarnDetails(List(employment), List(Allowance("desc", 222.00, "FlatRateJobExpenses"),Allowance("desc1", 333.00, "ProfessionalSubscriptions")))

      val c = injected[MainController]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Individual) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(paye)))))
      c
    }
  }

  trait NoEnrolmentsSetup {

    lazy val controller = {
      val employment = Employment(payeReference = "ABC", employerName = "Fred West Shoes", taxTotal = Some(BigDecimal.valueOf(123.12)), taxablePayTotal = Some(BigDecimal.valueOf(45.32)), startDate = startDate, endDate = None)
      val paye = PayAsYouEarnDetails(List(employment), List(Allowance("desc", 222.00, "FlatRateJobExpenses"),Allowance("desc1", 333.00, "ProfessionalSubscriptions")))

      val c = injected[MainController]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(Set()))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(paye)))))
      c
    }
  }

  "GET /tax-history" should {
    "return 200" in new LocalSetup {
      val result = controller.getTaxHistory().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
    }

    "return 200 even when no citizen details available" in new NoCitizenDetails {
      val result = controller.getTaxHistory().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
    }

    "return not found error page when citizen details returns locked status 423" in new LockedCitizenDetails {
      val result = controller.getTaxHistory().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.notfound.header", nino).toString)
    }

    "return not found error page when citizen details returns deceased indicator" in new DeceasedCitizenDetails {
      val result = controller.getTaxHistory().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.notfound.header", nino).toString)
    }

    "show not found error page when 404 returned from connector" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,Some(Json.toJson("[]")))))
      val result = controller.getTaxHistory()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.notfound.header", nino).toString)
    }

    "show not authorised error page when 401 returned from connector" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.UNAUTHORIZED,Some(Json.toJson("{Message:Unauthorised}")))))
      val result = controller.getTaxHistory()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.unauthorised.header", nino))
    }

    "show technical error page when any response other than 200, 401, 404 returned from connector" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR,Some(Json.toJson("{Message:InternalServerError}")))))
      val result = controller.getTaxHistory()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.technicalerror.header"))
    }

    "show technical error page when no nino has been set in session" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      val result = controller.getTaxHistory()(fakeRequest)
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.select.client.title"))
    }

    "return error page when connector not available" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.failed(new BadGatewayException("")))
      val result = controller.getTaxHistory()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.technicalerror.header"))
    }

    "redirect to gg when not logged in" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.failed(new MissingBearerToken))
      val result = controller.getTaxHistory()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      await(result.header.headers.get("Location")).get should include("/gg/sign-in")
    }

    "return Status: 400 when invalid data is input" in new LocalSetup {
      val result = controller.submitSelectClientPage().apply(FakeRequest()
        .withFormUrlEncodedBody(invalidSelectClientForm: _*))
      status(result) shouldBe Status.BAD_REQUEST
    }

    "redirect to when agent has no enrolments" in new NoEnrolmentsSetup {
      val result = controller.getTaxHistory()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      await(result.header.headers.get("Location")) shouldBe Some(FrontendAppConfig.AfiNoAgentServicesAccountPage)
    }

    "redirect when user is not an agent" in new NotAgentSetup {
      val result = controller.getTaxHistory()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      await(result.header.headers.get("Location")) shouldBe Some(FrontendAppConfig.AfiNoAgentServicesAccountPage)
    }
  }

  "GET /tax-history/logout" should {
    "redirect to gg and clear session data" in new LocalSetup{
      val result = controller.logout()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
    }
  }
}
