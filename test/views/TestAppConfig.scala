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

package views

import java.net.URL

import config.AppConfig

trait TestAppConfig {
  implicit val appConfig: AppConfig = new AppConfig {
    val analyticsToken: String = "faketoken"
    val analyticsHost: String = "fakehost"
    val reportAProblemPartialUrl: String = "fakeurl"
    val reportAProblemNonJSUrl: String = "fakeurl"
    val loginUrl: String = "fakeurl"
    val logoutUrl: String = "fakeurl"
    val loginContinue: String = "fakeurl"
    val serviceSignOut: String = "fakeurl"
    val betaFeedbackUrl: String = "fakeurl"
    val betaFeedbackUnauthenticatedUrl: String = "fakeurl"
    val agentAccountHomePage: String = "fakeurl"
    val agentSubscriptionStart: String = "fakeurl"
    val agentInvitation: String = "fakeurl"
    val agentInvitationFastTrack: String = "fakeurl"
    val studentLoanFlag: Boolean = true
    val companyBenefitsFlag: Boolean = true
    val eyaWhatsThisFlag: Boolean = true
    val authBaseUrl: URL = new URL("http://localhost")
    val citizenDetailsBaseUrl: URL = new URL("http://localhost")
    val taxHistoryBaseUrl: URL = new URL("http://localhost")
    val googleTagManagerId: String = "fakeurl"
  }
}
