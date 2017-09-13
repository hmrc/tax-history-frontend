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

import models.taxhistory._
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import uk.gov.hmrc.urls.Link
import utils.DateHelper
import views.{Fixture, GenericTestHelper}

class employments_mainSpec extends GenericTestHelper with MustMatchers with EmpConstants {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino = "AA000000A"
    val taxYear = 2017
    val person = Some(Person(Some("James"),Some("Dean")))

  }

  "employments_main view" must {

    "have correct title and heading" in new ViewFixture {
      val view = views.html.taxhistory.employments_main( nino, taxYear, payePartialModel, None)

      val title = Messages("employmenthistory.title")
      heading.html must be(Messages("employmenthistory.header",nino))
      doc.body().getElementById("taxYear").text() must be(Messages("employmenthistory.taxyear",taxYear+" to "+(taxYear+1)))
      doc.select("script").toString contains
        "ga('send', { hitType: 'event', eventCategory: 'content - view', eventAction: 'TaxHistory', eventLabel: 'EmploymentDetails'}" mustBe true
    }

    "have correct title and heading for the person" in new ViewFixture {
      val view = views.html.taxhistory.employments_main( nino, taxYear, payePartialModel, person)

      val title = Messages("employmenthistory.title")
      heading.html must be(Messages("employmenthistory.header","James Dean"))
      doc.body().getElementById("taxYear").text() must be(Messages("employmenthistory.taxyear",taxYear+" to "+(taxYear+1)))
      doc.select("script").toString contains
        "ga('send', { hitType: 'event', eventCategory: 'content - view', eventAction: 'TaxHistory', eventLabel: 'EmploymentDetails'}" mustBe true
    }

    "include employment breakdown" in new ViewFixture {

      val view = views.html.taxhistory.employments_main(nino, taxYear, payeCompleteModel, None)

      val tableRowStartDate = doc.select(".employment-table tbody tr").get(1)
      val tableRowEndDate = doc.select(".employment-table tbody tr").get(2)
      val tableRowPay = doc.select(".employment-table tbody tr").get(3)

      tableRowStartDate.text must include(employments.head.startDate.toString("d MMMM yyyy"))
      tableRowEndDate.text must include(messagesApi("employmenthistory.nopaydata"))
      tableRowPay.text must include(employments.head.taxablePayTotal.get.toString())
      doc.getElementsMatchingOwnText(Messages("lbl.company.benefits")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe true
      doc.getElementsMatchingOwnText(allowances.head.amount.toString()).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages(s"employmenthistory.al.${allowances.last.iabdMessageKey}")).hasText mustBe true
      doc.getElementsMatchingOwnText(allowances.last.amount.toString()).hasText mustBe true

      doc.getElementsMatchingOwnText(Messages("employmenthistory.eyu.pay")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.eyu.tax")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.eyu.date.received")).hasText mustBe true
      doc.getElementsMatchingOwnText(employments.head.earlierYearUpdates.head.taxablePayEYU.toString()).hasText mustBe true
      doc.getElementsMatchingOwnText(employments.head.earlierYearUpdates.head.taxEYU.toString()).hasText mustBe true
      doc.getElementsMatchingOwnText(DateHelper.formatDate(employments.head.earlierYearUpdates.head.receivedDate)).hasText mustBe true
    }

    "innclude correct type descriptions of Company benefits and Allowance" in new ViewFixture {
      val view = views.html.taxhistory.employments_main(nino, taxYear, payeModelWithCompleteListOfCBAndAllowances, None)

      val companyBenefits = payeModelWithCompleteListOfCBAndAllowances.employments.head.companyBenefits
      companyBenefits.foreach(cb => {
        doc.getElementsContainingOwnText(Messages(s"employmenthistory.cb.${cb.iabdMessageKey}")).hasText mustBe true
      })

      payeModelWithCompleteListOfCBAndAllowances.allowances.foreach(al => {
        doc.getElementsContainingOwnText(Messages(s"employmenthistory.al.${al.iabdMessageKey}")).hasText mustBe true
      })

    }

    "allow partial employment to be displayed" in new ViewFixture {

      val view = views.html.taxhistory.employments_main(nino, taxYear, payePartialModel, None)
      val tableRowStartDate = doc.select(".employment-table tbody tr").get(1)
      val tableRowEndDate = doc.select(".employment-table tbody tr").get(2)
      val tableRowPay = doc.select(".employment-table tbody tr").get(3)
      val tableRowTax = doc.select(".employment-table tbody tr").get(4)

      tableRowStartDate.text must include(payePartialModel.employments.head.startDate.toString("d MMMM yyyy"))
      tableRowEndDate.text must include(payePartialModel.employments.head.endDate.get.toString("d MMMM yyyy"))

      tableRowPay.text must include(messagesApi("employmenthistory.nopaydata"))
      tableRowTax.text must include(messagesApi("employmenthistory.nopaydata"))
      doc.getElementsMatchingOwnText(Messages("lbl.company.benefits")).hasText mustBe false
      doc.getElementsMatchingOwnText(Messages("employmenthistory.allowance.heading")).hasText mustBe false
    }

    "include sidebar links" in new ViewFixture {
      val link = Link.toExternalPage(id=Some("sidebarLink"), url="http://www.google.com", value=Some("Back To Google")).toHtml
      val employments = Seq()

      val view = views.html.taxhistory.employments_main(nino, taxYear, payePartialModel, None, Some(link))

      val sideBarLinks = doc.select("#sidebarLink")
      sideBarLinks.size mustBe 1
      val sideBarLink = sideBarLinks.get(0)
      sideBarLink.text must include("Back To Google")
    }

    "include persons first and last name" in new ViewFixture {
      val view = views.html.taxhistory.employments_main(nino, taxYear, payePartialModel, person)
      heading.toString must include("James Dean")
    }
  }
}

