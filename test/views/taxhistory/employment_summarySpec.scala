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

import model.api.EmploymentPaymentType.{JobseekersAllowance, OccupationalPension}
import model.api._
import models.taxhistory.Person
import org.jsoup.nodes.Element
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import uk.gov.hmrc.time.TaxYear
import utils.{Currency, TestUtil}
import views.html.taxhistory.employment_summary
import views.{BaseViewSpec, Fixture}

import java.time.LocalDate
import java.util.UUID

class employment_summarySpec extends GuiceAppSpec with BaseViewSpec with Constants {

  implicit val request: Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/tax-history/client-income-record").withCSRFToken
  val firstName                                         = "testFirstName"
  val surname                                           = "testSurname"
  val now: String                                       = dateUtils.dateToFormattedString(LocalDate.now())

  trait ViewFixture extends Fixture {
    val nino: String           = TestUtil.randomNino.toString()
    val currentTaxYear: Int    = TaxYear.current.currentYear
    val cyMinus1: Int          = TaxYear.current.previous.currentYear
    val cyMinus2: Int          = TaxYear.current.previous.currentYear - 1
    val person: Option[Person] = Some(Person(Some(firstName), Some(surname), Some(false)))
    val grossAmount: Int       = 100
    val taxYear: Int           = 2016
  }

