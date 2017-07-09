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
import org.mockito.Matchers.{eq ⇒ meq, _}
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.Application
import play.api.http.Status
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.{BadGatewayException, HttpResponse, SessionKeys}
import akka.actor.ActorSystem
import models.taxhistory.Employment
import org.mockito.Matchers
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json

import scala.concurrent.Future

class MainControllerSpec extends BaseSpec with MockitoSugar with Fixtures {

  private val mockConnector = mock[TaxHistoryConnector]
  private val mockFrontendAuthConnector = mock[FrontendAuthConnector]

  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(FakeRequest())

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance( mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[TaxHistoryConnector].toInstance(mockConnector))
    .build()


  val validTestNINO = "AB123456B"
  val invalidTestNINO = "9999999999999999"


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
      val employment = Employment(payeReference = "ABC", employerName = "Fred West Shoes", taxTotal = Some(BigDecimal.valueOf(123.12)), taxablePayTotal = Some(BigDecimal.valueOf(45.32)))
      val c = injected[MainController]
      when(c.authConnector.authorise(any(), meq(EmptyRetrieval))(any())).thenReturn(Future.successful())
      when(c.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(employment))))))
      c
    }
  }

  "GET /tax-history-frontend" should {

    "return 200" in new LocalSetup {
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.OK
    }

    "show not found error page when 404 returned from connector" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,Some(Json.toJson("[]")))))
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.notfound.message").toString)
    }

    "show not authorised error page when 401 returned from connector" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.UNAUTHORIZED,Some(Json.toJson("{Message:Unauthorised}")))))
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.unauthorised.message"))
    }

    "show technical error page when any response other than 200, 401, 404 returned from connector" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR,Some(Json.toJson("{Message:InternalServerError}")))))
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.technicalerror.message"))
    }

    "show technical error page when no nino has been set in session" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      //when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.successful(HttpResponse(Status.INTERNAL_SERVER_ERROR,Some(Json.toJson("{Message:InternalServerError}")))))
      val result = controller.get()(fakeRequest)
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("mtdfi.select_client.title"))
    }

    "return error page when connector not available" in new LocalSetup {
      implicit val actorSystem = ActorSystem("test")
      implicit val materializer = ActorMaterializer()
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.failed(new BadGatewayException("")))
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.OK
      bodyOf(await(result)) should include(Messages("employmenthistory.technicalerror.message"))
    }

    "redirect to gg when not logged in" in new LocalSetup {
      when(controller.taxHistoryConnector.getTaxHistory(any(), any())(any())).thenReturn(Future.failed(new MissingBearerToken))
      val result = controller.get()(fakeRequest.withSession("USER_NINO" -> "AA000003A"))
      status(result) shouldBe Status.SEE_OTHER
    }

    "return Status: 400 when invalid data is input" in new LocalSetup {
      val result = controller.submitSelectClientPage().apply(FakeRequest()
        .withFormUrlEncodedBody(invalidSelectClientForm: _*))

      status(result) shouldBe Status.BAD_REQUEST
    }
  }
}
