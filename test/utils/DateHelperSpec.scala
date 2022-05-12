/*
 * Copyright 2022 HM Revenue & Customs
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

import model.api.EmploymentStatus
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import support.GuiceAppSpec
import uk.gov.hmrc.play.language.LanguageUtils
import views.taxhistory.Constants

import java.time.LocalDate

class DateHelperSpec extends GuiceAppSpec with Constants {

  val languageUtils: LanguageUtils = injected[LanguageUtils]
  val dateUtils = new DateUtils(languageUtils)

  "DateHelper" should {
    "convert input to the expected date format" in {
      dateUtils.dateToFormattedString(LocalDate.parse("2001-10-11")) mustBe "11 October 2001"
    }

    "format dates correctly for an employment object" in {
      val formattedEmployment = dateUtils.formatEmploymentDates(emp1)
      formattedEmployment.startDateFormatted.get mustBe "21 January 2016"
      formattedEmployment.endDateFormatted.get mustBe "1 January 2017"
    }

    "return noRecord for start date given no start date" in {
      dateUtils.formatEmploymentDates(emp1.copy(startDate = None)).startDateFormatted.get mustBe "No record"
    }

    "return noRecord for end date given a potentially ceased status" in {
      dateUtils.formatEmploymentDates(emp1.copy(employmentStatus = EmploymentStatus.PotentiallyCeased)).endDateFormatted.get mustBe "No record"
    }

    "return end date given an unknown status" in {
      dateUtils.formatEmploymentDates(emp1.copy(employmentStatus = EmploymentStatus.Unknown)).endDateFormatted.get mustBe "1 January 2017"
    }

    "return noRecord for end date given an unknown status and no end date" in {
      dateUtils.formatEmploymentDates(emp1.copy(employmentStatus = EmploymentStatus.Unknown, endDate = None)).endDateFormatted.get mustBe "No record"
    }

    "return end date given a live status" in {
      dateUtils.formatEmploymentDates(emp1.copy(employmentStatus = EmploymentStatus.Live)).endDateFormatted.get mustBe "1 January 2017"
    }

    "return ongoing for end date given a live status and no end date" in {
      dateUtils.formatEmploymentDates(emp1.copy(employmentStatus = EmploymentStatus.Live, endDate = None)).endDateFormatted.get mustBe "Ongoing"
    }
  }

}
