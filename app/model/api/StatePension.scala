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

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.time.TaxYear

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, ZoneOffset}

case class StatePension(
  grossAmount: BigDecimal,
  typeDescription: String,
  paymentFrequency: Option[Int] = None,
  startDate: Option[LocalDate] = None,
  startDateFormatted: Option[String] = None
) {

  lazy val weeklyAmount: BigDecimal = grossAmount / 52

  def getAmountReceivedTillDate(taxYear: Int): Option[BigDecimal] =
    paymentFrequency match {
      case Some(1) if TaxYear.current.currentYear == taxYear =>
        // Weekly
        startDate.flatMap { start =>
          val noOfWeeksTillDate =
            ChronoUnit.WEEKS.between(start, Instant.now().atOffset(ZoneOffset.UTC).toLocalDate).toInt

          val noOfPaymentsTillDate =
            noOfWeeksTillDate + 1 // noOfWeeksTillDate comes out as one less than the no of payments

          Some(noOfPaymentsTillDate * weeklyAmount)
        }
      case _                                                 => Some(grossAmount)
    }
}

object StatePension {
  implicit val formats: OFormat[StatePension] = Json.format[StatePension]
}
