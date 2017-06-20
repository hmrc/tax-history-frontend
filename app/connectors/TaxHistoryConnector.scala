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

import javax.inject.{Inject,Singleton}

import models.taxhistory.Employment
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.Future

@Singleton
class TaxHistoryConnector @Inject()(val httpGet: HttpGet) extends ServicesConfig {

  def getTaxHistory(nino: Nino, taxYear: Int)(implicit hc: HeaderCarrier): Future[Seq[Employment]] = {

    val taxHistoryUrl = s"${baseUrl("tax-history")}/tax-history"

    httpGet.GET[Seq[Employment]](s"$taxHistoryUrl/$nino/$taxYear")
  }

}
