/*
 * Copyright 2020 HM Revenue & Customs
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

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import play.api.http.Status
import play.api.libs.json.Json
import support.fixtures.PersonFixture
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil
import views.TestAppConfig

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CitizenDetailsConnectorSpec extends UnitSpec with MockitoSugar with TestUtil with PersonFixture with TestAppConfig {

  private implicit val hc = HeaderCarrier()

  val mockHttpClient: HttpClient = mock[HttpClient]

  val nino =randomNino.toString()

  trait LocalSetup extends MockitoSugar with ArgumentMatchersSugar {
    lazy val connector = new CitizenDetailsConnector(appConfig, mockHttpClient)
  }

  "CitizenDetailsConnector" should {

    "fetch firstName and lastName of user based on nino" in new LocalSetup {
      when(mockHttpClient.GET[HttpResponse](any)(any, any, any)).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Some(person))))))

      val result = await(connector.getPersonDetails(Nino(nino)))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Some(person))
    }
  }

}
