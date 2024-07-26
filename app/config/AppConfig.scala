/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class AppConfig @Inject() (val servicesConfig: ServicesConfig) {

  lazy val authBaseUrl: String                    = servicesConfig.baseUrl("auth")
  lazy val citizenDetailsBaseUrl: String          = servicesConfig.baseUrl("citizen-details")
  lazy val taxHistoryBaseUrl: String              = servicesConfig.baseUrl("tax-history")
  lazy val contactHost: String                    = Try(servicesConfig.getString("contact-frontend.host")).toOption.getOrElse("")
  lazy val serviceSignOut: String                 = servicesConfig.getString("service-signout.url")
  lazy val agentAccountHomePage: String           = servicesConfig.getString("external-url.agent-account-home-page.url")
  lazy val agentSubscriptionStart: String         = servicesConfig.getString("external-url.agent-subscription-start.url")
  lazy val loginContinue: String                  = servicesConfig.getString("login.continue")
  lazy val betaFeedbackUrl: String                = servicesConfig.getString("betaFeedbackUrl")
  lazy val betaFeedbackUnauthenticatedUrl: String = servicesConfig.getString("betaFeedbackUnauthenticatedUrl")
  lazy val agentInvitationFastTrack: String       = servicesConfig.getString("external-url.agent-invitation.fast-track-url")
  lazy val studentLoanFlag: Boolean               = servicesConfig.getBoolean("featureFlags.studentLoanFlag")
  lazy val companyBenefitsFlag: Boolean           = servicesConfig.getBoolean("featureFlags.companyBenefitsFlag")
  lazy val eyaWhatsThisFlag: Boolean              = servicesConfig.getBoolean("featureFlags.eyaWhatsThisFlag")
  lazy val gtmContainer: String                   = servicesConfig.getString("tracking-consent-frontend.gtm.container")
  lazy val welshEnabled: Boolean                  = servicesConfig.getBoolean("welsh-enabled")

  lazy val timeout: Int          = servicesConfig.getInt("timeout.timeout")
  lazy val timeoutCountdown: Int = servicesConfig.getInt("timeout.countdown")
}
