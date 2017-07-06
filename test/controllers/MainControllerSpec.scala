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
import config.{ConfigDecorator, FrontendAuthConnector}
import connectors.TaxHistoryConnector
import controllers.auth.AgentAuth
import org.mockito.Matchers.{eq ⇒ meq, _}
import org.mockito.Mockito.{times, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mock.MockitoSugar
import play.api.{Application, Configuration, Environment}
import play.api.http.Status
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.{BadGatewayException, SessionKeys}
import form.SelectClientForm.selectClientForm

import scala.concurrent.Future
class MainControllerSpec extends BaseSpec with MockitoSugar with Fixtures {

  val mockConnector = mock[TaxHistoryConnector]
  val mockPlayAuthConnector= mock[PlayAuthConnector]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance(mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[TaxHistoryConnector].toInstance(mockConnector))
    .build()
  val agentEnrolment = Set(
    Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", validTestARN)), confidenceLevel = ConfidenceLevel.L200,
      state = "", delegatedAuthRule = Some("afi-auth"))
  )
   val testAgentAuth = new AgentAuth {
     override def authConnector: AuthConnector = mockPlayAuthConnector

     override def config: Configuration = ???

     override def env: Environment = ???
   }
  def authorisedForAfiMock(): OngoingStubbing[Future[~[Option[AffinityGroup], Enrolments]]] = {
    println(" \n\n I am not being called \n\n")
    when(mockPlayAuthConnector.authorise(any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any()))
      .thenReturn(Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(agentEnrolment))))
  }
  val validTestARN = "TARN0000001"
  val validTestNINO = "AB123456B"
  val invalidTestNINO = "9999999999999999"
  val invalidTestARN = "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ"
  val payeService = "PAYE"
  val validSelectClientForm = Seq(
    "clientId" -> validTestNINO
  )

  val invalidSelectClientForm = Seq(
    "clientId" -> invalidTestNINO
  )

  trait LocalSetup {

    lazy val authority = buildFakeAuthority(true)

    val fakeRequest = FakeRequest("GET", "/").withSession(
      SessionKeys.sessionId -> "SessionId",
      SessionKeys.token -> "Token",
      SessionKeys.userId -> "/auth/oid/tuser",
      SessionKeys.authToken -> ""
    )

    lazy val controller = {
      val c = injected[MainController]
      when(c.authConnector.authorise(any(), meq(EmptyRetrieval))(any())).thenReturn(Future.successful())
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(Nil))
      c
    }
  }
  "GET /tax-history-frontend" should {

    "return 200" in new LocalSetup {
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.OK
    }

    "return error page when connector not available" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.failed(new BadGatewayException("")))
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include("Tax History Connector not available")
    }

    "redirect to gg when not logged in" in new LocalSetup {

      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.failed(new MissingBearerToken))
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.SEE_OTHER
    }
    "return Status: 303 and redirect to Demo Employment History page when there is valid input" in  new LocalSetup{
        authorisedForAfiMock()

          val result = controller.submitSelectClientPage().apply(FakeRequest()
            .withFormUrlEncodedBody(validSelectClientForm: _*))

          status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result).get shouldBe "http://localhost:9996/tax-history"
      }

      "return Status: 400 when invalid data is input" in  new LocalSetup{
        authorisedForAfiMock()

          val result = controller.submitSelectClientPage().apply(FakeRequest()
            .withFormUrlEncodedBody(invalidSelectClientForm: _*))

        status(result) shouldBe  Status.BAD_REQUEST
        //verify(mockRelationshipConnector, times(0)).hasRelationship(any(), any(), any())(any())
      }
  }
}
