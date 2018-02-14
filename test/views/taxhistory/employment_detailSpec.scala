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

import models.taxhistory.Person
import org.jsoup.nodes.Element
import play.api.i18n.Messages
import support.GuiceAppSpec
import uk.gov.hmrc.time.TaxYear
import utils.DateHelper._
import utils.TestUtil
import views.{Fixture, TestAppConfig}

class employment_detailSpec extends GuiceAppSpec with DetailConstants with TestAppConfig {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino: String = TestUtil.randomNino.toString()
    val taxYear = 2017
    val person = Person(Some("James"), Some("Dean"), Some(false))
    val clientName: String = person.getName.getOrElse(nino)
  }

  "employment_detail view" must {

    "have correct title, heading and GA pageview event" in new ViewFixture {

      val view = views.html.taxhistory.employment_detail(taxYear, Some(payAndTax),
        employment, List.empty, clientName, actualOrForecast = true)

      val title = Messages("employmenthistory.employment.details.title")
      doc.title mustBe title
      doc.select("h1").text() mustBe "employer-1"
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }

    "have correct employment details" in new ViewFixture {
      val view = views.html.taxhistory.employment_detail(taxYear, Some(payAndTax),
        employment, List.empty, clientName, actualOrForecast = true)
      val payeReference: Element = doc.select("#employment-table tbody tr").get(0)
      val payrollId: Element = doc.select("#employment-table tbody tr").get(1)
      val startDate: Element = doc.select("#employment-table tbody tr").get(2)
      val endDate: Element = doc.select("#employment-table tbody tr").get(3)
      val taxablePay: Element = doc.select("#pay-and-tax-table tbody tr").get(0)
      val incomeTax: Element = doc.select("#pay-and-tax-table tbody tr").get(1)
      val paymentGuidance = Messages("employmenthistory.pay.and.tax.guidance", employment.employerName,
        payAndTax.paymentDate.get.toString("d MMMM yyyy"))

      doc.getElementsContainingOwnText(paymentGuidance).hasText mustBe true
      payrollId.text must include(employment.worksNumber)
      payeReference.text must include(employment.payeReference)
      startDate.text must include(employment.startDate.toString("d MMMM yyyy"))
      endDate.text must include(employment.endDate.get.toString("d MMMM yyyy"))
      taxablePay.text must include("£4,896.8")
      incomeTax.text must include("£979.36")

    }

    "have correct Earlier Year Update details" in new ViewFixture {

      val view = views.html.taxhistory.employment_detail(taxYear, Some(payAndTax),
        employment, List.empty, clientName, actualOrForecast = true)

      val eyuRow0: Element = doc.select("#eyu-table tbody tr").get(0)
      val eyuRow1: Element = doc.select("#eyu-table tbody tr").get(1)
      val eyuCaveat = Messages("employmenthistory.eyu.caveat", (taxYear + 1).toString, taxYear.toString, employment.employerName)

      doc.getElementsContainingOwnText(eyuCaveat).hasText mustBe true
      eyuRow0.text must include("21 January 2016")
      eyuRow0.text must include("£0")
      eyuRow0.text must include("£8.99")

      eyuRow1.text must include("21 May 2016")
      eyuRow1.text must include("£10")
      eyuRow1.text must include("£18.99")

    }

    "have correct company benefits details" in new ViewFixture {

      val view = views.html.taxhistory.employment_detail(taxYear, Some(payAndTax),
        employment, completeCBList, clientName, actualOrForecast = true)

      val caveat_actual = Messages("employmenthistory.company.benefit.caveat.actual",
        clientName, employment.employerName, formatDate(TaxYear(taxYear).starts), formatDate(TaxYear(taxYear).finishes))
      doc.getElementsContainingOwnText(caveat_actual).hasText mustBe true

      completeCBList.foreach(cb => {
        doc.getElementsContainingOwnText(Messages(s"employmenthistory.cb.${cb.iabdType}")).hasText mustBe true
      })
    }

    "show current" when {
      "employment is ongoing" in new ViewFixture {
        val view = views.html.taxhistory.employment_detail(taxYear,
          Some(payAndTax), employmentNoEndDate, completeCBList, clientName, actualOrForecast = false)

        doc.getElementsMatchingOwnText(Messages("lbl.current")).hasText mustBe true

        val caveat_estimate = Messages("employmenthistory.company.benefit.caveat.estimate",
          clientName, employment.employerName, formatDate(TaxYear(taxYear).starts), formatDate(TaxYear(taxYear).finishes))

        doc.getElementsContainingOwnText(caveat_estimate).hasText mustBe true
      }
    }

    "show data not available" when {
      "input data missing for payAndTax and Company benefit" in new ViewFixture {
        val view = views.html.taxhistory.employment_detail(taxYear, None,
          employment, List.empty, clientName, actualOrForecast = true)
        val eyutable: Element = doc.getElementById("eyu-table")
        val cbTable: Element = doc.getElementById("cb-table")
        val taxablePay: Element = doc.select("#pay-and-tax-table tbody tr").get(0)
        taxablePay.text must include(Messages("employmenthistory.nopaydata"))
        doc.getElementsContainingOwnText(Messages("lbl.company.benefits")).hasText mustBe false
        doc.getElementsContainingOwnText(Messages("employmenthistory.eyu.date.received")).hasText mustBe false
        val paymentGuidance = Messages("employmenthistory.pay.and.tax.guidance", employment.employerName, payAndTax.paymentDate.get.toString("d MMMM yyyy"))
        doc.getElementsContainingOwnText(paymentGuidance).hasText mustBe false
      }
    }
  }

}

