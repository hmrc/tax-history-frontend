/*
 * Copyright 2017 HM Revenue & Customs
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

import model.api.EmploymentStatus
import models.taxhistory.Person
import play.api.i18n.Messages
import support.GuiceAppSpec
import utils.{DateHelper, TestUtil}
import views.Fixture

class employment_summarySpec extends GuiceAppSpec with Constants {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino = TestUtil.randomNino.toString()
    val taxYear = 2017
    val person = Some(Person(Some("James"), Some("Dean"), false))
  }

  "employment_summary view" must {

    "have correct title and heading" in new ViewFixture {
      val view = views.html.taxhistory.employment_summary(nino, taxYear, employments, allowances, None)

      val title = Messages("employmenthistory.title")
      doc.title mustBe title
      doc.getElementsMatchingOwnText(Messages("employmenthistory.header", nino)).hasText mustBe true
      doc.getElementsByClass("heading-secondary").html must be(Messages("employmenthistory.taxyear", taxYear.toString,
        (taxYear + 1).toString))

      val viewDetailsElements = doc.getElementById("view-employment-0")
      viewDetailsElements.html must include(Messages("employmenthistory.view.record")+" <span class=\"visuallyhidden\">"+Messages("employmenthistory.view.record.hidden",nino,"employer-2")+"</span>")
      viewDetailsElements.attr("href") mustBe "/tax-history/single-record"


      val viewPensionElements = doc.getElementById("view-pension-0")
      viewPensionElements.attr("href") mustBe "/tax-history/single-record"
      viewPensionElements.html must include(Messages("employmenthistory.view.record")+" <span class=\"visuallyhidden\">"+Messages("employmenthistory.view.record.hidden",nino,"employer-1")+"</span>")

      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }

    "have correct employment content" in new ViewFixture {

      val view = views.html.taxhistory.employment_summary(nino, taxYear, employments, allowances, None)

      doc.getElementsMatchingOwnText(Messages("employmenthistory.table.header.employments")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.table.header.pensions")).hasText mustBe true
      employments.foreach(emp => {
        doc.getElementsContainingOwnText(emp.employerName).hasText mustBe true
        doc.getElementsContainingOwnText(DateHelper.formatDate(emp.startDate)).hasText mustBe true
        if (emp.employmentStatus == EmploymentStatus.Live) {
          doc.getElementsMatchingOwnText(emp.endDate.fold(Messages("lbl.current"))
          (d => DateHelper.formatDate(d))).hasText mustBe true
        } else {
          doc.getElementsMatchingOwnText(emp.endDate.fold(Messages("lbl.no.data.available"))
          (d => DateHelper.formatDate(d))).hasText mustBe true
        }
      })

      allowances.foreach(al => {
        doc.getElementsContainingOwnText(Messages(s"employmenthistory.al.${al.iabdType}")).hasText mustBe true
      })
      val caveatParagraphs = doc.select(".panel-border-wide p").text

      caveatParagraphs.contains(Messages("employmenthistory.caveat.p1.text")) mustBe true
      caveatParagraphs.contains(Messages("employmenthistory.caveat.p2.text")) mustBe true
      caveatParagraphs.contains(Messages("employmenthistory.caveat.p3.text")) mustBe true

      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"
    }
  }
}