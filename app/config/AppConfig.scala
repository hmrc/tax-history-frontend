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

package config

import java.net.URL

import javax.inject.Inject
import play.api.Environment
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.util.Try


trait AppConfig {
  val authBaseUrl: URL
  val citizenDetailsBaseUrl: URL
  val taxHistoryBaseUrl: URL
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
  val googleTagManagerId: String
}

class DefaultAppConfig @Inject()(val servicesConfig: ServicesConfig, val environment: Environment) extends AppConfig {
  lazy val authBaseUrl = new URL(servicesConfig.baseUrl("auth"))
  lazy val citizenDetailsBaseUrl = new URL(servicesConfig.baseUrl("citizen-details"))
  lazy val taxHistoryBaseUrl = new URL(servicesConfig.baseUrl("tax-history"))
  lazy val contactHost = Try(servicesConfig.getString("contact-frontend.host")).toOption.getOrElse("")
  lazy val serviceSignOut = servicesConfig.getString("service-signout.url")
  lazy val analyticsToken = servicesConfig.getString("google-analytics.token")
  lazy val analyticsHost = servicesConfig.getString("google-analytics.host")
  lazy val agentAccountHomePage = servicesConfig.getString("external-url.agent-account-home-page.url")
  lazy val agentSubscriptionStart = servicesConfig.getString("external-url.agent-subscription-start.url")
  lazy val reportAProblemPartialUrl = servicesConfig.getString("reportAProblemPartialUrl")
  lazy val reportAProblemNonJSUrl = servicesConfig.getString("reportAProblemNonJSUrl")
  lazy val loginUrl = servicesConfig.getString("login.url")
  lazy val logoutUrl = servicesConfig.getString("logout.url")
  lazy val loginContinue = servicesConfig.getString("login.continue")
  lazy val betaFeedbackUrl = servicesConfig.getString("betaFeedbackUrl")
  lazy val betaFeedbackUnauthenticatedUrl = servicesConfig.getString("betaFeedbackUnauthenticatedUrl")
  lazy val agentInvitation = servicesConfig.getString("external-url.agent-invitation.url")
  lazy val agentInvitationFastTrack = servicesConfig.getString("external-url.agent-invitation.fast-track-url")
  lazy val studentLoanFlag = servicesConfig.getBoolean("featureFlags.studentLoanFlag")
  lazy val companyBenefitsFlag = servicesConfig.getBoolean("featureFlags.companyBenefitsFlag")
  lazy val eyaWhatsThisFlag = servicesConfig.getBoolean("featureFlags.eyaWhatsThisFlag")
  lazy val googleTagManagerId = servicesConfig.getString("google-tag-manager.id")

}
