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
import play.api.i18n.Messages
import support.GuiceAppSpec
import uk.gov.hmrc.urls.Link
import utils.{Currency, DateHelper, TestUtil}
import views.Fixture



trait EmpConstants {

  val startDate = new LocalDate("2016-01-21")
  val endDate = new LocalDate("2016-11-01")

  val companyBenefit1 = CompanyBenefit("Benefit1", 1000.00, "IncomeTaxPaidNotDeductedFromDirectorsRemuneration")
  val companyBenefit2 = CompanyBenefit("Benefit2", 2000.00, "ExpensesPay")
  val EarlierYearUpdateList = List(EarlierYearUpdate(BigDecimal.valueOf(-21.00), BigDecimal.valueOf(-4.56), LocalDate.parse("2017-08-30")))
  val emp1 =  Employment("12341234", "Test Employer Name",  startDate, None, Some(25000.0), Some(2000.0), EarlierYearUpdateList,
    List(companyBenefit1, companyBenefit2))
  val emp2 = Employment("11111111", "Test Employer Name",  startDate, None, Some(25000.0), Some(2000.0), List.empty,
    List(companyBenefit1, companyBenefit2))
  val employments = List(emp1,emp2)
  val allowances = List(Allowance("desc", 222.00, "FlatRateJobExpenses"),
    Allowance("desc1", 333.00,"ProfessionalSubscriptions"),
    Allowance("desc", 222.00, "EarlierYearsAdjustment"))
  val payeCompleteModel = PayAsYouEarnDetails(employments, allowances)

  val partialEmploymentList = List(Employment("12341234", "Test Employer Name", startDate, Some(endDate),None, None))
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
