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

package config

import play.api.test.FakeRequest
import support.BaseSpec

class AppConfigSpec extends BaseSpec {

  implicit val rq: FakeRequest[_] = FakeRequest()

  "Application configuration" when {

    "contains correct configured values" must {

      "authBaseUrl" in {
        appConfig.authBaseUrl shouldBe "http://localhost:8500"
      }

      "citizenDetailsBaseUrl" in {
        appConfig.citizenDetailsBaseUrl shouldBe "http://localhost:9337"
      }

      "taxHistoryBaseUrl" in {
        appConfig.taxHistoryBaseUrl shouldBe "http://localhost:9997"
      }

      "exitSurveyUrl" in {
        appConfig.exitSurveyUrl shouldBe "http://localhost:9514/feedback/AGENTINDIV"
      }

      "signOutUrl" in {
        appConfig.signOutUrl shouldBe "http://localhost:9553/bas-gateway/sign-out-without-state"
      }

      "agentAccountHomePage" in {
        appConfig.agentAccountHomePage shouldBe "http://localhost:9401/agent-services-account"
      }

      "agentSubscriptionStart" in {
        appConfig.agentSubscriptionStart shouldBe "http://localhost:9437/agent-subscription/start"
      }

      "loginContinue" in {
        appConfig.loginContinue shouldBe "http://localhost:9996/tax-history/select-client"
      }

      "betaFeedbackUrl" in {
        appConfig.betaFeedbackUrl shouldBe "http://localhost:9250/contact/beta-feedback"
      }

      "betaFeedbackUnauthenticatedUrl" in {
        appConfig.betaFeedbackUnauthenticatedUrl shouldBe "http://localhost:9250/contact/beta-feedback-unauthenticated"
      }

      "agentInvitationFastTrack" in {
        appConfig.agentInvitationFastTrack shouldBe "http://localhost:9435/agent-client-relationships/agents/fast-track"
      }

      "gtmContainer" in {
        appConfig.gtmContainer shouldBe "c"
      }

    }
  }

}
