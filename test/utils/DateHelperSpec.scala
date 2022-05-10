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

  val languageUtils = injected[LanguageUtils]
  val dateUtils = new DateUtils(languageUtils)

  "DateHelper" should {
    "convert input to the expected date format" in {
      dateUtils.format(LocalDate.parse("2001-10-11")) mustBe "11 October 2001"
    }

    "convert optional some input to the expected date format" in {
      dateUtils.format(Some(LocalDate.parse("2001-10-11"))) mustBe "11 October 2001"
    }

    "convert optional none input to empty string" in {
      dateUtils.format(None) mustBe ""
    }

    "get start date" in {
      dateUtils.getStartDate(emp1) mustBe "21 January 2016"
    }

    "return no record given start date" in {
      dateUtils.getStartDate(emp1.copy(startDate = None)) mustBe "No record"
    }

    "get end date" in {
      dateUtils.getEndDate(emp1) mustBe "1 January 2017"
    }

    "return no record given a potentially ceased status" in {
      dateUtils.getEndDate(emp1.copy(employmentStatus = EmploymentStatus.PotentiallyCeased)) mustBe "No record"
    }

    "return end date given an unknown status" in {
      dateUtils.getEndDate(emp1.copy(employmentStatus = EmploymentStatus.Unknown)) mustBe "1 January 2017"
    }

    "return no record given an unknown status and no end date" in {
      dateUtils.getEndDate(emp1.copy(employmentStatus = EmploymentStatus.Unknown, endDate = None)) mustBe "No record"
    }

    "return end date given a live status" in {
      dateUtils.getEndDate(emp1.copy(employmentStatus = EmploymentStatus.Live)) mustBe "1 January 2017"
    }

    "return ongoing given a live status and no end date" in {
      dateUtils.getEndDate(emp1.copy(employmentStatus = EmploymentStatus.Live, endDate = None)) mustBe "Ongoing"
    }
  }

}
