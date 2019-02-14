/*
 * Copyright 2019 HM Revenue & Customs
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

trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val loginUrl: String
  val logoutUrl: String
  val loginContinue:String
  val serviceSignOut:String
  val betaFeedbackUrl: String
  val betaFeedbackUnauthenticatedUrl: String
  val agentAccountHomePage: String
  val agentSubscriptionStart: String
  val agentInvitation: String
  val agentInvitationFastTrack: String
  val studentLoanFlag: Boolean
  val companyBenefitsFlag: Boolean
  val eyaWhatsThisFlag: Boolean
}

