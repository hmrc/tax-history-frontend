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

package model.api

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

import java.time.LocalDate
import java.util.UUID

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil

class EmploymentSpec extends TestUtil with UnitSpec {

  lazy val employmentJson = loadFile("/json/model/api/employment.json")
  lazy val employmentNoEndDateJson = loadFile("/json/model/api/employmentNoEndDate.json")
  lazy val employmentListJson = loadFile("/json/model/api/employments.json")

  lazy val employment1 =  Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = LocalDate.parse("2016-01-21"),
    endDate = Some(LocalDate.parse("2017-01-01"))
  )
  lazy val employment2 = Employment(
    employmentId = UUID.fromString("019f5fee-d5e4-4f3e-9569-139b8ad81a87"),
    payeReference = "paye-2",
    employerName = "employer-2",
    startDate = LocalDate.parse("2016-02-22")
  )

  lazy val employmentList = List(employment1,employment2)

  "Employment" should {

    "transform into Json from object correctly " in {
      Json.toJson(employment1) shouldBe employmentJson
    }
    "transform into object from json correctly " in {
      employmentJson.as[Employment] shouldBe employment1
    }
    "generate employmentId when none is supplied" in {
      val emp = Employment(payeReference = "paye-1",
        employerName = "employer-1",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2017-01-01"))
      )
      emp.employmentId.toString.nonEmpty shouldBe true
      emp.employmentId shouldNot be(employment1.employmentId)
    }
    "transform into Json from object list correctly " in {
      Json.toJson(employmentList) shouldBe employmentListJson
    }
    "transform into object list from json correctly " in {
      employmentListJson.as[List[Employment]] shouldBe employmentList
    }
    "allow omission of endDate in json" in {
      employmentNoEndDateJson.as[Employment] shouldBe employment2
    }

  }
}



