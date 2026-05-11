/*
 * Copyright 2026 HM Revenue & Customs
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
import utils.LocalDateFormat

import java.time.LocalDate
import java.util.UUID

case class PayAndTax(
  payAndTaxId: UUID = UUID.randomUUID(),
  taxablePayTotal: Option[BigDecimal] = None,
  taxTotal: Option[BigDecimal] = None,
  studentLoan: Option[BigDecimal] = None,
  studentLoanIncludingEYU: Option[BigDecimal] = None,
  paymentDate: Option[LocalDate] = None,
  earlierYearUpdates: List[EarlierYearUpdate] = Nil
) {
  val earlierYearUpdatesWithStudentLoans: List[EarlierYearUpdate] =
    earlierYearUpdates.filter(_.studentLoanEYU.isDefined)
}

object PayAndTax extends LocalDateFormat {
  implicit val formats: OFormat[PayAndTax] = Json.format[PayAndTax]
}
