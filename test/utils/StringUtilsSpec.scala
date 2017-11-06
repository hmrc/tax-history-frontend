package utils

import model.api.EmploymentStatus
import uk.gov.hmrc.play.test.UnitSpec

class StringUtilsSpec extends UnitSpec {

  "StringUtils" must {
    "return default message when there is no end date and employment status is Live" in {
      StringUtils.getDefaultEndDateText(EmploymentStatus.Live, "current", "no data") shouldBe "current"
    }

    "return default message when there is no end date and employment status is not Live" in {
      StringUtils.getDefaultEndDateText(EmploymentStatus.Ceased, "current", "no data") shouldBe "no data"
    }
  }

}
