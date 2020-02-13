/*
 * Copyright 2020 HM Revenue & Customs
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

import java.net.URL
import java.util.UUID

import model.api.EmploymentPaymentType.JobseekersAllowance
import model.api._
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil

import scala.concurrent.Future

class TaxHistoryConnectorSpec extends UnitSpec with MockitoSugar with TestUtil {

  private implicit val hc = HeaderCarrier()

  val startDate = new LocalDate("2016-01-21")

  val mockHttpClient: HttpClient = mock[HttpClient]

  trait LocalSetup {
    lazy val nino =randomNino.toString()
    lazy val connector = new TaxHistoryConnector(new URL("http://localhost"), mockHttpClient)
  }

  private val employment = Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = Some(LocalDate.parse("2016-01-21")),
    endDate = Some(LocalDate.parse("2017-01-01")),
    companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
    payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
    employmentPaymentType = Some(JobseekersAllowance),
    employmentStatus = EmploymentStatus.Live,
    worksNumber = "00191048716")

  "TaxHistoryConnector" should {

    "fetch tax history" in new LocalSetup {
      val employmentsJson = JsArray(Seq(Json.toJson(employment)))
      when(mockHttpClient.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(employmentsJson))))

      val result = await(connector.getEmploymentsAndPensions(Nino(nino), 2017))

      result.status shouldBe Status.OK
      result.json shouldBe employmentsJson
    }

    "fetch allowance for tax history" in new LocalSetup {

      val allowance = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
        iabdType = "allowanceType",
        amount = BigDecimal(12.00))

      when(mockHttpClient.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(allowance))))))

      val result = await(connector.getAllowances(Nino(nino), 2017))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Seq(allowance))

    }

    "fetch company benefits for Employment details" in new LocalSetup {
      val companyBenefits = CompanyBenefit(UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"), "", 200.00, isForecastBenefit = true)

      when(mockHttpClient.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(companyBenefits))))))

      val result = await(connector.getCompanyBenefits(Nino(nino), 2014, "c9923a63-4208-4e03-926d-7c7c88adc7ee"))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Seq(companyBenefits))
    }

    "fetch Employment details from backend" in new LocalSetup {
      lazy val eyuList = List(EarlierYearUpdate(
        earlierYearUpdateId = UUID.fromString("e6926848-818b-4d01-baa1-02111eb0f514"),
        taxablePayEYU = BigDecimal(123.45),
        taxEYU = BigDecimal(67.89),
        receivedDate = new LocalDate("2015-05-29")))

      lazy val payAndTaxWithEyu = PayAndTax(
        payAndTaxId = UUID.fromString("bb1c1ea4-04d0-4285-a2e6-4ade1e57f12a"),
        taxablePayTotal = Some(BigDecimal(1234567.89)),
        taxablePayTotalIncludingEYU = Some(BigDecimal(2345678.90)),
        taxTotal = Some(BigDecimal(2222.22)),
        taxTotalIncludingEYU = Some(BigDecimal(3333.33)),
        studentLoan = None,
        studentLoanIncludingEYU = None,
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = eyuList)

      when(mockHttpClient.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(payAndTaxWithEyu)))))

      val result = await(connector.getPayAndTaxDetails(Nino(nino), 2014, "12341234"))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(payAndTaxWithEyu)
    }

    "fetch Employment from backend" in new LocalSetup {
      val employmentJson = Json.toJson(employment)
      when(mockHttpClient.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(employmentJson))))

      val result = await(connector.getEmployment(Nino(nino), 2014, "12341234"))

      result.status shouldBe Status.OK
      result.json shouldBe employmentJson
    }

    "fetch Tax years from backend" in new LocalSetup {

      val taxYears = List(IndividualTaxYear(2015, "uri1","uri2","uri3"), IndividualTaxYear(2015, "uri1","uri2", "uri3"))

      when(mockHttpClient.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(taxYears)))))

      val result = await(connector.getTaxYears(Nino(nino)))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(taxYears)
    }

    "fetch payAndTax data from backend" in new LocalSetup {

      val payAndTax = PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxablePayTotalIncludingEYU = Some(BigDecimal(3785.70)),
        taxTotal = Some(979.36),
        taxTotalIncludingEYU = Some(BigDecimal(868.25)),
        studentLoan = Some(101.00),
        studentLoanIncludingEYU = Some(101.00),
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = List.empty)

      when(mockHttpClient.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(payAndTax)))))

      val result = await(connector.getPayAndTaxDetails(Nino(nino),2017,"testID"))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(payAndTax)
    }
  }

}
