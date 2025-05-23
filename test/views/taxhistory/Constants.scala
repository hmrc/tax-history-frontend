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

package views.taxhistory

import model.api.EmploymentPaymentType.{JobseekersAllowance, OccupationalPension}
import model.api._
import models.taxhistory.Person

import java.time.LocalDate
import java.util.UUID

trait Constants {

  val startDate: LocalDate = LocalDate.parse("2016-01-21")
  val endDate: LocalDate   = LocalDate.parse("2017-01-01")
  val startDateFormatted   = "21 January 2016"
  val endDateFormatted     = "1 January 2017"

  val emp1LiveOccupationalPension: Employment =
    Employment(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      payeReference = "Paye 1",
      employerName = "Employer 1",
      startDate = Some(startDate),
      endDate = Some(endDate),
      startDateFormatted = Some(startDateFormatted),
      endDateFormatted = Some(endDateFormatted),
      companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
      payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
      employmentPaymentType = Some(OccupationalPension),
      employmentStatus = EmploymentStatus.Live,
      worksNumber = "00191048716"
    )

  val emp2Live: Employment =
    Employment(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      payeReference = "Paye 2",
      employerName = "Employer 2",
      startDate = Some(startDate),
      endDate = None,
      startDateFormatted = Some(startDateFormatted),
      endDateFormatted = Some("Ongoing"),
      companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
      payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
      employmentPaymentType = None,
      employmentStatus = EmploymentStatus.Live,
      worksNumber = "00191048716"
    )

  val emp3Ceased: Employment =
    Employment(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      payeReference = "Paye 3",
      employerName = "Employer 3",
      startDate = Some(startDate),
      startDateFormatted = Some(startDateFormatted),
      endDateFormatted = Some("Ongoing"),
      endDate = None,
      companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
      payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
      employmentPaymentType = None,
      employmentStatus = EmploymentStatus.Ceased,
      worksNumber = "00191048716"
    )

  val emp4UnknownEmploymentStatus: Employment =
    Employment(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      payeReference = "Paye 4",
      employerName = "Employer 4",
      startDate = Some(startDate),
      endDate = None,
      startDateFormatted = Some(startDateFormatted),
      endDateFormatted = Some("No record"),
      companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
      payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
      employmentPaymentType = None,
      employmentStatus = EmploymentStatus.Unknown,
      worksNumber = "00191048716"
    )

  val pension: Employment      =
    Employment(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      payeReference = "Paye 5",
      employerName = "Employer 5",
      startDate = Some(startDate),
      endDate = None,
      companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
      payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
      employmentPaymentType = Some(OccupationalPension),
      employmentStatus = EmploymentStatus.Unknown,
      worksNumber = "00191048716"
    )
  val totalIncome: TotalIncome =
    TotalIncome(
      employmentIncomeAndTax = List(
        EmploymentIncomeAndTax(emp2Live.employmentId.toString, BigDecimal(100), BigDecimal(300)),
        EmploymentIncomeAndTax(pension.employmentId.toString, BigDecimal(100), BigDecimal(200)),
        EmploymentIncomeAndTax(emp1LiveOccupationalPension.employmentId.toString, BigDecimal(100), BigDecimal(200))
      ),
      employmentTaxablePayTotalIncludingEYU = BigDecimal(100),
      pensionTaxablePayTotalIncludingEYU = BigDecimal(200),
      employmentTaxTotalIncludingEYU = BigDecimal(300),
      pensionTaxTotalIncludingEYU = BigDecimal(400)
    )

  val employments: List[Employment] =
    List(emp1LiveOccupationalPension, emp2Live, emp3Ceased, emp4UnknownEmploymentStatus)

  val employmentsWithJobseekers: List[Employment] =
    List(
      emp1LiveOccupationalPension,
      emp1LiveOccupationalPension.copy(employmentPaymentType = Some(JobseekersAllowance)),
      emp2Live,
      emp3Ceased,
      emp4UnknownEmploymentStatus
    )

  val employmentWithPensions: List[Employment]    = List(emp1LiveOccupationalPension, emp2Live, pension)
  val employmentsNoPensions: List[Employment]     = List(emp2Live, emp3Ceased, emp4UnknownEmploymentStatus)
  val employmentWithPensionOnly: List[Employment] = List(pension)

  val allowance1: Allowance = Allowance(
    allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "FlatRateJobExpenses",
    amount = BigDecimal(12.00)
  )
  val allowance2: Allowance = Allowance(
    allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "ProfessionalSubscriptions",
    amount = BigDecimal(22.00)
  )
  val allowance3: Allowance = Allowance(
    allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "EarlierYearsAdjustment",
    amount = BigDecimal(32.00)
  )

  val allowances: List[Allowance]      = List(allowance1, allowance2, allowance3)
  val allowancesNoEYA: List[Allowance] = List(allowance1, allowance2)

  val oDR                            = "101.01"
  val uA                             = "202.00"
  val aPC                            = "301.01"
  val taxAccount: Option[TaxAccount] = Some(
    TaxAccount(UUID.randomUUID(), Some(BigDecimal(oDR)), Some(BigDecimal(uA)), Some(BigDecimal(aPC)))
  )

  val statePension: StatePension =
    StatePension(
      grossAmount = 1.0,
      typeDescription = "test",
      paymentFrequency = None,
      startDate = Some(LocalDate.of(2000, 12, 30))
    )

  val eyu1: EarlierYearUpdate =
    EarlierYearUpdate(
      taxablePayEYU = 0,
      taxEYU = 8.99,
      studentLoanEYU = Some(10.0),
      receivedDate = startDate,
      receivedDateFormatted = Some(startDateFormatted)
    )

