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

import config.{ConfigDecorator, FrontendAuthConnector}
import controllers.auth.LocalRegime
import models.TaxSummary
import org.joda.time.DateTime
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.Application
import play.api.http.Status
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{TaiService, TaxSummarySuccessResponse}
import support.Fixtures
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class MainControllerSpec extends BaseSpec with MockitoSugar with Fixtures {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance(mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[TaiService].toInstance(mock[TaiService]))
    .build()

  trait LocalSetup {

    val fakeTaiService = mock[TaiService]
    lazy val authority = buildFakeAuthority(true)

    val fakeRequest = FakeRequest("GET", "/").withSession(
      SessionKeys.sessionId -> "SessionId",
      SessionKeys.token -> "Token",
      SessionKeys.userId -> "/auth/oid/tuser",
      SessionKeys.authToken -> ""
    )

    lazy val controller = {
      val c = injected[MainController]

      when(c.taiService.taxSummary(any(), any())(any())).thenReturn(Future.successful(TaxSummarySuccessResponse(TaxSummary(Json.obj()))))
      when(c.authConnector.currentAuthority(org.mockito.Matchers.any())) thenReturn {
        Future.successful(Some(authority))
      }
      c
    }
  }

  "GET /tax-history-frontend" should {

    "return 200" in new LocalSetup {
      val result = controller.index(fakeRequest)
      println(contentAsString(result))
      status(result) shouldBe Status.OK
    }

  }

}
