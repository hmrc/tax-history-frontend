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

package connectors

import config.{ConfigDecorator, FrontendAuthConnector, WSHttpT}
import models.taxhistory.Employment
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.i18n.MessagesApi
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.{HttpGet, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.Future

class TaxHistoryConnectorSpec extends BaseSpec with MockitoSugar with Fixtures {

  val http = mock[WSHttpT]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance(mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[WSHttpT].toInstance(http))
    .build()

  trait LocalSetup {

    lazy val connector = {
      injected[TaxHistoryConnector]
    }
  }

  "TaxHistoryConnector" should {

    "fetch tax history" in new LocalSetup {
      when(connector.httpGet.GET[HttpResponse](any())(any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(Employment("AA12341234", "Test Employer Name", "21/01/2016", None, Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0))))))))

      val result = await(connector.getTaxHistory(Nino("AA000000A"), 2017))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Seq(Employment("AA12341234", "Test Employer Name", "21/01/2016", None, Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0))))

    }
  }

}
