package utils

import org.joda.time.LocalDate
import org.scalatestplus.play.PlaySpec

class DateHelperSpec extends PlaySpec {

  "DateHelper" must {
    "convert input to the expected date format" in {
      DateHelper.formatDate(LocalDate.parse("2001-10-11")) mustBe "11 October 2001"
    }
  }

}
