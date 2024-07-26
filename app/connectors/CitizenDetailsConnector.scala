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
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CitizenDetailsConnector @Inject() (val appConfig: AppConfig, httpClient: HttpClientV2)(implicit
  ec: ExecutionContext
) {

  private def url(nino: String) = s"${appConfig.citizenDetailsBaseUrl}/citizen-details/$nino/designatory-details/basic"

  def getPersonDetails(nino: Nino)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient
      .get(url"${url(nino.value)}")
      .execute[HttpResponse]

}