  "employment_summary view" must {

    "have the correct title" in new ViewFixture {
      val view: HtmlFormat.Appendable =
        inject[employment_summary].apply(nino, currentTaxYear, employments, allowances, person, None, None, None, now)
      doc.title mustBe expectedPageTitle(messages("employmenthistory.title"))
    }

    "display a client name as a pre header" in new ViewFixture {
      val view: HtmlFormat.Appendable =
        inject[employment_summary].apply(nino, currentTaxYear, employments, allowances, person, None, None, None, now)
      val preHeaderElement            = doc.getElementById("pre-header")
      val preHeaderWithoutHiddenText  = preHeaderElement.ownText()
      val preHeader                   = preHeaderElement.text()

      preHeaderWithoutHiddenText mustBe s"$firstName $surname"
      preHeader mustBe s"This section relates to $firstName $surname"
    }

    val preHeaderScenarios = List(
      ("there is no client name available", None),
      ("there is only a partial client name available", Some(Person(Some(firstName), None, None)))
    )

    "display a nino as a pre header" when {
      preHeaderScenarios foreach { scenario =>
        scenario._1 in new ViewFixture {
          val view: HtmlFormat.Appendable = inject[employment_summary]
            .apply(nino, cyMinus1, employments, allowances, scenario._2, taxAccount, None, None, now)
          val preHeaderElement            = doc.getElementById("pre-header")
          val preHeaderWithoutHiddenText  = preHeaderElement.ownText()
          val preHeader                   = preHeaderElement.text()

          preHeaderWithoutHiddenText mustBe nino
          preHeader mustBe s"This section relates to $nino"
        }
      }
    }

    "display correct heading" in new ViewFixture {
      val view: HtmlFormat.Appendable =
        inject[employment_summary].apply(nino, currentTaxYear, employments, allowances, person, None, None, None, now)
      heading.text() mustBe messages("employmenthistory.header")

      doc.getElementById("taxYearRange").text must be(
        messages("employmenthistory.taxyear", currentTaxYear.toString, (currentTaxYear + 1).toString)
      )
    }

    "display employments" in new ViewFixture {
      val view: HtmlFormat.Appendable =
        inject[employment_summary].apply(nino, currentTaxYear, employments, allowances, person, None, None, None, now)

      val viewDetailsElements: Element = doc.getElementById("view-link-employment-0")
      viewDetailsElements.html must include(
        "<span aria-hidden=\"true\">" + messages("employmenthistory.view") + "</span> " +
          "<span class=\"govuk-visually-hidden\">" + messages(
            "employmenthistory.view.record.hidden",
            s"$firstName $surname",
            "employer-2"
          ) + "</span>"
      )

      val viewDetailsElementsNoRecord: Element = doc.getElementById("view-employment-2")
      viewDetailsElementsNoRecord.html must include(messages("lbl.none"))

      val viewPensionElements: Element = doc.getElementById("view-pension-0")
      viewPensionElements.attr("href") mustBe "/tax-history/single-record"
      viewPensionElements.html must include(
        "<span aria-hidden=\"true\">" + messages("employmenthistory.view") + "</span> " +
          "<span class=\"govuk-visually-hidden\">" + messages(
            "employmenthistory.view.record.hidden",
            s"$firstName $surname",
            "employer-1"
          ) + "</span>"
      )
    }

    "have correct employment content" in new ViewFixture {

      val view: HtmlFormat.Appendable =
        inject[employment_summary].apply(nino, cyMinus1, employments, allowances, None, None, None, None, now)

      doc.getElementsMatchingOwnText(messages("employmenthistory.table.header.employment")).hasText mustBe true
      doc.getElementsMatchingOwnText(messages("employmenthistory.table.header.pensions")).hasText mustBe true
      employments.foreach { emp =>
        doc.getElementsContainingOwnText(emp.employerName).hasText mustBe true
        doc.getElementsContainingOwnText(dateUtils.dateToFormattedString(emp.startDate.get)).hasText mustBe true

        emp.employmentStatus match {
          case EmploymentStatus.PotentiallyCeased              =>
            doc.getElementsMatchingOwnText(messages("lbl.date.no-record")).hasText mustBe true
          case EmploymentStatus.Live | EmploymentStatus.Ceased =>
            doc
              .getElementsMatchingOwnText(
                emp.endDate.fold(messages("lbl.end-date.ongoing"))(d => dateUtils.dateToFormattedString(d))
              )
              .hasText mustBe true
          case EmploymentStatus.Unknown                        =>
            doc
              .getElementsMatchingOwnText(
                emp.endDate.fold(messages("lbl.date.no-record"))(d => dateUtils.dateToFormattedString(d))
              )
              .hasText mustBe true
        }
      }

      allowances.foreach { al =>
        doc.getElementsContainingOwnText(messages(s"employmenthistory.al.${al.iabdType}")).hasText mustBe true
      }
      val caveatParagraph: String = doc.getElementsByClass("govuk-inset-text").text

      caveatParagraph.contains(messages("employmenthistory.caveat.p.text")) mustBe true
    }

    "have correct tax account content when a populated TaxAccount is provided" in new ViewFixture {
      val view: HtmlFormat.Appendable =
        inject[employment_summary].apply(nino, cyMinus1, employments, allowances, person, taxAccount, None, None, now)

      doc
        .getElementsContainingOwnText(
          messages(
            "employmenthistory.tax-account.underpayment-amount.title",
            s"${TaxYear.current.previous.currentYear}",
            s"${TaxYear.current.previous.finishYear}"
          )
        )
        .hasText mustBe true
      doc
        .getElementsContainingOwnText(messages("employmenthistory.tax-account.underpayment-amount.hint"))
        .hasText mustBe true
      doc.getElementsContainingOwnText(oDR).hasText mustBe true

      doc
        .getElementsContainingOwnText(
          messages(
            "employmenthistory.tax-account.potential-underpayment.title",
            s"${TaxYear.current.previous.currentYear}",
            s"${TaxYear.current.previous.finishYear}",
            s"${TaxYear.current.currentYear}",
            s"${TaxYear.current.finishYear}"
          )
        )
        .hasText mustBe true
      doc
        .getElementsContainingOwnText(
          messages(
            "employmenthistory.tax-account.potential-underpayment.hint",
            s"${TaxYear.current.previous.currentYear}",
            s"${TaxYear.current.previous.finishYear}"
          )
        )
        .hasText mustBe true

      doc.getElementsContainingOwnText(uA).hasText mustBe true

      doc
        .getElementsContainingOwnText(
          messages(
            "employmenthistory.tax-account.outstanding.debt.title",
            s"${TaxYear.current.previous.currentYear}",
            s"${TaxYear.current.previous.finishYear}"
          )
        )
        .hasText mustBe true
      doc.getElementsContainingOwnText(aPC).hasText mustBe true

      doc
        .getElementsContainingOwnText(messages("employmenthistory.tax-account.outstanding.debt.hint"))
        .hasText mustBe true
      doc.getElementsContainingOwnText(messages("employmenthistory.tax-account.header")).hasText mustBe true

      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"

    }
  }

  "Show state pensions when clients receiving them for the first time in the current year" in new ViewFixture {
    val startDate: LocalDate        = LocalDate.now().withYear(currentTaxYear)
    val sp: StatePension            = StatePension(
      grossAmount,
      "test",
      Some(1),
      Some(startDate),
      startDateFormatted = Some(dateUtils.dateToFormattedString(startDate))
    )
    val view: HtmlFormat.Appendable = inject[employment_summary]
      .apply(nino, currentTaxYear, employments, allowances, None, taxAccount, Some(sp), None, now)
    doc.getElementsContainingOwnText("State Pension").hasText mustBe true
    val weeklyP1: String            =
      messages("employmenthistory.state.pensions.text.weekly.p1", "£1.92", dateUtils.dateToFormattedString(startDate))
    val weeklyP2: String            = messages(
      "employmenthistory.state.pensions.text.weekly.p2",
      dateUtils.dateToFormattedString(LocalDate.now()),
      s"${Currency.fromOptionBD(sp.getAmountReceivedTillDate(currentTaxYear))}"
    )
    doc.getElementsContainingOwnText(weeklyP1).hasText mustBe true
    doc.getElementsContainingOwnText(weeklyP2).hasText mustBe true
  }

