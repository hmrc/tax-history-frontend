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

import java.util.UUID

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import model.api.CompanyBenefit
import utils.TestUtil

class CompanyBenefitSpec extends TestUtil with UnitSpec {

  lazy val companyBenefitJson = loadFile("/json/model/api/companyBenefit.json")
  lazy val companyBenefitListJson = loadFile("/json/model/api/companyBenefits.json")

  lazy val companyBenefit = CompanyBenefit(companyBenefitId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
                                  iabdType = "companyBenefitType",
                                  amount = BigDecimal(12.00))

  lazy val companyBenefitList = List(companyBenefit)

  "CompanyBenefit" should {
    "transform into Json from object correctly " in {
      Json.toJson(companyBenefit) shouldBe companyBenefitJson
    }
    "transform into object from json correctly " in {
      companyBenefitJson.as[CompanyBenefit] shouldBe companyBenefit
    }
    "generate companyBenefitId when none is supplied" in {
      val comBenefit = CompanyBenefit(iabdType = "otherCompanyBenefitType", amount = BigDecimal(10.00))
      comBenefit.companyBenefitId.toString.nonEmpty shouldBe true
      comBenefit.companyBenefitId shouldNot be(companyBenefit.companyBenefitId)
    }
    "transform into Json from object list correctly " in {
      Json.toJson(companyBenefitList) shouldBe companyBenefitListJson
    }
    "transform into object list from json correctly " in {
      companyBenefitListJson.as[List[CompanyBenefit]] shouldBe companyBenefitList
    }
  }
}


