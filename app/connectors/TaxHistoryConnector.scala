/*
 * Copyright 2024 HM Revenue & Customs
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

import config.AppConfig
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaxHistoryConnector @Inject() (val appConfig: AppConfig, httpClient: HttpClientV2)(implicit
  ec: ExecutionContext
) {

  private val taxHistoryUrl                                                      = s"${appConfig.taxHistoryBaseUrl}/tax-history"
  private def employmentsUrl(nino: Nino, taxYear: Int)                           = s"$taxHistoryUrl/$nino/$taxYear/employments"
  private def allowancesUrl(nino: Nino, taxYear: Int)                            = s"$taxHistoryUrl/$nino/$taxYear/allowances"
  private def companyBenefitsUrl(nino: Nino, taxYear: Int, employmentId: String) =
    s"$taxHistoryUrl/$nino/$taxYear/employments/$employmentId/company-benefits"
  private def payAndTaxUrl(nino: Nino, taxYear: Int, employmentId: String)       =
    s"$taxHistoryUrl/$nino/$taxYear/employments/$employmentId/pay-and-tax"
  private def getEmploymentUrl(nino: Nino, taxYear: Int, employmentId: String)   =
    s"$taxHistoryUrl/$nino/$taxYear/employments/$employmentId"
  private def taxYearsUrl(nino: Nino)                                            = s"$taxHistoryUrl/$nino/tax-years"
  private def taxAccountUrl(nino: Nino, taxYear: Int)                            = s"$taxHistoryUrl/$nino/$taxYear/tax-account"
  private def statePensionUrl(nino: Nino, taxYear: Int)                          = s"$taxHistoryUrl/$nino/$taxYear/state-pension"
  private def incomeSourceUrl(nino: Nino, taxYear: Int, employmentId: String)    =
    s"$taxHistoryUrl/$nino/$taxYear/employments/$employmentId/income-source"
  private def allPayAndTaxUrl(nino: Nino, taxYear: Int)                          = s"$taxHistoryUrl/$nino/$taxYear/all-pay-and-tax"

  def getEmploymentsAndPensions(nino: Nino, taxYear: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.get(url"${employmentsUrl(nino, taxYear)}").execute[HttpResponse]

  def getAllowances(nino: Nino, taxYear: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.get(url"${allowancesUrl(nino, taxYear)}").execute[HttpResponse]

  def getCompanyBenefits(nino: Nino, taxYear: Int, employmentId: String)(implicit
    hc: HeaderCarrier
  ): Future[HttpResponse] =
    httpClient.get(url"${companyBenefitsUrl(nino, taxYear, employmentId)}").execute[HttpResponse]

  def getPayAndTaxDetails(nino: Nino, taxYear: Int, employmentId: String)(implicit
    hc: HeaderCarrier
  ): Future[HttpResponse] =
    httpClient.get(url"${payAndTaxUrl(nino, taxYear, employmentId)}").execute[HttpResponse]

  def getEmployment(nino: Nino, taxYear: Int, employmentId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.get(url"${getEmploymentUrl(nino, taxYear, employmentId)}").execute[HttpResponse]

  def getTaxYears(nino: Nino)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.get(url"${taxYearsUrl(nino)}").execute[HttpResponse]

  def getTaxAccount(nino: Nino, taxYear: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.get(url"${taxAccountUrl(nino, taxYear)}").execute[HttpResponse]

  def getStatePension(nino: Nino, taxYear: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.get(url"${statePensionUrl(nino, taxYear)}").execute[HttpResponse]

  def getIncomeSource(nino: Nino, taxYear: Int, employmentId: String)(implicit
    hc: HeaderCarrier
  ): Future[HttpResponse] =
    httpClient.get(url"${incomeSourceUrl(nino, taxYear, employmentId)}").execute[HttpResponse]

  def getAllPayAndTax(nino: Nino, taxYear: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.get(url"${allPayAndTaxUrl(nino, taxYear)}").execute[HttpResponse]

}