  val eyu2: EarlierYearUpdate =
    EarlierYearUpdate(
      taxablePayEYU = 10,
      taxEYU = 18.99,
      receivedDate = LocalDate.parse("2016-05-21"),
      receivedDateFormatted = Some("21 May 2016")
    )

  val eyuList: List[EarlierYearUpdate] = List(eyu1, eyu2)

  val payAndTax: PayAndTax =
    PayAndTax(
      taxablePayTotal = Some(4896.80),
      taxablePayTotalIncludingEYU = Some(4906.80),
      taxTotal = Some(979.36),
      taxTotalIncludingEYU = Some(1007.34),
      studentLoan = Some(101.00),
      studentLoanIncludingEYU = Some(111.0),
      paymentDate = Some(LocalDate.parse("2016-02-20")),
      earlierYearUpdates = eyuList
    )

  val payAndTaxNoTotal: PayAndTax =
    PayAndTax(
      taxablePayTotal = None,
      taxablePayTotalIncludingEYU = None,
      taxTotal = None,
      taxTotalIncludingEYU = None,
      studentLoan = Some(101.00),
      studentLoanIncludingEYU = Some(101.0),
      paymentDate = Some(LocalDate.parse("2016-02-20")),
      earlierYearUpdates = List.empty
    )

  val payAndTaxNoStudentLoan: PayAndTax =
    PayAndTax(
      taxablePayTotal = Some(4896.80),
      taxablePayTotalIncludingEYU = Some(4906.80),
      taxTotal = Some(979.36),
      taxTotalIncludingEYU = Some(1007.34),
      studentLoan = None,
      studentLoanIncludingEYU = None,
      paymentDate = Some(LocalDate.parse("2016-02-20")),
      earlierYearUpdates = eyuList
    )

  val uuid: UUID = UUID.randomUUID()

  val companyBenefit =
    CompanyBenefit(
      companyBenefitId = uuid,
      iabdType = "EmployerProvidedServices",
      amount = 1000.00,
      source = None,
      isForecastBenefit = true
    )

  val completeCBList: List[CompanyBenefit] = List(
    companyBenefit,
    companyBenefit.copy(iabdType = "CarFuelBenefit"),
    companyBenefit.copy(iabdType = "MedicalInsurance"),
    companyBenefit.copy(iabdType = "CarBenefit"),
    companyBenefit.copy(iabdType = "TelephoneBenefit"),
    companyBenefit.copy(iabdType = "ServiceBenefit"),
    companyBenefit.copy(iabdType = "TaxableExpenseBenefit"),
    companyBenefit.copy(iabdType = "VanBenefit"),
    companyBenefit.copy(iabdType = "VanFuelBenefit"),
    companyBenefit.copy(iabdType = "BeneficialLoan"),
    companyBenefit.copy(iabdType = "TotalBenefitInKind"),
    companyBenefit.copy(iabdType = "Accommodation"),
    companyBenefit.copy(iabdType = "Assets"),
    companyBenefit.copy(iabdType = "AssetTransfer"),
    companyBenefit.copy(iabdType = "EducationalService"),
    companyBenefit.copy(iabdType = "Entertaining"),
    companyBenefit.copy(iabdType = "ExpensesPay"),
    companyBenefit.copy(iabdType = "Mileage"),
    companyBenefit.copy(iabdType = "NonQualifyingRelocationExpense"),
    companyBenefit.copy(iabdType = "OtherItems"),
    companyBenefit.copy(iabdType = "PaymentEmployeesBehalf"),
    companyBenefit.copy(iabdType = "PersonalIncidentExpenses"),
    companyBenefit.copy(iabdType = "QualifyingRelocationExpenses"),
    companyBenefit.copy(iabdType = "EmployerProvidedProfessionalSubscription"),
    companyBenefit.copy(iabdType = "IncomeTaxPaidNotDeductedFromDirectorsRemuneration"),
    companyBenefit.copy(iabdType = "TravelAndSubsistence"),
    companyBenefit.copy(iabdType = "VoucherAndCreditCards"),
    companyBenefit.copy(iabdType = "NonCashBenefit")
  )

  val employment: Employment =
    Employment(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      payeReference = "Payroll ID",
      employerName = "employer-1",
      startDate = Some(startDate),
      endDate = Some(endDate),
      startDateFormatted = Some(startDateFormatted),
      endDateFormatted = Some(endDateFormatted),
      companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
      payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
      employmentPaymentType = None,
      employmentStatus = EmploymentStatus.Live,
      worksNumber = "00191048716"
    )

  val employmentWithJobseekers: Employment =
    Employment(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      payeReference = "paye-1",
      employerName = "employer-1",
      startDate = Some(startDate),
      endDate = Some(endDate),
      companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
      payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
      employmentPaymentType = Some(JobseekersAllowance),
      employmentStatus = EmploymentStatus.Live,
      worksNumber = "00191048716"
    )

  val employmentNoEndDate: Employment =
    Employment(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      payeReference = "paye-1",
      employerName = "employer-1",
      startDate = Some(startDate),
      employmentPaymentType = None,
      employmentStatus = EmploymentStatus.Live,
      worksNumber = "00191048716"
    )

  val person: Option[Person] = Some(Person(Some("firstname"), Some("secondname"), deceased = Some(false)))

  val companyBenefits: CompanyBenefit =
    CompanyBenefit(
      iabdType = "allowanceType",
      amount = BigDecimal(1),
      source = None,
      isForecastBenefit = true
    )

}
