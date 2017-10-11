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

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.FrontendAppConfig
import model.api.Employment
import models.taxhistory.Person
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{BadGatewayException, HttpResponse}

import scala.concurrent.Future

class MainControllerSpec extends BaseControllerSpec  {

  val startDate = new LocalDate("2016-01-21")

  val employment =  Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = LocalDate.parse("2016-01-21"),
    endDate = Some(LocalDate.parse("2017-01-01"))
  )

  val employments = List(employment)

  trait LocalSetup {

    lazy val controller = {

      val person = Some(Person(Some("first name"),Some("second name"), false))
      val c = injected[MainController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employments)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  trait NoCitizenDetails {

    lazy val controller = {
      val c = injected[MainController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employments)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,None)))
      c
    }
  }

  trait LockedCitizenDetails {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val c = injected[MainController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employments)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.LOCKED,None)))
      c
    }
  }

  trait DeceasedCitizenDetails {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val person = Some(Person(Some("James"),Some("Bond"),true))

      val c = injected[MainController]

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.LOCKED,Some(Json.toJson(employments)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
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

  }

  "GET /tax-history/logout" should {
    "redirect to gg and clear session data" in new LocalSetup{
      val result = controller.logout()(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
    }
  }
}
