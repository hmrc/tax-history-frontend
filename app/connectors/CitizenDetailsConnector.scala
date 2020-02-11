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

import javax.inject.{Inject, Named, Singleton}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpReads, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CitizenDetailsConnector @Inject()(@Named("citizen-details-baseUrl") baseUrl: URL, httpGet: HttpGet) {

  implicit val httpReads: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse) = response
  }

  private def url(nino: String) = new URL(baseUrl, s"/citizen-details/$nino/designatory-details/basic")

    /**
      * Gets the person details
      */
    def getPersonDetails(nino: Nino)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
      httpGet.GET[HttpResponse](url(nino.value).toString)
    }
  }
