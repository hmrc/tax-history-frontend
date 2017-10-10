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
import models.taxhistory.Person
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.domain.Nino
import utils.TestUtil

import scala.concurrent.Future
import uk.gov.hmrc.http.HttpResponse

class CitizenDetailsConnectorSpec extends BaseSpec with Fixtures with TestUtil{

  val http = mock[WSHttpT]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance(mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[WSHttpT].toInstance(http))
    .build()

  val nino =randomNino.toString()

  trait LocalSetup {
    lazy val connector = {
      injected[CitizenDetailsConnector]
    }
  }

  "CitizenDetailsConnector" should {

    "fetch firstName and lastName of user based on nino" in new LocalSetup {
      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Some(Person(Some("Davey"),Some("Jones"), false)))))))

      val result = await(connector.getPersonDetails(Nino(nino)))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Some(Person(Some("Davey"),Some("Jones"), false)))

    }
  }

}
