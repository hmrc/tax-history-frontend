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

import model.api.EmploymentStatus
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import support.GuiceAppSpec
import views.taxhistory.Constants

class DateUtilsSpec extends GuiceAppSpec with Constants {

  "DateHelper" should {

    "format pension start date if present" in {
      dateUtils.formatStatePensionStartDate(statePension).startDateFormatted.get mustBe "30 December 2000"

      dateUtils.formatStatePensionStartDate(statePension)(welshMessages).startDateFormatted.get mustBe "30 Rhagfyr 2000"
    }

    "return none given no pension start date" in {
      dateUtils.formatStatePensionStartDate(statePension.copy(startDate = None)).startDateFormatted mustBe None
    }

    "format Earlier Year Update Receive date" in {
      val dates = List("21 January 2016", "21 May 2016")
      dateUtils
        .formatEarlierYearUpdateReceivedDate(payAndTax)
        .earlierYearUpdates
        .flatMap(_.receivedDateFormatted) should equal(dates)

      val welshDates = List("21 Ionawr 2016", "21 Mai 2016")
      dateUtils
        .formatEarlierYearUpdateReceivedDate(payAndTax)(welshMessages)
        .earlierYearUpdates
        .flatMap(_.receivedDateFormatted) should equal(welshDates)
    }

    "format dates abbreviating the month correctly for an employment object" in {
      val formattedEmployment = dateUtils.formatEmploymentDatesAbbrMonth(emp1LiveOccupationalPension)
      formattedEmployment.startDateFormatted.get mustBe "21 Jan 2016"
      formattedEmployment.endDateFormatted.get mustBe "1 Jan 2017"

      val formattedEmploymentWelsh =
        dateUtils.formatEmploymentDatesAbbrMonth(emp1LiveOccupationalPension)(welshMessages)
      formattedEmploymentWelsh.startDateFormatted.get mustBe "21 Ion 2016"
      formattedEmploymentWelsh.endDateFormatted.get mustBe "1 Ion 2017"
    }

    "format dates correctly for an employment object" in {
      val formattedEmployment = dateUtils.formatEmploymentDates(emp1LiveOccupationalPension)
      formattedEmployment.startDateFormatted.get mustBe "21 January 2016"
      formattedEmployment.endDateFormatted.get mustBe "1 January 2017"

      val formattedEmploymentWelsh = dateUtils.formatEmploymentDates(emp1LiveOccupationalPension)(welshMessages)
      formattedEmploymentWelsh.startDateFormatted.get mustBe "21 Ionawr 2016"
      formattedEmploymentWelsh.endDateFormatted.get mustBe "1 Ionawr 2017"
    }

    "return noRecord for start date given no start date" in {
      dateUtils
        .formatEmploymentDates(emp1LiveOccupationalPension.copy(startDate = None))
        .startDateFormatted
        .get mustBe "No record"

      dateUtils
        .formatEmploymentDates(emp1LiveOccupationalPension.copy(startDate = None))(welshMessages)
        .startDateFormatted
        .get mustBe "Dim cofnod"
    }

    "return noRecord for end date given a potentially ceased status" in {
      dateUtils
        .formatEmploymentDates(emp1LiveOccupationalPension.copy(employmentStatus = EmploymentStatus.PotentiallyCeased))
        .endDateFormatted
        .get mustBe "No record"

      dateUtils
        .formatEmploymentDates(emp1LiveOccupationalPension.copy(employmentStatus = EmploymentStatus.PotentiallyCeased))(
          welshMessages
        )
        .endDateFormatted
        .get mustBe "Dim cofnod"
    }

    "return end date given a live status" in {
      dateUtils
        .formatEmploymentDates(emp1LiveOccupationalPension.copy(employmentStatus = EmploymentStatus.Live))
        .endDateFormatted
        .get mustBe "1 January 2017"

      dateUtils
        .formatEmploymentDates(emp1LiveOccupationalPension.copy(employmentStatus = EmploymentStatus.Live))(
          welshMessages
        )
        .endDateFormatted
        .get mustBe "1 Ionawr 2017"
    }

    "return ongoing for end date given a live status and no end date" in {
      dateUtils
        .formatEmploymentDates(
          emp1LiveOccupationalPension.copy(employmentStatus = EmploymentStatus.Live, endDate = None)
        )
        .endDateFormatted
        .get mustBe "Ongoing"

      dateUtils
        .formatEmploymentDates(
          emp1LiveOccupationalPension.copy(employmentStatus = EmploymentStatus.Live, endDate = None)
        )(welshMessages)
        .endDateFormatted
        .get mustBe "Parhaus"
    }
  }

}
