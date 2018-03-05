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

package views.taxhistory

import model.api.{EmploymentStatus, StatePension}
import models.taxhistory.Person
import org.jsoup.nodes.Element
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import uk.gov.hmrc.time.TaxYear
import utils.{DateHelper, TestUtil}
import views.{Fixture, TestAppConfig}

class employment_summarySpec extends GuiceAppSpec with Constants with TestAppConfig {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino: String = TestUtil.randomNino.toString()
    val taxYear = 2017
    val person = Some(Person(Some("James"), Some("Dean"), Some(false)))
  }

  "employment_summary view" must {

    "have correct title and heading" in new ViewFixture {
      val view = views.html.taxhistory.employment_summary(nino, taxYear, employments, allowances, None, None, None)

      val title = Messages("employmenthistory.title")
      doc.title mustBe title
      doc.getElementsMatchingOwnText(Messages("employmenthistory.header", nino)).hasText mustBe true
      doc.getElementsByClass("pre-heading-small boldFont").html must be(Messages("employmenthistory.taxyear", taxYear.toString,
        (taxYear + 1).toString))

      val viewDetailsElements: Element = doc.getElementById("view-employment-0")
      viewDetailsElements.html must include(Messages("employmenthistory.view") +
        " <span class=\"visuallyhidden\">" + Messages("employmenthistory.view.record.hidden", nino, "employer-2") + "</span>")

     val viewDetailsElementsNoRecord: Element = doc.getElementById("view-employment-2")
     viewDetailsElementsNoRecord.html must include(Messages("lbl.none"))

      val viewPensionElements: Element = doc.getElementById("view-pension-0")
      viewPensionElements.attr("href") mustBe "/tax-history/single-record"
      viewPensionElements.html must include(Messages("employmenthistory.view") +
        " <span class=\"visuallyhidden\">" + Messages("employmenthistory.view.record.hidden", nino, "employer-1") + "</span>")

      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }

    "have correct employment content" in new ViewFixture {

      val view = views.html.taxhistory.employment_summary(nino, taxYear, employments, allowances, None, None, None)

      doc.getElementsMatchingOwnText(Messages("employmenthistory.table.header.employments")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.table.header.pensions")).hasText mustBe true
      employments.foreach(emp => {
        doc.getElementsContainingOwnText(emp.employerName).hasText mustBe true
        doc.getElementsContainingOwnText(DateHelper.formatDate(emp.startDate)).hasText mustBe true
        if (emp.employmentStatus == EmploymentStatus.PotentiallyCeased) {
          doc.getElementsMatchingOwnText(Messages("lbl.end-date.unknown")).hasText mustBe true
        } else {
          doc.getElementsMatchingOwnText(emp.endDate.fold(Messages("lbl.end-date.ongoing"))
          (d => DateHelper.formatDate(d))).hasText mustBe true
        }
      })

      allowances.foreach(al => {
        doc.getElementsContainingOwnText(Messages(s"employmenthistory.al.${al.iabdType}")).hasText mustBe true
      })
      val caveatParagraphs: String = doc.select(".panel-border-wide p").text

      caveatParagraphs.contains(Messages("employmenthistory.caveat.p1.text")) mustBe true
      caveatParagraphs.contains(Messages("employmenthistory.caveat.p2.text")) mustBe true
      caveatParagraphs.contains(Messages("employmenthistory.caveat.p3.text")) mustBe true
    }

    "have correct tax account content when a populated TaxAccount is provided" in new ViewFixture {
      val view = views.html.taxhistory.employment_summary(nino, taxYear, employments, allowances, None, taxAccount, None)

      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.underpayment-amount.title",
        s"${TaxYear.current.previous.currentYear}",s"${TaxYear.current.previous.finishYear}")).hasText mustBe true
      doc.getElementsContainingOwnText(oDR).hasText mustBe true

      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.potential-underpayment.title",
        s"${TaxYear.current.previous.currentYear}",s"${TaxYear.current.previous.finishYear}")).hasText mustBe true

      doc.getElementsContainingOwnText(uA.toString).hasText mustBe true

      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.outstanding.debt.title",
        s"${TaxYear.current.previous.currentYear}",s"${TaxYear.current.previous.finishYear}")).hasText mustBe true
      doc.getElementsContainingOwnText(aPC.toString).hasText mustBe true

      doc.getElementsContainingOwnText(Messages("employmenthistory.tax-account.outstanding.debt.text")).hasText mustBe true

      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"
    }
  }

  "Show state pensions when they have them" in new ViewFixture {
    val view = views.html.taxhistory.employment_summary(nino, 2016, employments, allowances, None, taxAccount, Some(StatePension(100,"test")))

    doc.getElementsContainingOwnText(Messages("employmenthistory.state.pensions")).hasText mustBe true
    doc.getElementsContainingOwnText(Messages("employmenthistory.state.pensions.text", "£100.00")).hasText mustBe true
  }

  "Don't show state pensions when they don't have them" in new ViewFixture {
    val view = views.html.taxhistory.employment_summary(nino, 2016, employments, allowances, None, taxAccount, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.state.pensions")).hasText mustBe false
    doc.getElementsContainingOwnText(Messages("employmenthistory.state.pensions.text", "£100.00")).hasText mustBe false
  }

  "Show allowances when they exist" in new ViewFixture {
    val view = views.html.taxhistory.employment_summary(nino, 2016, employments, allowances, None, taxAccount, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe true
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.description")).hasText mustBe true
  }

  "Don't show allowances when they do not exist" in new ViewFixture {
    val view = views.html.taxhistory.employment_summary(nino, 2016, employments, List.empty, None, taxAccount, None)

    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe false
    doc.getElementsContainingOwnText(Messages("employmenthistory.allowance.description")).hasText mustBe false
  }
}