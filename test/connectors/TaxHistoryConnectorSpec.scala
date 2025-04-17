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

package connectors

import model.api.EmploymentPaymentType.JobseekersAllowance
import model.api._
import play.api.http.Status
import play.api.libs.json.{JsArray, JsValue, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import java.util.UUID

class TaxHistoryConnectorSpec extends BaseConnectorSpec {

  private val connector: TaxHistoryConnector = new TaxHistoryConnector(appConfig, mockHttpClient)

  private val taxYear2014: Int = 2014
  private val taxYear2015: Int = 2015
  private val taxYear2017: Int = 2017

  private val employmentId: UUID     = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3")
  private val employment: Employment = Employment(
    employmentId = employmentId,
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = Some(LocalDate.parse("2016-01-21")),
    endDate = Some(LocalDate.parse("2017-01-01")),
    companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
    payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
    employmentPaymentType = Some(JobseekersAllowance),
    employmentStatus = EmploymentStatus.Live,
    worksNumber = "00191048716"
  )

  "TaxHistoryConnector" should {

    "fetch tax history" in {
      val employmentsJson: JsArray = JsArray(Seq(Json.toJson(employment)))

      mockExecuteMethod(employmentsJson.toString(), Status.OK)

      val result: HttpResponse = connector.getEmploymentsAndPensions(Nino(nino), taxYear2017).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe employmentsJson
    }

    "fetch allowance for tax history" in {
      val allowance: Allowance = Allowance(
        allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
        iabdType = "allowanceType",
        amount = BigDecimal(12.00)
      )

      mockExecuteMethod(Json.toJson(Seq(allowance)).toString(), Status.OK)

      val result: HttpResponse = connector.getAllowances(Nino(nino), taxYear2017).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(Seq(allowance))
    }

    "fetch company benefits for Employment details" in {
      val companyBenefits: CompanyBenefit =
        CompanyBenefit(
          companyBenefitId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
          iabdType = "",
          amount = 200.00,
          source = None,
          isForecastBenefit = true
        )

      mockExecuteMethod(Json.toJson(Seq(companyBenefits)).toString(), Status.OK)

      val result: HttpResponse =
        connector.getCompanyBenefits(Nino(nino), taxYear2014, employmentId.toString).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(Seq(companyBenefits))
    }

    "fetch Employment details from backend" in {
      lazy val eyuList: List[EarlierYearUpdate] = List(
        EarlierYearUpdate(
          earlierYearUpdateId = UUID.fromString("e6926848-818b-4d01-baa1-02111eb0f514"),
          taxablePayEYU = BigDecimal(123.45),
          taxEYU = BigDecimal(67.89),
          receivedDate = LocalDate.parse("2015-05-29")
        )
      )

      lazy val payAndTaxWithEyu: PayAndTax = PayAndTax(
        payAndTaxId = UUID.fromString("bb1c1ea4-04d0-4285-a2e6-4ade1e57f12a"),
        taxablePayTotal = Some(BigDecimal(1234567.89)),
        taxablePayTotalIncludingEYU = Some(BigDecimal(2345678.90)),
        taxTotal = Some(BigDecimal(2222.22)),
        taxTotalIncludingEYU = Some(BigDecimal(3333.33)),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(LocalDate.parse("2016-02-20")),
        earlierYearUpdates = eyuList
      )

      mockExecuteMethod(Json.toJson(payAndTaxWithEyu).toString(), Status.OK)

      val result: HttpResponse =
        connector.getPayAndTaxDetails(Nino(nino), taxYear2014, employmentId.toString).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(payAndTaxWithEyu)
    }

    "fetch Employment from backend" in {
      val employmentJson: JsValue = Json.toJson(employment)

      mockExecuteMethod(Json.toJson(employment).toString(), Status.OK)

      val result: HttpResponse = connector.getEmployment(Nino(nino), taxYear2014, employmentId.toString).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe employmentJson
    }

    "fetch Tax years from backend" in {
      val taxYears: List[IndividualTaxYear] =
        List(
          IndividualTaxYear(taxYear2015, "uri1", "uri2", "uri3"),
          IndividualTaxYear(taxYear2015, "uri1", "uri2", "uri3")
        )

      mockExecuteMethod(Json.toJson(taxYears).toString(), Status.OK)

      val result: HttpResponse = connector.getTaxYears(Nino(nino)).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(taxYears)
    }

    "fetch payAndTax data from backend" in {
      val payAndTax: PayAndTax = PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(BigDecimal(3785.70)),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(BigDecimal(868.25)),
        studentLoan = Some(101.00),
        studentLoanIncludingEYU = Some(101.00),
        paymentDate = Some(LocalDate.parse("2016-02-20")),
        earlierYearUpdates = List.empty
      )

      mockExecuteMethod(Json.toJson(payAndTax).toString(), Status.OK)

      val result: HttpResponse =
        connector.getPayAndTaxDetails(Nino(nino), taxYear2017, employmentId.toString).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(payAndTax)
    }

    "fetch getTaxAccount data from the backend" in {
      val taxAccount: TaxAccount = TaxAccount(
        taxAccountId = UUID.randomUUID,
        outstandingDebtRestriction = Some(BigDecimal(100.21)),
        underpaymentAmount = Some(BigDecimal(201.10)),
        actualPUPCodedInCYPlusOneTaxYear = Some(BigDecimal(302.10))
      )

      mockExecuteMethod(Json.toJson(taxAccount).toString(), Status.OK)

      val result: HttpResponse = connector.getTaxAccount(Nino(nino), taxYear2017).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(taxAccount)
    }

    "fetch getStatePension data from the backend" in {
      val statePension: StatePension = StatePension(
        grossAmount = BigDecimal(10000.00),
        typeDescription = "a generic description",
        paymentFrequency = Some(1),
        startDate = Some(LocalDate.now())
      )

      mockExecuteMethod(Json.toJson(statePension).toString(), Status.OK)

      val result: HttpResponse = connector.getStatePension(Nino(nino), taxYear2017).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(statePension)
    }

    "fetch getIncomeSource data from the backend" in {
      val empId = 12345

      val incomeSource: IncomeSource = IncomeSource(
        employmentId = empId,
        employmentType = 1,
        actualPUPCodedInCYPlusOneTaxYear = Some(BigDecimal(10.00)),
        deductions = List(TaDeduction(0, "some description", BigDecimal(10.00), Some(BigDecimal(10.00)))),
        allowances = List(TaAllowance(1, "some other description", BigDecimal(99.00), Some(BigDecimal(99.0)))),
        taxCode = "taxCode",
        basisOperation = Some(2),
        employmentTaxDistrictNumber = 1,
        employmentPayeRef = "employment paye ref"
      )

      mockExecuteMethod(Json.toJson(incomeSource).toString(), Status.OK)

      val result: HttpResponse = connector.getIncomeSource(Nino(nino), taxYear2017, employmentId.toString).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(incomeSource)
    }

    "fetch getAllPayAndTax data from the backend" in {
      val payAndTax: PayAndTax = PayAndTax(
        payAndTaxId = UUID.fromString("bb1c1ea4-04d0-4285-a2e6-4ade1e57f12a"),
        taxablePayTotal = Some(BigDecimal(1234567.89)),
        taxablePayTotalIncludingEYU = Some(BigDecimal(2345678.90)),
        taxTotal = Some(BigDecimal(2222.22)),
        taxTotalIncludingEYU = Some(BigDecimal(3333.33)),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(LocalDate.parse("2016-02-20")),
        earlierYearUpdates = List(
          EarlierYearUpdate(
            UUID.fromString("e6926848-818b-4d01-baa1-02111eb0f514"),
            BigDecimal(123.45),
            taxEYU = BigDecimal(67.89),
            receivedDate = LocalDate.parse("2015-05-29")
          )
        )
      )

      val allPayAndTaxResponse: Map[String, PayAndTax] = Map("first" -> payAndTax)

      mockExecuteMethod(Json.toJson(allPayAndTaxResponse).toString(), Status.OK)

      val result: HttpResponse = connector.getAllPayAndTax(Nino(nino), taxYear2017).futureValue

      result.status shouldBe Status.OK
      result.json   shouldBe Json.toJson(allPayAndTaxResponse)
    }
  }
}
