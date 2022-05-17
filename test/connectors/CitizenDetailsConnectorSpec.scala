/*
 * Copyright 2022 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.libs.json.Json
import support.BaseSpec
import support.fixtures.ControllerFixture
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import utils.TestUtil

import scala.concurrent.{ExecutionContext, Future}

class CitizenDetailsConnectorSpec extends TestUtil with ControllerFixture with BaseSpec with ScalaFutures {

  val mockHttpClient: HttpClient = mock[HttpClient]

  val nino: String = randomNino.toString()
  val url: String = s"http://localhost:9337/citizen-details/$nino/designatory-details/basic"

  trait LocalSetup extends MockitoSugar {
    lazy val connector = new CitizenDetailsConnector(appConfig, mockHttpClient)
  }

  "CitizenDetailsConnector" should {

    "fetch firstName and lastName of user based on nino" in new LocalSetup {
      when(mockHttpClient.GET[HttpResponse](argEq(url), any(), any())(any[HttpReads[HttpResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(Status.OK, json = Json.toJson(person), Map.empty)))

      val result: HttpResponse = connector.getPersonDetails(Nino(nino)).futureValue

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Some(person))

    }
  }

}