  "Show state pensions when clients receiving them yearly" in new ViewFixture {
    val paymentFrequency            = 5
    val view: HtmlFormat.Appendable = inject[employment_summary].apply(
      nino,
      cyMinus1,
      employments,
      allowances,
      None,
      taxAccount,
      Some(StatePension(grossAmount, "test", Some(paymentFrequency))),
      None,
      now
    )

    doc.getElementsContainingOwnText("State Pension").hasText mustBe true
    doc.getElementsContainingOwnText("Your client's State Pension this tax year was £100.00").hasText mustBe true
  }

  "Don't show state pensions when they don't have them" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, cyMinus1, employments, allowances, None, taxAccount, None, None, now)

    doc.getElementsContainingOwnText(messages("employmenthistory.state.pensions")).hasText mustBe false
    doc.getElementsContainingOwnText(messages("employmenthistory.state.pensions.text", "£100.00")).hasText mustBe false
  }

  "Show allowances when they exist" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, cyMinus1, employments, allowances, None, taxAccount, None, None, now)

    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.heading")).hasText mustBe true
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.description")).hasText mustBe true
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.inset")).hasText mustBe true
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.no-allowances")).hasText mustBe false
  }

  "Show no allowances notice when they do not exist" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, cyMinus1, employments, List.empty, None, taxAccount, None, None, now)

    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.heading")).hasText mustBe true
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.description")).hasText mustBe false
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.inset")).hasText mustBe false
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.no-allowances")).hasText mustBe true
  }

  "Don't show allowances for current year" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, currentTaxYear, employments, List.empty, None, taxAccount, None, None, now)

    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.heading")).hasText mustBe false
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.description")).hasText mustBe false
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.inset")).hasText mustBe false
    doc.getElementsContainingOwnText(messages("employmenthistory.allowance.no-allowances")).hasText mustBe false
  }

  "show alternative text instead of employments table when they are no employment records only pensions" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary]
      .apply(nino, taxYear, employmentWithPensionOnly, allowances, None, taxAccount, None, None, now)

    doc.getElementsContainingOwnText(messages("employmenthistory.employment.records")).hasText mustBe true
    doc.getElementsContainingOwnText(messages("employmenthistory.no.employments")).hasText mustBe true
  }

  "show alternative text instead of pensions table when they are no pensions records" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary]
        .apply(nino, taxYear, employmentsNoPensions, allowances, None, taxAccount, None, None, now)

    doc.getElementsContainingOwnText(messages("employmenthistory.table.header.pensions")).hasText mustBe true
    doc.getElementsContainingOwnText(messages("employmenthistory.no.pensions")).hasText mustBe true
  }

  "Show the what's this link when the allowance is an early year adjustment" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, taxYear, employments, allowances, None, taxAccount, None, None, now)
    doc.getElementsContainingOwnText(messages("employmenthistory.allowances.eya.summary.header")).hasText mustBe true
  }

  "Don't show the what's this link when there is no early year adjustment" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, taxYear, employments, allowancesNoEYA, None, taxAccount, None, None, now)
    doc.getElementsContainingOwnText(messages("employmenthistory.allowances.eya.summary.header")).hasText mustBe false
  }

  "Show correct total amounts for one employer and two pensions" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary]
      .apply(nino, cyMinus1, employmentWithPensions, List.empty, None, taxAccount, None, Some(totalIncome), now)
    doc.getElementById("pensionIncome").text()    shouldBe s"£${totalIncome.pensionTaxablePayTotalIncludingEYU.toString()}"
    doc
      .getElementById("employmentIncomeTax")
      .text()                                     shouldBe s"£${totalIncome.employmentTaxTotalIncludingEYU.toString()}"
    doc
      .getElementById("employmentIncome")
      .text()                                     shouldBe s"£${totalIncome.employmentTaxablePayTotalIncludingEYU.toString()}"
    doc.getElementById("pensionIncomeTax").text() shouldBe s"£${totalIncome.pensionTaxTotalIncludingEYU.toString()}"
  }

  "Show correct total amounts for two employers and one pension" in new ViewFixture {
    val employer1: Employment       =
      emp1.copy(employmentPaymentType = Some(JobseekersAllowance), employmentId = UUID.randomUUID())
    val employer2: Employment       = emp1.copy(employmentPaymentType = None, employmentId = UUID.randomUUID())
    val pension: Employment         =
      emp1.copy(employmentPaymentType = Some(OccupationalPension), employmentId = UUID.randomUUID())
    val employmentWithPensions      = List(employer1, employer2, pension)
    val incomeTotals: TotalIncome   = totalIncome.copy(employmentIncomeAndTax =
      List(
        // scalastyle:off magic.number
        EmploymentIncomeAndTax(employer1.employmentId.toString, BigDecimal(100), BigDecimal(50)),
        EmploymentIncomeAndTax(employer2.employmentId.toString, BigDecimal(20), BigDecimal(30)),
        EmploymentIncomeAndTax(pension.employmentId.toString, BigDecimal(70), BigDecimal(20))
        // scalastyle:on magic.number
      )
    )
    val view: HtmlFormat.Appendable =
      inject[employment_summary]
        .apply(nino, cyMinus1, employmentWithPensions, List.empty, None, taxAccount, None, Some(incomeTotals), now)

    doc
      .getElementById("pensionIncome")
      .text() shouldBe s"£${incomeTotals.employmentIncomeAndTax.last.income.toString()}"
    doc
      .getElementById("pensionIncomeTax")
      .text() shouldBe s"£${incomeTotals.employmentIncomeAndTax.last.tax.toString()}"
    doc
      .getElementById("employmentIncomeTax")
      .text() shouldBe s"£${totalIncome.employmentTaxTotalIncludingEYU.toString()}"
    doc
      .getElementById("employmentIncome")
      .text() shouldBe s"£${totalIncome.employmentTaxablePayTotalIncludingEYU.toString()}"
    doc
      .getElementById("employmentIncome0")
      .text() shouldBe s"£${incomeTotals.employmentIncomeAndTax.head.income.toString()}"
    doc
      .getElementById("employmentIncomeTax0")
      .text() shouldBe s"£${incomeTotals.employmentIncomeAndTax.head.tax.toString()}"
    doc
      .getElementById("employmentIncome1")
      .text() shouldBe s"£${incomeTotals.employmentIncomeAndTax(1).income.toString()}"
    doc
      .getElementById("employmentIncomeTax1")
      .text() shouldBe s"£${incomeTotals.employmentIncomeAndTax(1).tax.toString()}"
  }

  "Show error message when total amounts are zero" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary]
      .apply(nino, cyMinus1, employmentWithPensions, List.empty, None, taxAccount, None, None, now)
    doc.getElementById("employmentIncome").text()    shouldBe messages("employmenthistory.error.no-record")
    doc.getElementById("employmentIncomeTax").text() shouldBe messages("employmenthistory.error.no-record")
    doc.getElementById("pensionIncome0").text()      shouldBe messages("employmenthistory.error.no-record")
    doc.getElementById("pensionIncomeTax0").text()   shouldBe messages("employmenthistory.error.no-record")
    doc.getElementById("pensionIncome1").text()      shouldBe messages("employmenthistory.error.no-record")
    doc.getElementById("pensionIncomeTax1").text()   shouldBe messages("employmenthistory.error.no-record")
  }

  "Show underpaid tax and debts tab in current year minus 1" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary]
      .apply(nino, cyMinus1, employmentWithPensions, List.empty, None, taxAccount, None, None, now)
    doc.getElementsContainingOwnText(messages("employmenthistory.employment.summary.tab.3")).size shouldBe 2
  }

  "Show underpaid tax and debts tab in current year minus 1 with error text when there is no data" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, cyMinus1, employmentWithPensions, List.empty, None, None, None, None, now)
    doc.getElementsContainingOwnText(messages("employmenthistory.employment.summary.tab.3")).size shouldBe 2
    doc.getElementById("no-tax-account").text()                                                   shouldBe messages("employmenthistory.tax-account.empty.text")
  }

  "Not show underpaid tax and debts tab in current year" in new ViewFixture {
    val view: HtmlFormat.Appendable = inject[employment_summary]
      .apply(nino, currentTaxYear, employmentWithPensions, List.empty, None, None, None, None, now)
    doc.getElementsContainingOwnText(messages("employmenthistory.employment.summary.tab.3")).size shouldBe 0
  }

  "Not show underpaid tax and debts tab for current year minus 2 or earlier" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, cyMinus2, employmentWithPensions, List.empty, None, None, None, None, now)
    doc.getElementsContainingOwnText(messages("employmenthistory.employment.summary.tab.3")).size shouldBe 1
  }

  "display navigation bar with correct links" in new ViewFixture {
    val view: HtmlFormat.Appendable =
      inject[employment_summary].apply(nino, cyMinus2, employmentWithPensions, List.empty, None, None, None, None, now)
    doc.getElementById("nav-home").text         shouldBe messages("nav.home")
    doc.getElementById("nav-client").text       shouldBe messages("nav.client")
    doc.getElementById("nav-year").text         shouldBe messages("nav.year")
    doc.getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
  }

}