trait EmpConstants {

  val startDate = new LocalDate("2016-01-21")
  val endDate = new LocalDate("2016-11-01")

  val companyBenefit1 = CompanyBenefit("Benefit1", 1000.00, "IncomeTaxPaidNotDeductedFromDirectorsRemuneration")
  val companyBenefit2 = CompanyBenefit("Benefit2", 2000.00, "ExpensesPay")
  val EarlierYearUpdateList = List(EarlierYearUpdate(BigDecimal.valueOf(-21.00), BigDecimal.valueOf(-4.56), LocalDate.parse("2017-08-30")))
  val emp1 =  Employment("AA12341234", "Test Employer Name",  startDate, None, Some(25000.0), Some(2000.0), EarlierYearUpdateList,
    List(companyBenefit1, companyBenefit2))
  val emp2 = Employment("AA111111", "Test Employer Name",  startDate, None, Some(25000.0), Some(2000.0), List.empty,
    List(companyBenefit1, companyBenefit2))
  val employments = List(emp1,emp2)
  val allowances = List(Allowance("desc", 222.00, "FlatRateJobExpenses"),
    Allowance("desc1", 333.00,"ProfessionalSubscriptions"),
    Allowance("desc", 222.00, "EarlierYearsAdjustment"))
  val payeCompleteModel = PayAsYouEarnDetails(employments, allowances)

  val partialEmploymentList = List(Employment("AA12341234", "Test Employer Name", startDate, Some(endDate),None, None))
  val payePartialModel = PayAsYouEarnDetails(partialEmploymentList, List.empty)

  val completeCBList = List(CompanyBenefit("Benefit8", 1000.00, "EmployerProvidedServices"),
    CompanyBenefit("Benefit29", 1000.00, "CarFuelBenefit"),
    CompanyBenefit("Benefit30", 1000.00, "MedicalInsurance"),
    CompanyBenefit("Benefit31", 1000.00, "CarBenefit"),
    CompanyBenefit("Benefit32", 1000.00, "TelephoneBenefit"),
    CompanyBenefit("Benefit33", 1000.00, "ServiceBenefit"),
    CompanyBenefit("Benefit34", 1000.00, "TaxableExpenseBenefit"),
    CompanyBenefit("Benefit35", 1000.00, "VanBenefit"),
    CompanyBenefit("Benefit36", 1000.00, "VanFuelBenefit"),
    CompanyBenefit("Benefit37", 1000.00, "BeneficialLoan"),
    CompanyBenefit("Benefit28", 1000.00, "TotalBenefitInKind"),
    CompanyBenefit("Benefit38", 1000.00, "Accommodation"),
    CompanyBenefit("Benefit39", 1000.00, "Assets"),
    CompanyBenefit("Benefit40", 1000.00, "AssetTransfer"),
    CompanyBenefit("Benefit41", 1000.00, "EducationalService"),
    CompanyBenefit("Benefit42", 1000.00, "Entertaining"),
    CompanyBenefit("Benefit43", 1000.00, "ExpensesPay"),
    CompanyBenefit("Benefit44", 1000.00, "Mileage"),
    CompanyBenefit("Benefit45", 1000.00, "NonQualifyingRelocationExpense"),
    CompanyBenefit("Benefit46", 1000.00, "NurseryPlaces"),
    CompanyBenefit("Benefit47", 1000.00, "OtherItems"),
    CompanyBenefit("Benefit48", 1000.00, "PaymentEmployeesBehalf"),
    CompanyBenefit("Benefit49", 1000.00, "PersonalIncidentExpenses"),
    CompanyBenefit("Benefit50", 1000.00, "QualifyingRelocationExpenses"),
    CompanyBenefit("Benefit51", 1000.00, "EmployerProvidedProfessionalSubscription"),
    CompanyBenefit("Benefit52", 1000.00, "IncomeTaxPaidNotDeductedFromDirectorsRemuneration"),
    CompanyBenefit("Benefit53", 1000.00, "TravelAndSubsistence"),
    CompanyBenefit("Benefit54", 1000.00, "VoucherAndCreditCards"),
    CompanyBenefit("Benefit117", 1000.00, "NonCashBenefit")
  )
  val employmentWithCB = List(emp1.copy(companyBenefits = completeCBList))

  val payeModelWithCompleteListOfCBAndAllowances = PayAsYouEarnDetails(employmentWithCB, allowances)

}
