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

import model.api.{EmploymentStatus, StatePension}
import models.taxhistory.Person
import org.joda.time.LocalDate
import org.jsoup.nodes.Element
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import uk.gov.hmrc.time.TaxYear
import utils.DateHelper._
import utils.{Currency, TestUtil}
import views.html.taxhistory.employment_summary
import views.{BaseViewSpec, Fixture}

class employment_summarySpec extends GuiceAppSpec with BaseViewSpec with Constants {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest("GET", "/tax-history/client-income-record").withCSRFToken

  trait ViewFixture extends Fixture {
    val nino: String = TestUtil.randomNino.toString()
    val currentTaxYear: Int = TaxYear.current.currentYear
    val cyMinus1: Int = TaxYear.current.previous.currentYear
    val cyMinus2:Int = TaxYear.current.previous.currentYear - 1
    val person: Option[Person] = Some(Person(Some("James"), Some("Dean"), Some(false)))
  }

  "employment_summary view" must {

    "have correct title and heading" in new ViewFixture {

      val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, currentTaxYear, employments, allowances, person, None, None, None)

      doc.title mustBe expectedPageTitle(messages("employmenthistory.title"))
      doc.getElementById("taxYearRange").text must be(Messages("employmenthistory.taxyear", currentTaxYear.toString,
        (currentTaxYear + 1).toString))

      val viewDetailsElements: Element = doc.getElementById("view-link-employment-0")
      viewDetailsElements.html must include(Messages("employmenthistory.view") +
        " <span class=\"govuk-visually-hidden\">" + Messages("employmenthistory.view.record.hidden", "James Dean", "employer-2") + "</span>")

      val viewDetailsElementsNoRecord: Element = doc.getElementById("view-employment-2")
      viewDetailsElementsNoRecord.html must include(Messages("lbl.none"))

      val viewPensionElements: Element = doc.getElementById("view-pension-0")
      viewPensionElements.attr("href") mustBe "/tax-history/single-record"
      viewPensionElements.html must include(Messages("employmenthistory.view") +
        " <span class=\"govuk-visually-hidden\">" + Messages("employmenthistory.view.record.hidden", "James Dean", "employer-1") + "</span>")

    }
    "Show nino when no name is present" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino,cyMinus1,employments,allowances,None,taxAccount,None,None)
      doc.getElementsMatchingOwnText(Messages("employmenthistory.header", nino)).hasText mustBe true
      doc.getElementsByClass("grey no-bottom-margin").size() shouldBe 0
    }

    "have correct employment content" in new ViewFixture {

      val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employments, allowances, None, None, None, None)

      doc.getElementsMatchingOwnText(Messages("employmenthistory.table.header.employments")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.table.header.pensions")).hasText mustBe true
      employments.foreach(emp => {
        doc.getElementsContainingOwnText(emp.employerName).hasText mustBe true
        doc.getElementsContainingOwnText(formatDate(emp.startDate.get)).hasText mustBe true
        if (emp.employmentStatus == EmploymentStatus.PotentiallyCeased) {
          doc.getElementsMatchingOwnText(Messages("lbl.date.no-record")).hasText mustBe true
        } else {
          doc.getElementsMatchingOwnText(emp.endDate.fold(Messages("lbl.end-date.ongoing"))
          (d => formatDate(d))).hasText mustBe true
        }
      })

      allowances.foreach(al => {
        doc.getElementsContainingOwnText(Messages(s"employmenthistory.al.${al.iabdType}")).hasText mustBe true
      })
      val caveatParagraphs: String = doc.getElementsByClass("govuk-inset-text").text

      caveatParagraphs.contains(Messages("employmenthistory.caveat.p1.text")) mustBe true
      caveatParagraphs.contains(Messages("employmenthistory.caveat.p2.text")) mustBe true
    }

    "have correct tax account content when a populated TaxAccount is provided" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employments, allowances, person, taxAccount, None, None)

      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.underpayment-amount.title",
        s"${TaxYear.current.previous.currentYear}", s"${TaxYear.current.previous.finishYear}")).hasText mustBe true
      doc.getElementsContainingOwnText(oDR).hasText mustBe true

      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.potential-underpayment.title",
        s"${TaxYear.current.previous.currentYear}", s"${TaxYear.current.previous.finishYear}")).hasText mustBe true

      doc.getElementsContainingOwnText(uA).hasText mustBe true

      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.outstanding.debt.title",
        s"${TaxYear.current.previous.currentYear}", s"${TaxYear.current.previous.finishYear}")).hasText mustBe true
      doc.getElementsContainingOwnText(aPC).hasText mustBe true

      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.outstanding.debt.text")).hasText mustBe true
      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.header")).hasText mustBe true
      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.text")).hasText mustBe true

      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"

    }
  }

  "Show state pensions when clients receiving them for the first time in the current year" in new ViewFixture {
    val startDate: LocalDate = LocalDate.now().withYear(currentTaxYear)
    val sp: StatePension = StatePension(100, "test", Some(1), Some(startDate))
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, currentTaxYear, employments, allowances, None, taxAccount,Some(sp), None)
    doc.getElementsContainingOwnText("State Pension").hasText mustBe true
    val weeklyP1: String = Messages("employmenthistory.state.pensions.text.weekly.p1", "£1.92", formatDate(startDate))
    val weeklyP2: String = Messages("employmenthistory.state.pensions.text.weekly.p2",  formatDate(LocalDate.now()), s"${Currency.fromOptionBD(sp.getAmountReceivedTillDate(currentTaxYear))}")
    doc.getElementsContainingOwnText(weeklyP1).hasText mustBe true
    doc.getElementsContainingOwnText(weeklyP2).hasText mustBe true
  }

  "Show state pensions when clients receiving them yearly" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employments, allowances, None, taxAccount, Some(StatePension(100, "test", Some(5))), None)

    doc.getElementsContainingOwnText("State Pension").hasText mustBe true
    doc.getElementsContainingOwnText("Your client's State Pension this tax year was £100.00").hasText mustBe true
  }

  "Don't show state pensions when they don't have them" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employments, allowances, None, taxAccount, None, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.state.pensions")).hasText mustBe false
    doc.getElementsContainingOwnText(Messages("employmenthistory.state.pensions.text", "£100.00")).hasText mustBe false
  }

  "Show allowances when they exist" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employments, allowances, None, taxAccount, None, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe true
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.description")).hasText mustBe true
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.no-allowances")).hasText mustBe false
  }

  "Show no allowances notice when they do not exist" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employments, List.empty, None, taxAccount, None, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe true
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.description")).hasText mustBe false
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.no-allowances")).hasText mustBe true
  }

  "Don't show allowances for current year" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, currentTaxYear, employments, List.empty, None, taxAccount, None, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe false
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.description")).hasText mustBe false
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.no-allowances")).hasText mustBe false
  }

  "show alternative text instead of employments table when they are no employment records only pensions" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, 2016, employmentWithPensionOnly, allowances, None, taxAccount, None, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.employment.records")).hasText mustBe true
    doc.getElementsContainingOwnText(Messages("employmenthistory.no.employments")).hasText mustBe true
  }

  "show alternative text instead of pensions table when they are no pensions records" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, 2016, employmentsNoPensions, allowances, None, taxAccount, None, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.table.header.pensions")).hasText mustBe true
    doc.getElementsContainingOwnText(Messages("employmenthistory.no.pensions")).hasText mustBe true
  }


  "Show the what's this link when the allowance is an early year adjustment" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, 2016, employments, allowances, None, taxAccount, None,  None)
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowances.eya.summary.header")).hasText mustBe true
  }

  "Don't show the what's this link when there is no early year adjustment" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, 2016, employments, allowancesNoEYA, None, taxAccount, None, None)
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowances.eya.summary.header")).hasText mustBe false
  }

  "Show correct total amounts " in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employmentWithPensions, List.empty, None, taxAccount, None, Some(totalIncome))
    doc.getElementById("pensionIncome").text() shouldBe s"£${totalIncome.pensionTaxablePayTotalIncludingEYU.toString()}"
    doc.getElementById("employmentIncomeTax").text() shouldBe s"£${totalIncome.employmentTaxTotalIncludingEYU.toString()}"
    doc.getElementById("employmentIncome").text() shouldBe s"£${totalIncome.employmentTaxablePayTotalIncludingEYU.toString()}"
    doc.getElementById("pensionIncomeTax").text() shouldBe s"£${totalIncome.pensionTaxTotalIncludingEYU.toString()}"
  }

  "Show error message when total amounts are zero" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employmentWithPensions, List.empty, None, taxAccount, None, None)
    doc.getElementById("employmentIncomeEmpty").text() shouldBe Messages("employmenthistory.employment.table.error.no-values")
  }

  "Show underpaid tax and debts tab in current year minus 1" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employmentWithPensions, List.empty, None, taxAccount, None, None)
    doc.getElementsContainingOwnText(Messages("employmenthistory.employment.summary.tab.3")).size shouldBe 2
  }

  "Show underpaid tax and debts tab in current year minus 1 with error text when there is no data" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus1, employmentWithPensions, List.empty, None, None, None, None)
    doc.getElementsContainingOwnText(Messages("employmenthistory.employment.summary.tab.3")).size shouldBe 2
    doc.getElementById("no-tax-account").text() shouldBe Messages("employmenthistory.tax-account.empty.text")
  }

  "Not show underpaid tax and debts tab in current year" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, currentTaxYear, employmentWithPensions, List.empty, None, None, None, None)
    doc.getElementsContainingOwnText(Messages("employmenthistory.employment.summary.tab.3")).size shouldBe 0
  }

  "Not show underpaid tax and debts tab for current year minus 2 or earlier" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus2, employmentWithPensions, List.empty, None, None, None, None)
    doc.getElementsContainingOwnText(Messages("employmenthistory.employment.summary.tab.3")).size shouldBe 1
  }

  "display navigation bar with correct links" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(nino, cyMinus2, employmentWithPensions, List.empty, None, None, None, None)
    doc.getElementById("nav-home").text shouldBe Messages("nav.home")
    doc.getElementById("nav-client").text shouldBe Messages("nav.client")
    doc.getElementById("nav-year").text shouldBe Messages("nav.year")
    doc.getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
  }

}
