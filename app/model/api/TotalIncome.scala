package model.api

import play.api.libs.json.{Json, OFormat}

case class TotalIncome(employmentTaxablePayTotal: BigDecimal,
                       pensionTaxablePayTotal: BigDecimal,
                       employmentTaxTotal: BigDecimal,
                       pensionTaxTotal: BigDecimal)

object TotalIncome {
  implicit val formats: OFormat[TotalIncome] = Json.format[TotalIncome]
}
