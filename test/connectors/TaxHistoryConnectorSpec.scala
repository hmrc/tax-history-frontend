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

import config.{ConfigDecorator, FrontendAuthConnector}
import org.mockito.Matchers.{eq => meq}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.play.http.HttpGet
import config.{ConfigDecorator, FrontendAuthConnector}
import controllers.auth.LocalRegime
import models.TaxSummary
import models.taxhistory.Employment
import org.joda.time.DateTime
import org.mockito.Matchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.Application
import play.api.http.Status
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

class TaxHistoryConnectorSpec extends BaseSpec with MockitoSugar with Fixtures {

  val http = mock[HttpGet]

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance(mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[HttpGet].toInstance(http))
    .build()

  trait LocalSetup {

    lazy val connector = {
      injected[TaxHistoryConnector]
    }
  }

  "TaxHistoryConnector" should {

    "fetch tax history" in new LocalSetup {
      when(connector.httpGet.GET[Seq[Employment]](any())).thenReturn(
        Future.successful(Seq(Employment("AA12341234", "Test Employer Name", 25000.0, 2000.0, Some(1000.0), Some(250.0)))))

    }
  }

}
