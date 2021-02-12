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

import model.api.EmploymentPaymentType._
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

class EmploymentPaymentTypeSpec extends UnitSpec {

  "EmploymentPaymentType" must {
    import EmploymentPaymentType.format

    "deserialising from json" when {
      "deserialising from string 'JobseekersAllowance'" in {
        format.reads(JsString("JobseekersAllowance")) shouldBe JsSuccess(JobseekersAllowance)
      }
      "deserialising from string 'OccupationalPension'" in {
        format.reads(JsString("OccupationalPension")) shouldBe JsSuccess(OccupationalPension)
      }
      "deserialising from string 'IncapacityBenefit'" in {
        format.reads(JsString("IncapacityBenefit")) shouldBe JsSuccess(IncapacityBenefit)
      }
      "deserialising from string 'EmploymentAndSupportAllowance'" in {
        format.reads(JsString("EmploymentAndSupportAllowance")) shouldBe JsSuccess(EmploymentAndSupportAllowance)
      }
      "deserialising from string 'StatePensionLumpSum'" in {
        format.reads(JsString("StatePensionLumpSum")) shouldBe JsSuccess(StatePensionLumpSum)
      }
    }

    "serialising to json" when {
      "serialising from JobseekersAllowance" in {
        format.writes(JobseekersAllowance) shouldBe JsString("JobseekersAllowance")
      }
      "serialising from OccupationalPension" in {
        format.writes(OccupationalPension) shouldBe JsString("OccupationalPension")
      }
      "serialising from IncapacityBenefit" in {
        format.writes(IncapacityBenefit) shouldBe JsString("IncapacityBenefit")
      }
      "serialising from EmploymentAndSupportAllowance" in {
        format.writes(EmploymentAndSupportAllowance) shouldBe JsString("EmploymentAndSupportAllowance")
      }
      "serialising from StatePensionLumpSum" in {
        format.writes(StatePensionLumpSum) shouldBe JsString("StatePensionLumpSum")
      }
    }

    "read unknown values successfully as Unknown" in {
      format.reads(JsString("SomeSurpriseFutureValue")) shouldBe JsSuccess(Unknown)
    }

    "throw error on invalid type" in {
      format.reads(JsNumber(10)) shouldBe a[JsError]
    }

  }

}
