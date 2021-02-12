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

package utils

import java.util.Locale

case class Currency(value: BigDecimal, minDecimalPlaces: Int = 0) {

  override def toString: String = {
    val formatter = java.text.NumberFormat.getCurrencyInstance(Locale.UK)

    if (value % 1 != 0)formatter.setMinimumFractionDigits(2)
    else formatter.setMinimumFractionDigits(minDecimalPlaces)

    if (value.signum >= 0)formatter.format(value)
    else "- " + formatter.format(value.abs)
  }

}

object Currency {
  implicit def fromOptionBD(value:Option[BigDecimal]): String = {
    value match {
      case Some(amount) => Currency(amount).toString
      case None => ""
    }
  }

  implicit def fromBD(value: BigDecimal): Currency = Currency(value)

  implicit def fromInt(value: Int): Currency = Currency(value)

  implicit def currencyToDouble(c: Currency): Double = c.value.toDouble

  implicit def currencyToFloat(c: Currency): Float = c.value.toFloat

  implicit def withPositive(value:BigDecimal):String ={
    if (value >= 0) "+" + Currency(value)
    else "-" + Currency(value.abs)
  }

}

