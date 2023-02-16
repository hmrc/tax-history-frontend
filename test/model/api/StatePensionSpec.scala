/*
 * Copyright 2023 HM Revenue & Customs
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

import support.GuiceAppSpec
import uk.gov.hmrc.time.TaxYear
import utils.TestUtil

import java.math.MathContext
import java.time.LocalDate

class StatePensionSpec extends GuiceAppSpec {

  val nino: String        = TestUtil.randomNino.toString()
  val currentTaxYear: Int = TaxYear.current.currentYear
  val cyMinus1: Int       = TaxYear.current.previous.currentYear
  val cyPlus1: Int        = TaxYear.current.next.currentYear
  val cyMinus2: Int       = TaxYear.current.previous.currentYear - 1
  val grossAmount: Int    = 100

  val nonCurrentTaxYears: List[Int] = List(cyMinus1, cyPlus1, cyMinus2)

  "StatePension" should {

    "amountTillDate for current tax year, weekly payment" in {
      val startDate: LocalDate = LocalDate.now().withYear(currentTaxYear)
      val sp: StatePension     = StatePension(
        grossAmount,
        "test",
        Some(1),
        Some(startDate),
        startDateFormatted = Some(dateUtils.dateToFormattedString(startDate))
      )
      val amountTillDate       = sp
        .getAmountReceivedTillDate(currentTaxYear)
        .map(_.round(MathContext.DECIMAL32))

      Some(101.9231) shouldBe amountTillDate
    }

    "amountTillDate for current tax year, non weekly payment" in {
      val startDate: LocalDate = LocalDate.now().withYear(currentTaxYear)
      val sp: StatePension     = StatePension(
        grossAmount,
        "test",
        Some(2),
        Some(startDate),
        startDateFormatted = Some(dateUtils.dateToFormattedString(startDate))
      )
      val amountTillDate       = sp.getAmountReceivedTillDate(currentTaxYear)

      Some(grossAmount) shouldBe amountTillDate
    }

    nonCurrentTaxYears.foreach { taxYear =>
      s"amountTillDate for $taxYear tax year, weekly payment" in {
        val startDate: LocalDate = LocalDate.now().withYear(currentTaxYear)
        val sp: StatePension     = StatePension(
          grossAmount,
          "test",
          Some(1),
          Some(startDate),
          startDateFormatted = Some(dateUtils.dateToFormattedString(startDate))
        )
        val amountTillDate       = sp.getAmountReceivedTillDate(taxYear)

        Some(grossAmount) shouldBe amountTillDate
      }
    }

  }
}
