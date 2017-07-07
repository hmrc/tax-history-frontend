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

package connectors

import javax.inject.{Inject, Singleton}

import config.WSHttpT
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads, HttpResponse}

import scala.concurrent.Future

@Singleton
class TaxHistoryConnector @Inject()(val httpGet: WSHttpT) extends ServicesConfig {

  implicit val httpReads: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse) = response
  }


  def getTaxHistory(nino: Nino, taxYear: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val taxHistoryUrl = s"${baseUrl("tax-history")}/tax-history"

    httpGet.GET[HttpResponse](s"$taxHistoryUrl/$nino/$taxYear")
  }

}
