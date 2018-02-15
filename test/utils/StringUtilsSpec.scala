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
import org.joda.time.format.DateTimeFormat
import play.api.i18n.Messages
import support.GuiceAppSpec

class StringUtilsSpec extends GuiceAppSpec {

  "StringUtils - getEndDate" must {
    "return default message when there is no end date and employment status is Live" in {
      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = None,
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716")

      StringUtils.getEndDate(emp) shouldBe Messages("lbl.end-date.ongoing")
    }

    "return end date when there is an end date and employment status is Live" in {
      val parsedEndDate = LocalDate.parse("2016-02-07")

      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(parsedEndDate),
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716")

      StringUtils.getEndDate(emp) shouldBe DateTimeFormat.forPattern("d MMMM yyyy").print(parsedEndDate)
    }

    "return error message when employment status is PotentiallyCeased" in {
      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = None,
        employmentStatus = EmploymentStatus.PotentiallyCeased,
        worksNumber = "00191048716")

      StringUtils.getEndDate(emp) shouldBe Messages("lbl.end-date.unknown")
    }

    "return formatted end date" in {
      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716")

      StringUtils.getEndDate(emp) shouldBe "1 February 2016"
    }

    "return date when Employment Status is unknown and endDate is provided" in {
      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentStatus = EmploymentStatus.Unknown,
        worksNumber = "00191048716")

      StringUtils.getEndDate(emp) shouldBe "1 February 2016"
    }

    "return unknown when Employment Status is unknown and no endDate is provided" in {
      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = None,
        employmentStatus = EmploymentStatus.Unknown,
        worksNumber = "00191048716")

      StringUtils.getEndDate(emp) shouldBe Messages("lbl.end-date.unknown")
    }
  }

  "StringUtils - getEmploymentStatus" must {
    "return default message when employment is Live" in {
      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716")

      StringUtils.getEmploymentStatus(emp) shouldBe Messages("lbl.employment.status.current")
    }

    "return alternate message when employment is not Live" in {
      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentStatus = EmploymentStatus.PotentiallyCeased,
        worksNumber = "00191048716")

      StringUtils.getEmploymentStatus(emp) shouldBe Messages("lbl.employment.status.ceased")
    }

    "return unknown message when employment status is unknown" in {
      val emp =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentStatus = EmploymentStatus.Unknown,
        worksNumber = "00191048716")

      StringUtils.getEmploymentStatus(emp) shouldBe Messages("lbl.employment.status.unknown")
    }
  }
}
