/*
 * Copyright 2025 HM Revenue & Customs
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
import models.taxhistory.Person
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.JsValue
import play.api.mvc.{MessagesControllerComponents, Result, Results}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BaseControllerSpec extends ControllerSpec with BaseSpec with ScalaFutures {

  trait TestSetup {

    lazy val controller: Controller = {
      val controller = new Controller(
        mock(classOf[AuthConnector]),
        injected[Configuration],
        injected[Environment],
        messagesControllerComponents,
        appConfig
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

      controller
    }
  }

  "BaseController" must {

    "redirect to agent-subscription-start when there is no enrolment" in new TestSetup {
      when(
        controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(
          Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(Set())))
        )

      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(
        hc,
        fakeRequest.withSession("USER_NINO" -> nino)
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controller.appConfig.agentSubscriptionStart)
    }

    "redirect to not no-agent-services-account when there is no enrolment and is not an agent" in new TestSetup {
      when(
        controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(
          Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Individual), Enrolments(Set())))
        )

      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(
        hc,
        fakeRequest.withSession("USER_NINO" -> nino)
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ClientErrorController.getNoAgentServicesAccountPage().url
      )
    }

    "redirect to not no-agent-services-account when there is no enrolment and has no affinity group" in new TestSetup {
      when(
        controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(
          Future.successful(new ~[Option[AffinityGroup], Enrolments](None, Enrolments(Set())))
        )

      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(
        hc,
        fakeRequest.withSession("USER_NINO" -> nino)
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(
        controllers.routes.ClientErrorController.getNoAgentServicesAccountPage().url
      )
    }

    "load error page when failed to fetch enrolment" in new TestSetup {
      when(
        controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(Future.failed(new BadGatewayException("error")))

      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(
        hc,
        fakeRequest.withSession("USER_NINO" -> nino)
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }

    "redirect to gg when not logged in" in new TestSetup {
      when(
        controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(Future.failed(new MissingBearerToken))

      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(
        hc,
        fakeRequest.withSession("USER_NINO" -> nino)
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get should include("/bas-gateway/sign-in")
    }

    "redirect to gg logged in when session expired" in new TestSetup {
      when(
        controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(Future.failed(new SessionRecordNotFound))

      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(
        hc,
        fakeRequest.withSession("USER_NINO" -> nino)
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get should include("/bas-gateway/sign-in")
    }

    "redirect to gg logged in when Internal server error or other errors" in new TestSetup {
      when(
        controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(Future.failed(new RuntimeException))

      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(
        hc,
        fakeRequest.withSession("USER_NINO" -> nino)
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result).get should include("/bas-gateway/sign-in")
    }

    "redirect to not authorised page when user is not authorised" in new TestSetup {
      when(
        controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      )
        .thenReturn(Future.failed(new InsufficientEnrolments))

      val result: Future[Result] = controller.authorisedForAgent(_ => Future.successful(Results.Ok("test")))(
        hc,
        fakeRequest.withSession("USER_NINO" -> nino)
      )
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNotAuthorised().url)
    }
  }

  "show not found error page when 404 returned from connector" in new TestSetup {
    val taxYear                = 2017
    val result: Future[Result] = Future(controller.handleHttpFailureResponse(NOT_FOUND, Some(taxYear)))
    status(result)           shouldBe SEE_OTHER
    redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNoData(taxYear).url)
  }

  "show not authorised error page when 401 returned from connector" in new TestSetup {
    val result: Future[Result] = Future(controller.handleHttpFailureResponse(UNAUTHORIZED))
    status(result)           shouldBe SEE_OTHER
    redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getNotAuthorised().url)
  }

  "show technical error page" when {
    "404 with no taxYear is returned from connector" in new TestSetup {
      val result: Future[Result] = Future(controller.handleHttpFailureResponse(NOT_FOUND, None))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }

    "any response other than 200, 401, 404 returned from connector" in new TestSetup {
      val result: Future[Result] = Future(controller.handleHttpFailureResponse(INTERNAL_SERVER_ERROR))
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
    }
  }

  "redirect to MciRestricted" when {
    "status from citizen detail service is Locked" in new TestSetup {
      val result: Future[Result] = controller.redirectToClientErrorPage(LOCKED)
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getMciRestricted().url)
    }
  }

  "redirect to Deceased" when {
    "status from citizen detail service is GONE" in new TestSetup {
      val result: Future[Result] = controller.redirectToClientErrorPage(GONE)
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getDeceased().url)
    }
  }

  "redirect to Technical error page" when {
    "status from citizen detail service is anything other then LOCKED and GONE" in new TestSetup {
      val result: Future[Result] = controller.redirectToClientErrorPage(2)
      redirectLocation(result) shouldBe Some(routes.ClientErrorController.getTechnicalError().url)
    }
  }

  "retrieveCitizenDetails" should {

    "return Left(GONE) when the deceased flag is true" in new TestSetup {
      val json: JsValue    = loadFile("/json/model/api/personDeceasedTrue.json")
      val hr: HttpResponse = HttpResponse(OK, json = json, Map.empty)
      await(controller.retrieveCitizenDetails(Future(hr))) shouldBe Left(GONE)
    }

    "return Right(person) when the deceased flag is false" in new TestSetup {
      val json: JsValue    = loadFile("/json/model/api/personDeceasedFalse.json")
      val hr: HttpResponse = HttpResponse(OK, json = json, Map.empty)
      val person: Person   = Person(Some("first name"), Some("second name"), Some(false))
      await(controller.retrieveCitizenDetails(Future(hr))) shouldBe Right(person)
    }

    "return Right(person) when the deceased flag is not given" in new TestSetup {
      val json: JsValue    = loadFile("/json/model/api/personDeceasedNoValue.json")
      val hr: HttpResponse = HttpResponse(OK, json = json, Map.empty)
      val person: Person   = Person(Some("first name"), Some("second name"), None)
      await(controller.retrieveCitizenDetails(Future(hr))) shouldBe Right(person)
    }

    "return Left(BAD_REQUEST) when there is an exception" in new TestSetup {
      await(controller.retrieveCitizenDetails(Future.failed(new Exception))) shouldBe Left(BAD_REQUEST)
    }
  }

  class Controller @Inject() (
    override val authConnector: AuthConnector,
    override val config: Configuration,
    override val env: Environment,
    val cc: MessagesControllerComponents,
    implicit val appConfig: AppConfig
  ) extends BaseController(cc) {
    val loginContinue: String          = appConfig.loginContinue
    val agentSubscriptionStart: String = appConfig.agentSubscriptionStart
  }
}
