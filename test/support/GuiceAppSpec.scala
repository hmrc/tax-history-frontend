/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.language.LanguageUtils
import utils.DateUtils

class GuiceAppSpec extends BaseSpec {

  protected val bindModules: Seq[GuiceableModule] = Seq()

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[TaxHistoryConnector].toInstance(mock[TaxHistoryConnector]))
    .overrides(bind[CitizenDetailsConnector].toInstance(mock[CitizenDetailsConnector]))
    .overrides(bind[DateUtils].toInstance(mock[DateUtils]))
    .build()

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages       = messagesApi.preferred(FakeRequest())
  lazy val welshMessages: Messages      = messagesApi.preferred(FakeRequest().withTransientLang("cy"))

  lazy val languageUtils: LanguageUtils = injected[LanguageUtils]
  lazy val dateUtils                    = new DateUtils(languageUtils)
}
