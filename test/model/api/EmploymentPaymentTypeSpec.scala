/*
 * Copyright 2018 HM Revenue & Customs
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

import model.api.EmploymentPaymentType._
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

class EmploymentPaymentTypeSpec extends UnitSpec {

  "EmploymentPaymentType" must {
    import EmploymentPaymentType.format

    "read and write json successfully" in {
      format.reads(format.writes(JobseekersAllowance)) shouldBe JsSuccess(JobseekersAllowance)
      format.reads(format.writes(OccupationalPension)) shouldBe JsSuccess(OccupationalPension)
      format.reads(format.writes(IncapacityBenefit)) shouldBe JsSuccess(IncapacityBenefit)
      format.reads(format.writes(EmploymentAndSupportAllowance)) shouldBe JsSuccess(EmploymentAndSupportAllowance)
      format.reads(format.writes(StatePensionLumpSum)) shouldBe JsSuccess(StatePensionLumpSum)
    }

    "read unknown values successfully as Unknown" in {
      format.reads(JsString("SomeSurpriseFutureValue")) shouldBe JsSuccess(Unknown)
    }

    "throw error on invalid type" in {
      format.reads(JsNumber(10)) shouldBe a[JsError]
    }

  }

}
