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

package utils

import org.scalatestplus.play.PlaySpec

class CurrencySpec extends PlaySpec {

  "Currency" must {
    "format +ve value successfully" in {
      Currency(BigDecimal(100.55)).toString must be("£100.55")
    }

    "format -ve value successfully" in {
      Currency(BigDecimal(-100.55)).toString must be("- £100.55")
    }

    "format zero value successfully" in {
      Currency(BigDecimal(0), 2).toString must be("£0.00")
    }

    "format number to skip .00 from decimals when no minDPs is provided" in {
      Currency(BigDecimal(100.00)).toString must be("£100")
    }

    "format number to convert 100.00 to 100.000 when minDPs is 3" in {
      Currency(BigDecimal(100.00), 3).toString must be("£100.000")
    }

    "format number to convert 100 to 100.00 when minDPs is 2" in {
      val oneHundredInt = 100
      Currency(BigDecimal(oneHundredInt), 2).toString must be("£100.00")
    }

    "format number 100.12 from decimals" in {
      Currency(BigDecimal(100.12)).toString must be("£100.12")
    }

    "format -ve number to skip .00 from decimals when no minDPs is provided" in {
      Currency(BigDecimal(-100.00)).toString must be("- £100")
    }

    "return nothing when using fromOptionBD if none is provided" in {
      Currency.fromOptionBD(None) must be("")
    }

    "format +ve value successfully when using fromOptionBD" in {
      Currency.fromOptionBD(Some(100.55)) must be("£100.55")
    }

    "format +ve with the + prefix when using withPositive" in {
      Currency.withPositive(100.55) must be("+£100.55")
    }

    "format -ve with the - prefix when using withPositive" in {
      Currency.withPositive(BigDecimal(-100.55)) must be("-£100.55")
    }

    "add extra 0 when there is 1 number after the decimal point" in {
      Currency(BigDecimal(101.1)).toString must be("£101.10")
    }

    "convert BigDecimal to Currency value successfully when using fromBD" in {
      Currency.fromBD(100.55) must be(Currency(BigDecimal(100.55)))
    }

    "convert Int to Currency value successfully when using fromBD" in {
      Currency.fromInt(3) must be(Currency(BigDecimal(3.00)))
    }

    "convert Currency to Double value successfully when using currencyToDouble" in {
      Currency.currencyToDouble(Currency(BigDecimal(100.55))) must be(100.55.doubleValue())
    }

    "convert Currency to Float value successfully when using currencyToFloat" in {
      Currency.currencyToFloat(Currency(BigDecimal(100.55))) must be(100.55.floatValue())
    }
  }
}
