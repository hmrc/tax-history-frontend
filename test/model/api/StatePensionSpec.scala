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

import java.math.MathContext
import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, ZoneOffset}

class StatePensionSpec extends GuiceAppSpec {

  val currentTaxYear: Int = TaxYear.current.currentYear
  val cyMinus1: Int       = TaxYear.current.previous.currentYear
  val cyPlus1: Int        = TaxYear.current.next.currentYear
  val cyMinus2: Int       = TaxYear.current.previous.currentYear - 1
  val grossAmount: Int    = 100

  val nonCurrentTaxYears: List[Int] = List(cyMinus1, cyPlus1, cyMinus2)

  "StatePension" should {

    "amountTillDate for current tax year, weekly payment" in {

      val startDate: LocalDate  = LocalDate.now().withYear(currentTaxYear)
      val grossAmountInput: Int = 5200
      val sp: StatePension      =
        StatePension(
          grossAmount = grossAmountInput,
          typeDescription = "test",
          paymentFrequency = Some(1),
          startDate = Some(startDate),
          startDateFormatted = Some(dateUtils.dateToFormattedString(startDate))
        )

      val amountTillDate =
        sp.getAmountReceivedTillDate(currentTaxYear).map(_.round(MathContext.DECIMAL32))

      val numberOfWeeksBetweenStartDateOfTaxYearPlus1Week =
        ChronoUnit.WEEKS.between(startDate, Instant.now().atOffset(ZoneOffset.UTC).toLocalDate).toInt + 1

      // This test breaks due to the calculation being dynamic, with the value changing based
      // on the number of weeks passed since the start of the current tax year.
      // The amount of StatePension received to date changes based on the number of weeks since the start of the tax year.
      // E.g. for 1 year (52 weeks), if someone is to receive £5200 of state pension for an entire year and one week
      // has passed since the current tax year's start date
      // Then the calculation is £5200/52 = £100
      // However when a new tax year rolls over and the current day is less than 1 week since the new tax year start date.
      // The date calculation ends up being 0, to compensate the method .getAmountReceivedTillDate() adds 1 to the number of weeks calculated.

      amountTillDate shouldBe Some(grossAmount / numberOfWeeksBetweenStartDateOfTaxYearPlus1Week)
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

      amountTillDate shouldBe Some(grossAmount)
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

        amountTillDate shouldBe Some(grossAmount)
      }
    }
  }
}
