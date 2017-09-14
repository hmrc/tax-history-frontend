package utils

import org.scalatestplus.play.PlaySpec

class CurrencySpec extends PlaySpec {

  "Currency" must {
    "format +ve value successfully" in {
      Currency(BigDecimal(1000.55)).toString must be("£1,000.55")
    }

    "format -ve value successfully" in {
      Currency(BigDecimal(-1000.55)).toString must be("-£1000.55")
    }
  }
}
