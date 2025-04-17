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

package connectors

import models.taxhistory.Person
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse

class CitizenDetailsConnectorSpec extends BaseConnectorSpec {

  lazy val connector         = new CitizenDetailsConnector(appConfig, mockHttpClient)
  val person: Option[Person] = Some(Person(Some("first name"), Some("second name"), Some(false)))

  "CitizenDetailsConnector" should {

    "fetch firstName and lastName of user based on nino" in {
      mockExecuteMethod(Json.toJson(person).toString(), Status.OK)

      val result: HttpResponse = connector.getPersonDetails(Nino(nino)).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(Some(person))
    }
  }

}
