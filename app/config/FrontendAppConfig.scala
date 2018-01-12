/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig

object FrontendAppConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactHost = configuration.getString(s"contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "AgentsForIndividuals"

  override lazy val analyticsToken = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"google-analytics.host")
  lazy val AgentAccountHomePage: String = getString("external-url.agent-account-home-page.url")
  lazy val AgentSubscriptionStart: String = getString("external-url.agent-subscription-start.url")

  override lazy val reportAProblemPartialUrl = getString("reportAProblemPartialUrl")
  override lazy val reportAProblemNonJSUrl = getString("reportAProblemNonJSUrl")

  override lazy val loginUrl = getString("login.url")
  override lazy val logoutUrl = getString("logout.url")
  override lazy val loginContinue = getString("login.continue")
  override lazy val serviceSignOut = loadConfig("service-signout.url")

  override lazy val betaFeedbackUrl = getString("betaFeedbackUrl")
  override lazy val betaFeedbackUnauthenticatedUrl = getString("betaFeedbackUnauthenticatedUrl")


}
