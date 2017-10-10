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

package controllers

import config.{ConfigDecorator, FrontendAuthConnector}
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import support.{BaseSpec, Fixtures}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.SessionKeys
import utils.TestUtil

class BaseControllerSpec extends BaseSpec with Fixtures with TestUtil {

  lazy val nino = randomNino.toString()

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[FrontendAuthConnector].toInstance( mock[FrontendAuthConnector]))
    .overrides(bind[ConfigDecorator].toInstance(mock[ConfigDecorator]))
    .overrides(bind[TaxHistoryConnector].toInstance(mock[TaxHistoryConnector]))
    .overrides(bind[CitizenDetailsConnector].toInstance(mock[CitizenDetailsConnector]))
    .build()

  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages = messagesApi.preferred(FakeRequest())

  lazy val fakeRequest = FakeRequest("GET", "/").withSession(
    SessionKeys.sessionId -> "SessionId",
    SessionKeys.token -> "Token",
    SessionKeys.userId -> "/auth/oid/tuser",
    SessionKeys.authToken -> ""
  )

  lazy val newEnrolments = Set(
    Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "TestArn")),
      state="",delegatedAuthRule = None)
  )

  lazy val authority = buildFakeAuthority(true)

}
