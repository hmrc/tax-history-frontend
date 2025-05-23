/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier}
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.Future

trait ControllerSpec extends GuiceAppSpec with BaseSpec {

  override lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/").withSession(
      SessionKeys.sessionId -> "SessionId",
      SessionKeys.authToken -> "Token"
    )

  lazy val newEnrolments: Set[Enrolment] =
    Set(
      Enrolment(
        key = "HMRC-AS-AGENT",
        identifiers = Seq(EnrolmentIdentifier("AgentReferenceNumber", "TestArn")),
        state = "",
        delegatedAuthRule = None
      )
    )

  implicit class ViewMatcherHelper(result: Future[Result]) {
    def rendersTheSameViewAs(expected: Html): Unit =
      contentAsString(result) shouldEqual expected.toString
  }
}
