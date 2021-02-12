/*
 * Copyright 2021 HM Revenue & Customs
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

import model.api.EmploymentStatus.{Ceased, Live, PotentiallyCeased}
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

class EmploymentStatusSpec extends UnitSpec {

  "EmploymentStatus" must {
    "read and write json successfully" in {
      EmploymentStatus.jsonReads.reads(EmploymentStatus.jsonWrites.writes(EmploymentStatus.Live)) shouldBe JsSuccess(Live)
      EmploymentStatus.jsonReads.reads(EmploymentStatus.jsonWrites.writes(EmploymentStatus.Ceased)) shouldBe JsSuccess(Ceased)
      EmploymentStatus.jsonReads.reads(EmploymentStatus.jsonWrites.writes(EmploymentStatus.PotentiallyCeased)) shouldBe JsSuccess(PotentiallyCeased)
    }

    "throw error on invalid data" in {
      EmploymentStatus.jsonReads.reads(Json.obj("employmentStatus" -> 10)) shouldBe JsError(List((JsPath \ "employmentStatus",
        List(JsonValidationError(List("Invalid EmploymentStatus"))))))
    }
  }

}
