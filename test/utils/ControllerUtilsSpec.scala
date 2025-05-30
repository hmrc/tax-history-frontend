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

import model.api.{Employment, EmploymentStatus}
import play.api.i18n.Messages
import support.GuiceAppSpec
import uk.gov.hmrc.time.TaxYear
import views.taxhistory.Constants

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class ControllerUtilsSpec extends GuiceAppSpec with Constants {

  "ControllerUtils - formatEndDate" must {
    "return default message when there is no end date and employment status is Live" in {

      val emp = Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye",
        employerName = "employer",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = None,
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716"
      )

      dateUtils.formatEndDate(emp, dateUtils.dateToFormattedString) shouldBe Messages("lbl.end-date.ongoing")
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
        worksNumber = "00191048716"
      )

      dateUtils.formatEndDate(emp, dateUtils.dateToFormattedString) shouldBe DateTimeFormatter
        .ofPattern("d MMMM yyyy")
        .format(parsedEndDate)
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
        worksNumber = "00191048716"
      )

      dateUtils.formatEndDate(emp, dateUtils.dateToFormattedString) shouldBe Messages("lbl.date.no-record")
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
        worksNumber = "00191048716"
      )

      dateUtils.formatEndDate(emp, dateUtils.dateToFormattedString) shouldBe "1 February 2016"
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
        worksNumber = "00191048716"
      )

      dateUtils.formatEndDate(emp, dateUtils.dateToFormattedString) shouldBe "1 February 2016"
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
        worksNumber = "00191048716"
      )

      dateUtils.formatEndDate(emp, dateUtils.dateToFormattedString) shouldBe Messages("lbl.date.no-record")
    }
  }

  "ControllerUtils - getStartDate" must {
    "return default message when there is no start date" in {
      val employmentNoStart = employment.copy(startDate = None)
      dateUtils.formatStartDate(employmentNoStart, dateUtils.dateToFormattedString) shouldBe "No record"
    }

    "return formatted date when there is a start date" in {
      val employmentNoStart = employment.copy(startDate = Some(LocalDate.parse("2016-02-01")))
      dateUtils.formatStartDate(employmentNoStart, dateUtils.dateToFormattedString) shouldBe "1 February 2016"
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
        worksNumber = "00191048716"
      )

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
        worksNumber = "00191048716"
      )

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
        worksNumber = "00191048716"
      )

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
        worksNumber = "00191048716"
      )

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
          worksNumber = "00191048716"
        )

        ControllerUtils.hasEmploymentDetails(emp) shouldBe false
      }
    }
  }
  "ControllerUtils - displaySource "       must {
    "return none when both source amount and amount are the same" in {
      val sourceAmount: Option[BigDecimal] = Some(1)
      val amount: BigDecimal               = 1

      ControllerUtils.displaySource(sourceAmount, amount) shouldBe None
    }

    "return the source amount when both source amount and amount are different" in {
      val sourceAmount: Option[BigDecimal] = Some(2)
      val amount: BigDecimal               = 1

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
      ControllerUtils.displayTaxCode(None)            shouldBe None
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

  "ControllerUtils - displayTaxCodeHeading" must {
    val previous = "Last tax code issued"
    val ongoing  = "Latest tax code issued"

    val currentTaxYearTC = List(
      // Live
      (TaxYear.current.startYear, EmploymentStatus.Live, Some(LocalDate.now().minusDays(1)), previous),
      (TaxYear.current.startYear, EmploymentStatus.Live, Some(LocalDate.now().plusDays(2)), ongoing),
      (TaxYear.current.startYear, EmploymentStatus.Live, None, ongoing),
      // PotentiallyCeased
      (TaxYear.current.startYear, EmploymentStatus.PotentiallyCeased, Some(LocalDate.now().minusDays(1)), previous),
      (TaxYear.current.startYear, EmploymentStatus.PotentiallyCeased, Some(LocalDate.now().plusDays(2)), ongoing),
      (TaxYear.current.startYear, EmploymentStatus.PotentiallyCeased, None, ongoing),
      // Ceased
      (TaxYear.current.startYear, EmploymentStatus.Ceased, Some(LocalDate.now().minusDays(1)), previous),
      (TaxYear.current.startYear, EmploymentStatus.Ceased, Some(LocalDate.now().plusDays(2)), previous),
      (TaxYear.current.startYear, EmploymentStatus.Ceased, None, previous),
      // Unknown
      (TaxYear.current.startYear, EmploymentStatus.Unknown, Some(LocalDate.now().minusDays(1)), previous),
      (TaxYear.current.startYear, EmploymentStatus.Unknown, Some(LocalDate.now().plusDays(2)), ongoing),
      (TaxYear.current.startYear, EmploymentStatus.Unknown, None, ongoing)
    ).sortBy(_._4).reverse

    currentTaxYearTC.foreach { case (taxYear, employmentStatus, employmentEndDate, expectedMessage) =>
      s"return '$expectedMessage' message when the tax year is $taxYear and the employment status is $employmentStatus and end date is $employmentEndDate" in {
        ControllerUtils.displayTaxCodeHeading(taxYear, employmentStatus, employmentEndDate) shouldBe expectedMessage
      }
    }

    val yearsBack = 5
    val statuses  =
      List(EmploymentStatus.Live, EmploymentStatus.PotentiallyCeased, EmploymentStatus.Ceased, EmploymentStatus.Unknown)

    val previousTaxYearsTC =
      statuses.flatMap(status =>
        (1 to yearsBack).map(back =>
          (TaxYear.current.back(back).startYear, status, Some(LocalDate.now().minusDays(1).minusYears(back)), previous)
        )
      ) ++
        statuses.flatMap(status =>
          (1 to yearsBack).map(back =>
            (TaxYear.current.back(back).startYear, status, Some(LocalDate.now().plusDays(2).minusYears(back)), previous)
          )
        ) ++
        statuses.flatMap(status =>
          (1 to yearsBack).map(back => (TaxYear.current.back(back).startYear, status, None, previous))
        )

    previousTaxYearsTC.foreach { case (taxYear, employmentStatus, employmentEndDate, expectedMessage) =>
      s"return '$expectedMessage' message when the tax year is $taxYear and the employment status is $employmentStatus and end date is $employmentEndDate" in {
        ControllerUtils.displayTaxCodeHeading(taxYear, employmentStatus, employmentEndDate) shouldBe expectedMessage
      }
    }

  }
}
