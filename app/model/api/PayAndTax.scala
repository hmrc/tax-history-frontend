/*
 * Copyright 2019 HM Revenue & Customs
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

import java.util.UUID

import org.joda.time.LocalDate
import play.api.libs.json.Json

case class PayAndTax(payAndTaxId:UUID = UUID.randomUUID(),
                     taxablePayTotal: Option[BigDecimal],
                     taxablePayTotalIncludingEYU: Option[BigDecimal],
                     taxTotal: Option[BigDecimal],
                     taxTotalIncludingEYU: Option[BigDecimal],
                     studentLoan: Option[BigDecimal],
                     studentLoanIncludingEYU: Option[BigDecimal],
                     paymentDate: Option[LocalDate],
                     earlierYearUpdates: List[EarlierYearUpdate]){

  val earlierYearUpdatesWithStudentLoans: List[EarlierYearUpdate] = earlierYearUpdates
    .filter(_.studentLoanEYU.isDefined)
  val earlierYearUpdatesWithNonZeroPayOrTax: List[EarlierYearUpdate] = earlierYearUpdates
    .filter( x => x.taxablePayEYU != 0.00 || x.taxEYU != 0.00)
}

object PayAndTax {
  implicit val formats = Json.format[PayAndTax]
}

