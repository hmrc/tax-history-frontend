/*
 * Copyright 2018 HM Revenue & Customs
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

import java.util.UUID

import model.api.{Employment, EmploymentStatus}
import org.joda.time.LocalDate
import uk.gov.hmrc.play.test.UnitSpec

class StringUtilsSpec extends UnitSpec {

  "StringUtils" must {
    "return default message when there is no end date and employment status is Live" in {

      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = None,
        employmentStatus = EmploymentStatus.Live)
      StringUtils.getEndDate(emp, "current", "no data") shouldBe "current"
    }

    "return default message when there is no end date and employment status is not Live" in {

      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = None,
        employmentStatus = EmploymentStatus.PotentiallyCeased)
      StringUtils.getEndDate(emp, "current", "no data") shouldBe "no data"
    }


    "return formatted end date" in {

      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentStatus = EmploymentStatus.PotentiallyCeased)
      StringUtils.getEndDate(emp, "current", "no data") shouldBe "1 February 2016"
    }
  }

}
