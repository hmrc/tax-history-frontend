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

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, ZoneOffset}

class StatePensionSpec extends GuiceAppSpec {

  val currentTaxYear: Int           = TaxYear.current.currentYear
  val cyMinus1: Int                 = TaxYear.current.previous.currentYear
  val cyPlus1: Int                  = TaxYear.current.next.currentYear
  val cyMinus2: Int                 = TaxYear.current.previous.currentYear - 1
  val grossAmount: BigDecimal       = 100
  val startDate: LocalDate          = LocalDate.now().withYear(currentTaxYear)
  val sp: StatePension              = StatePension(
    grossAmount = grossAmount,
    typeDescription = "test",
    paymentFrequency = Some(1),
    startDate = Some(startDate),
    startDateFormatted = Some(dateUtils.dateToFormattedString(startDate))
  )
  val noOfPaymentsTillDate: Int     =
    ChronoUnit.WEEKS.between(startDate, Instant.now().atOffset(ZoneOffset.UTC).toLocalDate).toInt + 1
  val nonCurrentTaxYears: List[Int] = List(cyMinus1, cyPlus1, cyMinus2)

  "StatePension" should {
    "return amount received till date for current tax year, weekly payment" in {
      val amountTillDate = sp.getAmountReceivedTillDate(currentTaxYear)

      amountTillDate shouldBe Some(noOfPaymentsTillDate * sp.weeklyAmount)
    }

    "return amount received till date for current tax year, non weekly payment" in {
      val amountTillDate = sp.copy(paymentFrequency = Some(2)).getAmountReceivedTillDate(currentTaxYear)

      amountTillDate shouldBe Some(grossAmount)
    }

    nonCurrentTaxYears.foreach { taxYear =>
      s"return amount received till date for $taxYear tax year, weekly payment" in {
        val amountTillDate = sp.getAmountReceivedTillDate(taxYear)

        amountTillDate shouldBe Some(grossAmount)
      }
    }
  }
}
