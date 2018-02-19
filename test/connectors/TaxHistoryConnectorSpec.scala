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

package connectors

import java.util.UUID

import config.{ConfigDecorator, FrontendAuthConnector, WSHttpT}
import model.api._
import models.taxhistory.Employment
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import support.BaseSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtil

import scala.concurrent.Future

class TaxHistoryConnectorSpec extends BaseSpec with MockitoSugar with TestUtil {

  val http = mock[WSHttpT]
  val startDate = new LocalDate("2016-01-21")

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance(mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[WSHttpT].toInstance(http))
    .build()

  trait LocalSetup {
    lazy val nino =randomNino.toString()
    lazy val connector = {
      injected[TaxHistoryConnector]
    }
  }

  "TaxHistoryConnector" should {

    "fetch tax history" in new LocalSetup {
      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(Employment("12341234", "Test Employer Name",
          startDate, None, Some(25000.0), Some(2000.0))))))))

      val result = await(connector.getEmploymentsAndPensions(Nino(nino), 2017))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Seq(Employment("12341234", "Test Employer Name", startDate, None, Some(25000.0), Some(2000.0))))

    }

    "fetch allowance for tax history" in new LocalSetup {

      val allowance = IabdAllowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
        iabdType = "allowanceType",
        amount = BigDecimal(12.00))

      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(allowance))))))

      val result = await(connector.getAllowances(Nino(nino), 2017))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(Seq(allowance))

    }

    "fetch company benefits for Employment details" in new LocalSetup {
      val companyBenefits = CompanyBenefit(UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"), "", 200.00)

      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(Seq(companyBenefits))))))

      val result = await(connector.getCompanyBenefits(Nino(nino), 2014, "TaxableExpenseBenefit"))

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
        taxTotal = Some(BigDecimal(2222.22)),
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = eyuList)

      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(payAndTaxWithEyu)))))

      val result = await(connector.getCompanyBenefits(Nino(nino), 2014, "TaxableExpenseBenefit"))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(payAndTaxWithEyu)
    }

    "fetch Employment from backend" in new LocalSetup {

      val employment = Employment("12341234", "Test Employer Name",
        startDate, None, Some(25000.0), Some(2000.0))
      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employment)))))

      val result = await(connector.getCompanyBenefits(Nino(nino), 2014, "TaxableExpenseBenefit"))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(employment)
    }

    "fetch Tax years from backend" in new LocalSetup {

      val taxYears = List(IndividualTaxYear(2015, "uri1","uri2","uri3"), IndividualTaxYear(2015, "uri1","uri2", "uri3"))

      when(connector.httpGet.GET[HttpResponse](any())(any(), any(), any())).thenReturn(
        Future.successful(HttpResponse(Status.OK,Some(Json.toJson(taxYears)))))

      val result = await(connector.getTaxYears(Nino(nino)))

      result.status shouldBe Status.OK
      result.json shouldBe Json.toJson(taxYears)
    }
  }

}
