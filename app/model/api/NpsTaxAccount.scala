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

import play.api.libs.json.Json

case class TaDeduction(`type`:Int,
                       npsDescription: String,
                       amount: BigDecimal,
                       sourceAmount: Option[BigDecimal])

object TaDeduction {
  implicit val formats = Json.format[TaDeduction]
}

case class TaAllowance(`type`:Int,
                       npsDescription: String,
                       amount: BigDecimal,
                       sourceAmount: Option[BigDecimal])

object TaAllowance {
  implicit val formats = Json.format[TaAllowance]
}


case class IncomeSource(employmentId:Int,
                        employmentType:Int,
                        actualPUPCodedInCYPlusOneTaxYear:Option[BigDecimal],
                        deductions: List[TaDeduction],
                        allowances: List[TaAllowance],
                        taxCode: String,
                        basisOperation: Option[Int],
                        employmentTaxDistrictNumber: Int,
                        employmentPayeRef: String)

object IncomeSource {
  implicit val formats = Json.format[IncomeSource]
}

