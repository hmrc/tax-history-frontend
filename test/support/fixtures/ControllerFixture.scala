/*
 * Copyright 2023 HM Revenue & Customs
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

package support.fixtures

import model.api.EmploymentPaymentType.OccupationalPension
import model.api._
import models.taxhistory.Person

import java.time.LocalDate
import java.util.UUID

trait ControllerFixture {
  // scalastyle:off magic.number

  val person: Option[Person] = Some(Person(Some("first name"), Some("second name"), Some(false)))

  val employment: Employment = Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = Some(LocalDate.parse("2016-01-21")),
    endDate = Some(LocalDate.parse("2017-01-01")),
    companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
    payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
    employmentPaymentType = None,
    employmentStatus = EmploymentStatus.Live,
    worksNumber = "00191048716"
  )

  val cbUUID: UUID = UUID.randomUUID()

  val companyBenefits: List[CompanyBenefit] = List(
    CompanyBenefit(cbUUID, "EmployerProvidedServices", 1000.00, Some(1), isForecastBenefit = true),
    CompanyBenefit(cbUUID, "CarFuelBenefit", 1000, isForecastBenefit = true)
  )

  val payAndTax: PayAndTax =
    PayAndTax(
      payAndTaxId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
      taxablePayTotal = Some(4896.80),
      taxablePayTotalIncludingEYU = Some(4896.80),
      taxTotal = Some(979.36),
      taxTotalIncludingEYU = Some(979.36),
      studentLoan = Some(1337),
      studentLoanIncludingEYU = None,
      paymentDate = Some(LocalDate.parse("2016-02-20")),
      earlierYearUpdates = List()
    )

  val allowance: Allowance = Allowance(
    allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "EarlierYearsAdjustment",
    amount = BigDecimal(32.00)
  )

  val taxAccount: TaxAccount = TaxAccount(
    taxAccountId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    outstandingDebtRestriction = Some(200),
    underpaymentAmount = Some(300),
    actualPUPCodedInCYPlusOneTaxYear = Some(400)
  )

  val employments: List[Employment] = List(
    employment,
    employment.copy(
      employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae4"),
      employmentPaymentType = Some(OccupationalPension)
    )
  )
  val allowances: List[Allowance]   = List(allowance)

  val statePension: StatePension = StatePension(100, "test")

  val payAndTaxFixedUUID: Map[String, PayAndTax] = Map(
    "01318d7c-bcd9-47e2-8c38-551e7ccdfae3" ->
      PayAndTax(
        payAndTaxId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(12.34),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(56.78),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(LocalDate.parse("2016-02-20")),
        earlierYearUpdates = List.empty
      ),
    "01318d7c-bcd9-47e2-8c38-551e7ccdfae4" ->
      PayAndTax(
        payAndTaxId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae4"),
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(90.12),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(34.56),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(LocalDate.parse("2016-02-20")),
        earlierYearUpdates = List.empty
      )
  )

  val payAndTaxRandomUUID: Map[String, PayAndTax] = Map(
    UUID.randomUUID().toString ->
      PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(12.34),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(56.78),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(LocalDate.parse("2016-02-20")),
        earlierYearUpdates = List.empty
      ),
    UUID.randomUUID().toString ->
      PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(90.12),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(34.56),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(LocalDate.parse("2016-02-20")),
        earlierYearUpdates = List.empty
      )
  )
}
