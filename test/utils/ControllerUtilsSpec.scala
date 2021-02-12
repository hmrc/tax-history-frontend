/*
 * Copyright 2021 HM Revenue & Customs
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
import views.taxhistory.DetailConstants

class ControllerUtilsSpec extends GuiceAppSpec with DetailConstants {

  "ControllerUtils - getEndDate" must {
    "return default message when there is no end date and employment status is Live" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = None,
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716")

      ControllerUtils.getEndDate(emp) shouldBe Messages("lbl.end-date.ongoing")
    }

    "return end date when there is an end date and employment status is Live" in {
      val parsedEndDate = LocalDate.parse("2016-02-07")

      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(parsedEndDate),
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716")

      ControllerUtils.getEndDate(emp) shouldBe DateTimeFormat.forPattern("d MMMM yyyy").print(parsedEndDate)
    }

    "return default message when employment status is PotentiallyCeased" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = None,
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.PotentiallyCeased,
        worksNumber = "00191048716")

      ControllerUtils.getEndDate(emp) shouldBe Messages("lbl.date.no-record")
    }

    "return formatted end date" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716")

      ControllerUtils.getEndDate(emp) shouldBe "1 February 2016"
    }

    "return date when Employment Status is unknown and endDate is provided" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Unknown,
        worksNumber = "00191048716")

      ControllerUtils.getEndDate(emp) shouldBe "1 February 2016"
    }

    "return unknown when Employment Status is unknown and no endDate is provided" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = None,
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Unknown,
        worksNumber = "00191048716")

      ControllerUtils.getEndDate(emp) shouldBe Messages("lbl.date.no-record")
    }
  }

  "ControllerUtils - getStartDate" must {
    "return default message when there is no start date" in {
      val employmentNoStart = employment.copy(startDate = None)
      ControllerUtils.getStartDate(employmentNoStart) shouldBe "No record"
    }

    "return formatted date when there is a start date" in {
      val employmentNoStart = employment.copy(startDate = Some(LocalDate.parse("2016-02-01")))
      ControllerUtils.getStartDate(employmentNoStart) shouldBe "1 February 2016"
    }
  }

  "ControllerUtils - getEmploymentStatus" must {
    "return default message when employment is Live" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716")

      ControllerUtils.getEmploymentStatus(emp) shouldBe Messages("lbl.employment.status.current")
    }

    "return alternate message when employment is not Live" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.PotentiallyCeased,
        worksNumber = "00191048716")

      ControllerUtils.getEmploymentStatus(emp) shouldBe Messages("lbl.employment.status.ceased")
    }

    "return unknown message when employment status is unknown" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Unknown,
        worksNumber = "00191048716")

      ControllerUtils.getEmploymentStatus(emp) shouldBe Messages("lbl.employment.status.unknown")
    }
  }

  "ControllerUtils - hasEmploymentDetails" must {
    "return true when employment status is not unknown" in {
      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(LocalDate.parse("2016-02-01")),
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.PotentiallyCeased,
        worksNumber = "00191048716")

      ControllerUtils.hasEmploymentDetails(emp) shouldBe true
    }

    "ControllerUtils - hasEmploymentDetails" must {
      "return false when employment status is unknown" in {
        val emp = Employment(
          employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
          payeReference = "paye",
          employerName = "employer",
          startDate = Some(LocalDate.parse("2016-01-21")),
          endDate = Some(LocalDate.parse("2016-02-01")),
          employmentPaymentType = None,
          employmentStatus = EmploymentStatus.Unknown,
          worksNumber = "00191048716")

        ControllerUtils.hasEmploymentDetails(emp) shouldBe false
      }
    }
  }

  "ControllerUtils - displaySource " must {
    "return none when both source amount and amount are the same" in {
      val sourceAmount: Option[BigDecimal] = Some(1)
      val amount: BigDecimal = 1

      ControllerUtils.displaySource(sourceAmount, amount) shouldBe None
    }

    "return the source amount when both source amount and amount are different" in {
      val sourceAmount: Option[BigDecimal] = Some(2)
      val amount: BigDecimal = 1

      ControllerUtils.displaySource(sourceAmount, amount) shouldBe Some(2)
    }
  }

  "ControllerUtils - displaytaxCode " must {
    "return X when basisOperation is either a 1 0r a 3" in {
      val basisOperation1: Option[Int] = Some(1)
      val basisOperation3: Option[Int] = Some(3)

      ControllerUtils.displayTaxCode(basisOperation1) shouldBe Some("X")
      ControllerUtils.displayTaxCode(basisOperation3) shouldBe Some("X")
    }
    "return None in all other cases" in {
      val basisOperation2: Option[Int] = Some(2)
      val basisOperation4: Option[Int] = Some(4)

      ControllerUtils.displayTaxCode(basisOperation2) shouldBe None
      ControllerUtils.displayTaxCode(basisOperation4) shouldBe None
      ControllerUtils.displayTaxCode(None) shouldBe None
    }
  }

  "ControllerUtils - sentenceCase " must {
    "return the input with the first letter capitalised and the rest lower case" in {
      ControllerUtils.sentenceCase("heLLo WorLD") shouldBe "Hello world"
    }
  }

  "ControllerUtils - isJobSeekerAllowance" must {
    "return the employers name if employmentPaymentType is not JobseekersAllowance" in {
      ControllerUtils.isJobSeekerAllowance(employment) shouldBe "employer-1"
    }

    "return the term Jobseeker''s Allowance if employmentPaymentType is JobseekersAllowance" in {
      ControllerUtils.isJobSeekerAllowance(employmentWithJobseekers) shouldBe Messages("employmenthistory.job.seekers")
    }
  }
}
