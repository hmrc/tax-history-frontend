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

class TotalIncomeSpec extends TestUtil with BaseSpec {

  private val employmentId                            = UUID.randomUUID()
  private val employment                              = Employment(
    employmentId,
    startDate = None,
    payeReference = "paye-1",
    employerName = "employer-1",
    employmentStatus = EmploymentStatus.Live,
    worksNumber = "1",
    employmentPaymentType = None
  )
  val employmentAndIncomeTax1: EmploymentIncomeAndTax =
    EmploymentIncomeAndTax(employmentId.toString, BigDecimal(10), BigDecimal(5))
  private val totalIncome                             = TotalIncome(
    employmentIncomeAndTax = List(employmentAndIncomeTax1),
    employmentTaxablePayTotalIncludingEYU = BigDecimal(10),
    pensionTaxablePayTotalIncludingEYU = BigDecimal(10),
    employmentTaxTotalIncludingEYU = BigDecimal(10),
    pensionTaxTotalIncludingEYU = BigDecimal(10)
  )

  "TotalIncome" should {

    "Get the employmentIncomeAndTax when it exists for an Employment" in {
      totalIncome.getIncomeAndTax(employment) shouldBe Some(employmentAndIncomeTax1)
    }

    "Not get the employmentIncomeAndTax when it does not exists for an Employment" in {
      totalIncome.getIncomeAndTax(employment.copy(employmentId = UUID.randomUUID())) shouldBe None
    }
  }
}
