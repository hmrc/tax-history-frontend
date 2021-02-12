/*
 * Copyright 2021 HM Revenue & Customs
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

package support

import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.FakeRequest
import play.api.{Application, Mode}

class GuiceAppSpec extends BaseSpec {

  protected val bindModules: Seq[GuiceableModule] = Seq()
  private val additionalConfig = Map(
    "google-analytics.host" -> "host",
    "google-analytics.token" -> "aToken")


  implicit override lazy val app: Application = new GuiceApplicationBuilder().configure(additionalConfig)
    .bindings(bindModules:_*).in(Mode.Test)
    .overrides(bind[TaxHistoryConnector].toInstance(mock[TaxHistoryConnector]))
    .overrides(bind[CitizenDetailsConnector].toInstance(mock[CitizenDetailsConnector]))
    .build()

  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(FakeRequest())

}
