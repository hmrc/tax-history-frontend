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

package model.api

import support.BaseSpec
import utils.TestUtil

import java.util.UUID

class CompanyBenefitSpec extends TestUtil with BaseSpec {

  lazy val companyBenefit: CompanyBenefit = CompanyBenefit(
    companyBenefitId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "companyBenefitType",
    amount = BigDecimal(12.00),
    source = Some(3),
    isForecastBenefit = true
  )

  "CompanyBenefit" should {

    "generate companyBenefitId when none is supplied" in {
      val comBenefit =
        CompanyBenefit(
          iabdType = "otherCompanyBenefitType",
          amount = BigDecimal(10.00),
          source = None,
          isForecastBenefit = true
        )
      comBenefit.companyBenefitId.toString.nonEmpty shouldBe true
      comBenefit.companyBenefitId                  shouldNot be(companyBenefit.companyBenefitId)
    }
  }
}
