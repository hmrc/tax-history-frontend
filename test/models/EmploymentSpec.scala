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

package models

import models.taxhistory.Employment
import play.api.libs.json.Json
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.play.test.UnitSpec


class EmploymentSpec extends UnitSpec {

  "Employment" should {
    "deserialize from json correctly" in {

      val json = Json.parse(
        """{
      "payeReference": "AA12341234",
      "employerName": "Test Employer Name",
      "taxablePayTotal": 25000.0,
      "taxTotal": 2000.0,
      "taxablePayEYU": 1000.0,
      "taxEYU": 250.0
    }""")

      val testEmployment = Employment("AA12341234", "Test Employer Name", Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0))

      Employment.formats.reads(json) shouldBe JsSuccess(testEmployment)


    }
  }

}
