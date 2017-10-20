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

import javax.inject.Inject

import config.FrontendAppConfig.getString
import config.{ConfigDecorator, FrontendAuthConnector}
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.{Application, Configuration, Environment}
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadGatewayException, HttpResponse, SessionKeys}
import utils.TestUtil

import scala.concurrent.Future

class BaseControllerSpec extends BaseSpec with Fixtures with TestUtil {

  lazy val nino = randomNino.toString()

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance( mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[TaxHistoryConnector].toInstance(mock[TaxHistoryConnector]))
    .overrides(bind[CitizenDetailsConnector].toInstance(mock[CitizenDetailsConnector]))
    .build()

  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(FakeRequest())

  lazy val fakeRequest = FakeRequest("GET", "/").withSession(
    SessionKeys.sessionId -> "SessionId",
    SessionKeys.token -> "Token",
    SessionKeys.userId -> "/auth/oid/tuser",
    SessionKeys.authToken -> ""
  )

  lazy val newEnrolments = Set(
    Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "TestArn")),
      state="",delegatedAuthRule = None)
  )

  lazy val authority = buildFakeAuthority(true)

  trait HappyPathSetup {

    lazy val controller = {
      val c = injected[Controller]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      c
    }
  }


  trait NoEnrolmentsSetup {

    lazy val controller = {
      val c = injected[Controller]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(Set()))))
      c
    }
  }

  trait NoEnrolmentsAndNotAnAgentSetup {

    lazy val controller = {
      val c = injected[Controller]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Individual) , Enrolments(Set()))))
      c
    }
  }

  trait NoEnrolmentsAndNoAffinityGroupSetup {

    lazy val controller = {
      val c = injected[Controller]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Individual) , Enrolments(Set()))))
      c
    }
  }

  trait failureOnRetrievalOfEnrolment {

    lazy val controller = {
      val c = injected[Controller]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.failed(new BadGatewayException("error")))
      c
    }
  }

  trait failureOnMissingBearerToken {

    lazy val controller = {
      val c = injected[Controller]
      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.failed(new MissingBearerToken))
      c
    }
  }

  "BaseController" must {

    "redirect to afi-not-an-agent-page when there is no enrolment" in new NoEnrolmentsSetup {
      val result = controller.authorisedForAgent(Future.successful(Results.Ok("test")))(hc, fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(getString("external-url.afi-not-an-agent-page.url"))
    }

    "redirect to afi-not-an-agent-page when there is no enrolment and is not an agent" in new NoEnrolmentsAndNotAnAgentSetup {
      val result = controller.authorisedForAgent(Future.successful(Results.Ok("test")))(hc, fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(getString("external-url.afi-not-an-agent-page.url"))
    }

    "redirect to afi-not-an-agent-page when there is no enrolment and has no affinity group" in new NoEnrolmentsAndNoAffinityGroupSetup {
      val result = controller.authorisedForAgent(Future.successful(Results.Ok("test")))(hc, fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(getString("external-url.afi-not-an-agent-page.url"))
    }

    "load error page when failed to fetch enrolment" in new failureOnRetrievalOfEnrolment {
      val result = controller.authorisedForAgent(Future.successful(Results.Ok("test")))(hc, fakeRequest)
      status(result) shouldBe Status.OK
      contentAsString(result) should include(Messages("employmenthistory.technicalerror.title"))
    }


    "redirect to gg when not logged in" in new failureOnMissingBearerToken {
      val result = controller.authorisedForAgent(Future.successful(Results.Ok("test")))(hc, fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      await(result.header.headers.get("Location")).get should include("/gg/sign-in")
    }
  }

  "show not found error page when 404 returned from connector" in new HappyPathSetup {

    val result = controller.handleHttpFailureResponse(Status.NOT_FOUND, Nino(nino))(fakeRequest.withSession("USER_NINO" -> nino))
    status(result) shouldBe Status.OK
    contentAsString(await(result)) should include(Messages("employmenthistory.notfound.header", nino).toString)
  }

  "show not authorised error page when 401 returned from connector" in new HappyPathSetup {
    val result = controller.handleHttpFailureResponse(Status.UNAUTHORIZED, Nino(nino))(fakeRequest.withSession("USER_NINO" -> nino))
    status(result) shouldBe Status.OK
    contentAsString(result) should include(Messages("employmenthistory.unauthorised.header", nino))
  }

  "show technical error page when any response other than 200, 401, 404 returned from connector" in new HappyPathSetup {
    val result = controller.handleHttpFailureResponse(Status.INTERNAL_SERVER_ERROR, Nino(nino))(fakeRequest.withSession("USER_NINO" -> nino))
    status(result) shouldBe Status.OK
    contentAsString(result) should include(Messages("employmenthistory.technicalerror.header"))
  }
}

class Controller  @Inject()(
                             override val authConnector: FrontendAuthConnector,
                             override val config: Configuration,
                             override val env: Environment,
                             implicit val messagesApi: MessagesApi
                           ) extends BaseController
