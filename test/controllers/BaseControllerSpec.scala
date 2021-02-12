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

import config.AppConfig
import javax.inject.Inject
import models.taxhistory.Person
import org.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.mvc.{MessagesControllerComponents, Result, Results}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import support.ControllerSpec
import support.fixtures.PersonFixture
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadGatewayException, HttpResponse}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.TestAppConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BaseControllerSpec extends ControllerSpec with TestAppConfig {

  trait HappyPathSetup extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment],
        injected[MessagesControllerComponents], appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
      controller
    }
  }

  trait NoEnrolmentsSetup extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(Set()))))
      controller
    }
  }

  trait NoEnrolmentsAndNotAnAgentSetup extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment], stubMessagesControllerComponents(), appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Individual), Enrolments(Set()))))
      controller
    }
  }

  trait NoEnrolmentsAndNoAffinityGroupSetup extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Individual), Enrolments(Set()))))
      controller
    }
  }

  trait failureOnRetrievalOfEnrolment extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.failed(new BadGatewayException("error")))
      controller
    }
  }

  trait failureOnMissingBearerToken extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.failed(new MissingBearerToken))
      controller
    }
  }

  trait failureOnAuthorisation extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.failed(new SessionRecordNotFound))
      controller
    }
  }

  trait failureOnOtherException extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.failed(new RuntimeException))
      controller
    }
  }

  trait failureInsufficientEnrolments extends MockitoSugar {

    lazy val controller: Controller = {
      val controller = new Controller(mock[AuthConnector], injected[Configuration], injected[Environment], injected[MessagesControllerComponents], appConfig)
      when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
        Future.failed(new InsufficientEnrolments))
      controller
    }
  }

  "BaseController" must {

    "redirect to agent-subscription-start when there is no enrolment" in new NoEnrolmentsSetup {
      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(hc,
        fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controller.appConfig.agentSubscriptionStart)
    }

    "redirect to not no-agent-services-account when there is no enrolment and is not an agent" in new NoEnrolmentsAndNotAnAgentSetup {
      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(hc,
        fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNoAgentServicesAccountPage().url)
    }

    "redirect to not no-agent-services-account when there is no enrolment and has no affinity group" in new NoEnrolmentsAndNoAffinityGroupSetup {
      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(hc,
        fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNoAgentServicesAccountPage().url)
    }

    "load error page when failed to fetch enrolment" in new failureOnRetrievalOfEnrolment {
      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(hc,
        fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }


    "redirect to gg when not logged in" in new failureOnMissingBearerToken {
      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(hc,
        fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      await(result.header.headers.get("Location")).get should include("/gg/sign-in")
    }

    "redirect to gg logged in when session expired" in new failureOnAuthorisation  {
      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(hc,
        fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      await(result.header.headers.get("Location")).get should include("/gg/sign-in")
    }

    "redirect to gg logged in when Internal server error or other errors" in new failureOnOtherException  {
      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(hc,
        fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      await(result.header.headers.get("Location")).get should include("/gg/sign-in")
    }

    "redirect to not authorised page when user is not authorised" in new failureInsufficientEnrolments {
      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(hc,
        fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNotAuthorised().url)
    }
  }

  "show not found error page when 404 returned from connector" in new HappyPathSetup {

    val result: Result = controller.handleHttpFailureResponse(Status.NOT_FOUND, Nino(nino),Some(2017))
    status(result) shouldBe Status.SEE_OTHER
    redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNoData(2017).url)
  }

  "show not authorised error page when 401 returned from connector" in new HappyPathSetup {
    val result: Result = controller.handleHttpFailureResponse(Status.UNAUTHORIZED, Nino(nino))
    status(result) shouldBe Status.SEE_OTHER
    redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNotAuthorised().url)
  }

  "show technical error page when any response other than 200, 401, 404 returned from connector" in new HappyPathSetup {
    val result: Result = controller.handleHttpFailureResponse(Status.INTERNAL_SERVER_ERROR, Nino(nino))
    status(result) shouldBe Status.SEE_OTHER
    redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
  }

  "Sign out" must {
    "redirect to feed back survey link" in new HappyPathSetup {
      val result: Future[Result] = controller.logout()(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controller.appConfig.serviceSignOut)
    }

  }


  "redirect to MciRestricted" when {
    "status from citizen detail service is Locked" in new HappyPathSetup {
      val result: Future[Result] = controller.redirectToClientErrorPage(LOCKED)
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getMciRestricted().url)
    }
  }

  "redirect to Deceased" when {
    "status from citizen detail service is GONE" in new HappyPathSetup {
      val result: Future[Result] = controller.redirectToClientErrorPage(GONE)
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getDeceased().url)
    }
  }

  "redirect to Technical error page" when {
    "status from citizen detail service is anything other then LOCKED and GONE" in new HappyPathSetup {
      val result: Future[Result] = controller.redirectToClientErrorPage(2)
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }
  }

  "retrieveCitizenDetails" when {

    "The deceased flag is true" in new HappyPathSetup {

      val json: JsValue = loadFile("/json/model/api/personDeceasedTrue.json")
      val hr = HttpResponse(OK, Some(json), Map.empty, None)
      await(controller.retrieveCitizenDetails(randomNino, Future(hr))) shouldBe Left(GONE)
    }

    "The deceased flag is false" in new HappyPathSetup with PersonFixture {

      val json: JsValue = loadFile("/json/model/api/personDeceasedFalse.json")
      val hr = HttpResponse(OK, Some(json), Map.empty, None)
      await(controller.retrieveCitizenDetails(randomNino, Future(hr))) shouldBe Right(person.get)
    }

    "The deceased flag is not given" in new HappyPathSetup {

      val json: JsValue = loadFile("/json/model/api/personDeceasedNoValue.json")
      val hr = HttpResponse(OK, Some(json), Map.empty, None)
      val person = Person(Some("first name"), Some("second name"), None)
      await(controller.retrieveCitizenDetails(randomNino, Future(hr))) shouldBe Right(person)
    }
  }
}

class Controller @Inject()(override val authConnector: AuthConnector,
                           override val config: Configuration,
                           override val env: Environment,
                           val cc: MessagesControllerComponents,
                           implicit val appConfig: AppConfig
                          ) extends BaseController(cc) {
  val loginContinue: String = appConfig.loginContinue
  val serviceSignout: String = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart
}
