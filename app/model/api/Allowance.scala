package model.api

import play.mvc.BodyParser.Json

case class Allowance()

object Allowance {
  implicit val formats = Json.format[IabdAllowance]
}
