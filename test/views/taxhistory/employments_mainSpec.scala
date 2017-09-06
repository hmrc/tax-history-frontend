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

import models.taxhistory.{Allowance, CompanyBenefit, Employment, PayAsYouEarnDetails, Person}
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import uk.gov.hmrc.urls.Link
import views.{Fixture, GenericTestHelper}

class employments_mainSpec extends GenericTestHelper with MustMatchers {

  val startDate = new LocalDate("2016-01-21")
  val endDate = new LocalDate("2016-11-01")

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino = "AA000000A"
    val taxYear = 2017
    val person = Some(Person(Some("James"),Some("Dean")))

  }

  "employments_main view" must {
    val employments = List(Employment("AA12341234", "Test Employer Name", startDate, None, Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0),
      List(CompanyBenefit("Benifit1", 1000.00), CompanyBenefit("Benifit2", 2000.00))),
      Employment("AA111111", "Test Employer Name", startDate, None, Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0),
        List(CompanyBenefit("Benifit2", 2000.00), CompanyBenefit("Benifit2", 2000.00))))
    val allowance = List(Allowance("desc", 222.00),Allowance("desc1", 333.00))
    val paye = PayAsYouEarnDetails(employments, allowance)


    "have correct title and heading" in new ViewFixture {
      val view = views.html.taxhistory.employments_main( nino, taxYear, paye, None)

      val title = Messages("employmenthistory.title")
      heading.html must be(Messages("employmenthistory.header",nino))
      doc.body().getElementById("taxYear").text() must be(Messages("employmenthistory.taxyear",taxYear+" to "+(taxYear+1)))
      doc.select("script").toString contains
        "ga('send', { hitType: 'event', eventCategory: 'content - view', eventAction: 'TaxHistory', eventLabel: 'EmploymentDetails'}" mustBe true
    }

    "have correct title and heading for the person" in new ViewFixture {
      val view = views.html.taxhistory.employments_main( nino, taxYear, paye, person)

      val title = Messages("employmenthistory.title")
      heading.html must be(Messages("employmenthistory.header","James Dean"))
      doc.body().getElementById("taxYear").text() must be(Messages("employmenthistory.taxyear",taxYear+" to "+(taxYear+1)))
      doc.select("script").toString contains
        "ga('send', { hitType: 'event', eventCategory: 'content - view', eventAction: 'TaxHistory', eventLabel: 'EmploymentDetails'}" mustBe true
    }

    "include employment breakdown" in new ViewFixture {

      val employments = Seq(Employment("AA12341234", "Test Employer Name", startDate, Some(endDate), Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0)))
      val view = views.html.taxhistory.employments_main(nino, taxYear, paye, None)

      val tableRowStartDate = doc.select(".employment-table tbody tr").get(1)
      val tableRowEndDate = doc.select(".employment-table tbody tr").get(2)
      val tableRowPay = doc.select(".employment-table tbody tr").get(4)

      tableRowStartDate.text must include(employments.head.startDate.toString("d MMMM yyyy"))
      tableRowEndDate.text must include(messagesApi("employmenthistory.nopaydata"))
      tableRowPay.text must include(employments.head.taxablePayTotal.get.toString())
      doc.getElementsMatchingOwnText(Messages("lbl.company.benefits")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe true
      doc.getElementsMatchingOwnText(allowance.head.typeDescription).hasText mustBe true
      doc.getElementsMatchingOwnText(allowance.head.amount.toString()).hasText mustBe true
      doc.getElementsMatchingOwnText(allowance.last.typeDescription).hasText mustBe true
      doc.getElementsMatchingOwnText(allowance.last.amount.toString()).hasText mustBe true

    }

    "allow partial employment to be displayed" in new ViewFixture {


      val employments = List(Employment("AA12341234", "Test Employer Name", startDate, Some(endDate), None, None, None, None))
      val paye1 = PayAsYouEarnDetails(employments, List.empty)

      val view = views.html.taxhistory.employments_main(nino, taxYear, paye1, None)

      val tableRowStartDate = doc.select(".employment-table tbody tr").get(1)
      val tableRowEndDate = doc.select(".employment-table tbody tr").get(2)
      val tableRowPay = doc.select(".employment-table tbody tr").get(4)
      val tableRowTax = doc.select(".employment-table tbody tr").get(5)

      tableRowStartDate.text must include(employments.head.startDate.toString("d MMMM yyyy"))
      tableRowEndDate.text must include(employments.head.endDate.get.toString("d MMMM yyyy"))
      tableRowPay.text must include(messagesApi("employmenthistory.nopaydata"))
      tableRowTax.text must include(messagesApi("employmenthistory.nopaydata"))
      doc.getElementsMatchingOwnText(Messages("lbl.company.benefits")).hasText mustBe false
      doc.getElementsMatchingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe false
    }

    "include sidebar links" in new ViewFixture {
      val link = Link.toExternalPage(id=Some("sidebarLink"), url="http://www.google.com", value=Some("Back To Google")).toHtml
      val employments = Seq()

      val view = views.html.taxhistory.employments_main(nino, taxYear, paye, None, Some(link))

      val sideBarLinks = doc.select("#sidebarLink")
      sideBarLinks.size mustBe 1
      val sideBarLink = sideBarLinks.get(0)
      sideBarLink.text must include("Back To Google")
    }
    "include persons first and last name" in new ViewFixture {
      val employments = Seq()
      val view = views.html.taxhistory.employments_main(nino, taxYear, paye, person)
      heading.toString must include("James Dean")

    }
  }

}
