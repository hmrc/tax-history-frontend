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

package views.taxhistory

import model.api.EmploymentPaymentType.OccupationalPension
import model.api.{CompanyBenefit, IncomeSource, TaAllowance, TaDeduction}
import models.taxhistory.Person
import org.jsoup.nodes.Element
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import uk.gov.hmrc.time.TaxYear
import utils.{ControllerUtils, TestUtil}
import views.html.taxhistory.employment_detail
import views.{BaseViewSpec, Fixture}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class employment_detailSpec extends GuiceAppSpec with BaseViewSpec with Constants {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest("GET", "/tax-history/single-record").withCSRFToken

  trait ViewFixture extends Fixture {
    val firstName = "testFirstName"
    val surname = "testSurname"
    val start: String = dateUtils.dateToFormattedString(LocalDate.now())
    val end: String = dateUtils.dateToFormattedString(LocalDate.now())
    val format: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM y")

    val currentTaxYear: Int = TaxYear.current.startYear
    val nino: String = TestUtil.randomNino.toString()
    val taxYear = 2016
    val person: Person = Person(Some(firstName), Some(surname), Some(false))
    val clientName: String = person.getName.getOrElse(nino)
    val incomeSourceNoDeductions: Option[IncomeSource] = Some(IncomeSource(1, 1, None, List.empty, List.empty, "", None, 1, ""))

    val deductions: List[TaDeduction] = List(TaDeduction(1, "test", 1.0, Some(1.0)), TaDeduction(1, "test", 1.0, Some(1.0)))
    val allowances : List[TaAllowance] = List(TaAllowance(1, "test", 1.0, Some(1.0)),TaAllowance(1, "test", 1.0, Some(1.0)))
    val incomeSourceWithDeductions: Option[IncomeSource] = Some(IncomeSource(1, 1, None, deductions, List.empty, "", None, 1, ""))
    val incomeSourceWithdeductionsAndAllowances: Option[IncomeSource] = Some(IncomeSource(1, 1, None, deductions, allowances, "", None, 1, ""))
  }

  "employment_detail view" should {

    "have correct title and headings for an employment" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
        employment, List.empty, clientName, None)
      val preHeaderElement = doc.getElementById("pre-header")
      val preHeaderWithoutHiddenText = preHeaderElement.ownText()
      val preHeader = preHeaderElement.text()

      doc.title shouldBe expectedPageTitle(messages("employmenthistory.employment.details.title"))
      preHeaderWithoutHiddenText mustBe s"$firstName $surname"
      preHeader mustBe s"This section relates to $firstName $surname"
      heading.text() shouldBe employment.employerName
      doc.getElementsContainingOwnText(Messages("employmenthistory.employment.details.caption.pension")).hasText shouldBe false
      doc.getElementsContainingOwnText(Messages("employmenthistory.employment.details.caption.employment")).hasText shouldBe true
    }

    "have correct title and headings for a pension" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
        employment.copy(employmentPaymentType = Some(OccupationalPension)), List.empty, clientName, None)

      val preHeaderElement = doc.getElementById("pre-header")
      val preHeaderWithoutHiddenText = preHeaderElement.ownText()
      val preHeader = preHeaderElement.text()

      doc.title shouldBe expectedPageTitle(messages("employmenthistory.employment.details.title"))
      preHeaderWithoutHiddenText mustBe s"$firstName $surname"
      preHeader mustBe s"This section relates to $firstName $surname"
      heading.text() shouldBe employment.employerName
      doc.getElementsContainingOwnText(Messages("employmenthistory.employment.details.caption.pension")).hasText shouldBe true
      doc.getElementsContainingOwnText(Messages("employmenthistory.employment.details.caption.employment")).hasText shouldBe false
    }

    "have correct employment details" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
        employment, List.empty, clientName, None)
      val payeReference: Element = doc.getElementById("employment-data-desktop").child(1).child(1)
      val payrollId: Element = doc.getElementById("employment-data-desktop").child(1).child(2)
      val startDate: Element = doc.getElementById("employment-data-desktop").child(1).child(3)
      val endDate: Element = doc.getElementById("employment-data-desktop").child(1).child(4)
      val taxablePay: Element = doc.getElementById("pay-and-tax-table").child(0).child(0)
      val incomeTax: Element = doc.getElementById("pay-and-tax-table").child(0).child(1)

      payrollId.text should include(employment.worksNumber)
      payeReference.text should include(employment.payeReference)
      startDate.text should include(employment.startDateFormatted.get)
      endDate.text should include(employment.endDateFormatted.get)
      taxablePay.text should include("£4,906.80")
      incomeTax.text should include("£1,007.34")
    }

    "not have payroll ID and status for a pension" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
        employment.copy(employmentPaymentType = Some(OccupationalPension)), List.empty, clientName, None)
      val startDate: Element = doc.getElementById("employment-data-desktop").child(1).child(1)
      val endDate: Element = doc.getElementById("employment-data-desktop").child(1).child(2)
      val taxablePay: Element = doc.getElementById("pay-and-tax-table").child(0).child(0)
      val incomeTax: Element = doc.getElementById("pay-and-tax-table").child(0).child(1)

      doc.getElementsContainingText(employment.worksNumber).hasText shouldBe false
      doc.getElementsContainingText(ControllerUtils.getEmploymentStatus(employment)).hasText shouldBe false
      startDate.text should include(employment.startDate.get.format(format))
      endDate.text should include(employment.endDate.get.format(format))
      taxablePay.text should include("£4,906.80")
      incomeTax.text should include("£1,007.34")
    }

    "have correct Earlier Year Update details" in new ViewFixture {

      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
        employment, List.empty, clientName, None)

      doc.getElementById("EYUs").child(1).child(0).text shouldEqual Messages("employmenthistory.eyu.caveat", employment.employerName)

      val eyuPayOrTaxRow0: Element = doc.select("#eyu-pay-or-tax-table tbody tr").get(0)
      val eyuPayOrTaxRow1: Element = doc.select("#eyu-pay-or-tax-table tbody tr").get(1)
      eyuPayOrTaxRow0.text should include("21 January 2016")
      eyuPayOrTaxRow0.text should include("£0")
      eyuPayOrTaxRow0.text should include("£8.99")

      eyuPayOrTaxRow1.text should include("21 May 2016")
      eyuPayOrTaxRow1.text should include("£10")
      eyuPayOrTaxRow1.text should include("£18.99")

      val eyuStudentLoanRow0:Element = doc.select("#eyu-student-loan-table tbody tr").get(0)

      eyuStudentLoanRow0.text should include("21 January 2016")
      eyuStudentLoanRow0.text should include("£10")
    }

    "have correct company benefits details" in new ViewFixture {

      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
        employment, completeCBList, clientName, None)

      doc.getElementsContainingOwnText(Messages("employmenthistory.company.benefit.caveat")).hasText shouldBe true

      completeCBList.foreach(cb => {
        doc.getElementsContainingOwnText(Messages(s"employmenthistory.cb.${cb.iabdType}")).hasText shouldBe true
      })
    }

    "show current" when {
      "employment is ongoing" in new ViewFixture {
        val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear,
          Some(payAndTax), employmentNoEndDate, completeCBList, clientName, None)

        doc.getElementsMatchingOwnText(Messages("lbl.employment.status.current")).hasText shouldBe true

        doc.getElementsContainingOwnText(Messages("employmenthistory.company.benefit.caveat")).hasText shouldBe true
      }
    }

    "show data not available" when {
      "input data missing for payAndTax.taxablePayTotal and payAndTax.taxTotal" in new ViewFixture {
        val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTaxNoTotal),
          employment, List.empty, clientName, None)
        val taxablePay: Element = doc.getElementById("pay-and-tax-table").child(0).child(1)
        taxablePay.text should include(Messages("employmenthistory.nopaydata"))
        val paymentGuidance: String = Messages("employmenthistory.pay.and.tax.guidance", employment.employerName, payAndTax.paymentDate.get.format(format))
        doc.getElementsContainingOwnText(paymentGuidance).hasText shouldBe false
      }
    }

    "not show tax code breakdown " when {
      "employment details are for previous year" in new ViewFixture {

        val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
          employment, completeCBList, clientName, None)

        doc.getElementsContainingOwnText(Messages("tax.code.heading")).hasText shouldBe false
        doc.getElementsContainingOwnText(Messages("tax.code.subheading")).hasText shouldBe false
        doc.getElementsContainingOwnText(Messages("tax.code.caveat")).hasText shouldBe false

        doc.getElementsByClass("allowance-table").size() shouldBe 0
        doc.getElementsByClass("deductions-table").size() shouldBe 0

        doc.getElementsContainingOwnText(Messages("tax.code.no.deductions")).hasText shouldBe false
      }
    }

    "Show tax code breakdown with deductions " when {
      "employment details are for current year and there are deductions" in new ViewFixture {

        val view: HtmlFormat.Appendable = inject[employment_detail].apply(currentTaxYear, Some(payAndTax),
          employment, completeCBList, clientName, incomeSourceWithDeductions)

        doc.getElementsContainingOwnText(Messages("tax.code.heading", {
          ""
        })).hasText shouldBe true
        doc.getElementsContainingOwnText(Messages("tax.code.subheading")).hasText shouldBe true
        doc.getElementsContainingOwnText(Messages("tax.code.caveat")).hasText shouldBe true
        doc.getElementsContainingOwnText(Messages("tax.code.no.deductions")).hasText shouldBe false

      }
    }

    "Show tax code breakdown without deductions and show no deductions text " when {
      "employment details are for current year and there are no deductions" in new ViewFixture {

        val view: HtmlFormat.Appendable = inject[employment_detail].apply(currentTaxYear, Some(payAndTax),
          employment, completeCBList, clientName, incomeSourceNoDeductions)

        doc.getElementsContainingOwnText(Messages("tax.code.heading", {
          ""
        })).hasText shouldBe true
        doc.getElementsContainingOwnText(Messages("tax.code.subheading")).hasText shouldBe true
        doc.getElementsContainingOwnText(Messages("tax.code.no.deductions")).hasText shouldBe true


      }
    }

    "Not Show tax code, paye Refrence, payroll id, early year updates, or company benefits" when {
      "The employment is recievingJobseekersAllowance = true" in new ViewFixture {

        val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
          employmentWithJobseekers, completeCBList, clientName, None)

        doc.getElementsContainingOwnText(Messages("employmenthistory.employment.details.eyu")).hasText shouldBe false
        doc.getElementsContainingOwnText(Messages("employmenthistory.employment.details.eyu.caveat")).hasText shouldBe false

        doc.getElementsContainingOwnText(Messages("employmenthistory.employment.details.companybenefits")).hasText shouldBe false

        doc.getElementsContainingOwnText(Messages("lbl.payroll.id")).hasText shouldBe false
        doc.getElementsContainingOwnText(Messages("lbl.paye.reference")).hasText shouldBe false

        doc.getElementsContainingOwnText(Messages("tax.code.heading", {
          ""
        })).hasText shouldBe false
        doc.getElementsContainingOwnText(Messages("tax.code.subheading")).hasText shouldBe false
        doc.getElementsContainingOwnText(Messages("tax.code.caveat")).hasText shouldBe false

        doc.getElementsByClass("allowance-table").size() shouldBe 0
        doc.getElementsByClass("deductions-table").size() shouldBe 0

        doc.getElementsContainingOwnText(Messages("tax.code.no.deductions")).hasText shouldBe false
      }
    }


    "Show company benefit's P11D/forecast descriptor" when {
      "the company benefit's payment is forecast" in new ViewFixture {
        val companyBenefitForecast: CompanyBenefit = completeCBList.head.copy(isForecastBenefit=true)
        val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax), employment, List(companyBenefitForecast), clientName, None)

        val cbTable: Element = doc.getElementById("cb-table")
        cbTable.getElementsContainingOwnText(Messages("employmenthistory.cb.forecast")).hasText shouldBe true
        cbTable.getElementsContainingOwnText(Messages("employmenthistory.cb.P11D")).hasText shouldBe false
      }
      "the company benefit's payment is not forecast" in new ViewFixture {
        val companyBenefitActual: CompanyBenefit = completeCBList.head.copy(isForecastBenefit=false)
        val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax), employment, List(companyBenefitActual), clientName, None)

        val cbTable: Element = doc.getElementById("cb-table")
        cbTable.getElementsContainingOwnText(Messages("employmenthistory.cb.forecast")).hasText shouldBe false
        cbTable.getElementsContainingOwnText(Messages("employmenthistory.cb.P11D")).hasText shouldBe true
      }
    }

    "Show student loans when data is available" in new ViewFixture {

      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
        employmentWithJobseekers, completeCBList, clientName, None)

      doc.getElementsMatchingOwnText(Messages("employmenthistory.student.loans")).hasText shouldBe true
      doc.getElementsMatchingOwnText("£111").hasText shouldBe true
    }

    "Not show student loans when data is available" in new ViewFixture {

      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTaxNoStudentLoan),
        employmentWithJobseekers, completeCBList, clientName, None)

      doc.getElementsMatchingOwnText(Messages("employmenthistory.student.loans")).hasText shouldBe false
    }


    "Show alternate text when payAndTax is not defined" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[employment_detail].apply(currentTaxYear, None,
        employment, completeCBList, clientName, incomeSourceNoDeductions)

      doc.getElementsMatchingOwnText(Messages("employmenthistory.no.pay.and.tax",
        employment.employerName, employment.startDate.get.format(format))).hasText shouldBe true
    }

    "show alternate text when no company benefits are available" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
        employment, List.empty, clientName, None)

      doc.getElementsMatchingOwnText(Messages("employmenthistory.employment.details.no.benefits", "employer-1")).hasText shouldBe true
    }
  }

  "totals for allowance and deduction have the correct values" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
      employment, List.empty, clientName, incomeSourceWithdeductionsAndAllowances)
    doc.getElementById("DeductionTotal").text shouldBe "£2"
    doc.getElementById("AllowanceTotal").text shouldBe "£2"
  }

  "display navigation bar with correct links" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_detail].apply(taxYear, Some(payAndTax),
      employment, List.empty, clientName, incomeSourceWithdeductionsAndAllowances)

    doc.getElementById("nav-home").text shouldBe Messages("nav.home")
    doc.getElementById("nav-client").text shouldBe Messages("nav.client")
    doc.getElementById("nav-year").text shouldBe Messages("nav.year")
    doc.getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
  }
}
