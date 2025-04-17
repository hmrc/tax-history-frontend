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

package views.models

import support.GuiceAppSpec

class EmploymentViewDetailSpec extends GuiceAppSpec {

  "EmploymentViewDetail" should {
    "return a EmploymentViewDetail object" when {
      "the client has job seekers allowance" in {
        val isJobSeekersAllowance = true
        val isOccupationalPension = false
        val incomeName            = "testJobSeekers"
        val jobSeekersDetail      = EmploymentViewDetail(isJobSeekersAllowance, isOccupationalPension, incomeName)
        jobSeekersDetail.heading shouldBe messages("employmenthistory.job.seekers")
        jobSeekersDetail.title   shouldBe messages("employmenthistory.employment.details.job.seekers.title")
      }

      "the client has occupational pension" in {
        val isJobSeekersAllowance = false
        val isoccupationalPension = true
        val pensionName           = "testPensionName"
        val pensionDetail         = EmploymentViewDetail(isJobSeekersAllowance, isoccupationalPension, pensionName)
        pensionDetail.heading shouldBe messages("employmenthistory.jobtype.pension", pensionName)
        pensionDetail.title   shouldBe messages("employmenthistory.employment.details.pension.title")
      }

      "the client has employment detail" in {
        val isJobSeekersAllowance = false
        val isoccupationalPension = false
        val employmentName        = "testEmploymentName"
        val employerDetail        = EmploymentViewDetail(isJobSeekersAllowance, isoccupationalPension, employmentName)
        employerDetail.heading shouldBe messages("employmenthistory.jobtype.employment", employmentName)
        employerDetail.title   shouldBe messages("employmenthistory.employment.details.employment.title")
      }
    }
  }
}
