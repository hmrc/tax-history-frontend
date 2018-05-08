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

package modules

import javax.inject.Provider

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import config.{AppConfig, WSHttpT}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.ServicesConfig

class LocalGuiceModule(val environment: Environment, val configuration: Configuration) extends AbstractModule {
  override def configure() = {

    bind(classOf[String]).annotatedWith(Names.named("contactFormServiceIdentifier")).toInstance("AgentsForIndividuals") //contactFormServiceIdentifier

    bind(classOf[AppConfig]).toProvider(new Provider[AppConfig] {
      def getConfStringOrThrow(key: String, default: Option[String] = None): String =
        configuration.getString(key).orElse(default).getOrElse(throw new RuntimeException(s"No configuration value found for '$key'"))
      def getConfBooleanOrThrow(key: String): Boolean =
        configuration.getBoolean(key).getOrElse(throw new RuntimeException(s"No configuration value found for '$key'"))


      def get: AppConfig = new AppConfig {
        val contactHost = getConfStringOrThrow("contact-frontend.host", default = Some(""))
        val serviceSignOut = getConfStringOrThrow("service-signout.url")
        val analyticsToken = getConfStringOrThrow("google-analytics.token")
        val analyticsHost = getConfStringOrThrow("google-analytics.host")
        val agentAccountHomePage = getConfStringOrThrow("external-url.agent-account-home-page.url")
        val agentSubscriptionStart = getConfStringOrThrow("external-url.agent-subscription-start.url")
        val reportAProblemPartialUrl = getConfStringOrThrow("reportAProblemPartialUrl")
        val reportAProblemNonJSUrl = getConfStringOrThrow("reportAProblemNonJSUrl")
        val loginUrl = getConfStringOrThrow("login.url")
        val logoutUrl = getConfStringOrThrow("logout.url")
        val loginContinue = getConfStringOrThrow("login.continue")
        val betaFeedbackUrl = getConfStringOrThrow("betaFeedbackUrl")
        val betaFeedbackUnauthenticatedUrl = getConfStringOrThrow("betaFeedbackUnauthenticatedUrl")
        val agentInvitation = getConfStringOrThrow("external-url.agent-invitation.url")
        val agentInvitationFastTrack = getConfStringOrThrow("external-url.agent-invitation.fast-track-url")
        val studentLoanFlag = getConfBooleanOrThrow("featureFlags.studentLoanFlag")
        val companyBenefitsFlag = getConfBooleanOrThrow("featureFlags.companyBenefitsFlag")
        val eyaWhatsThisFlag = getConfBooleanOrThrow("featureFlags.eyaWhatsThisFlag")
      }
    })


    //These library components must be bound in this way, or using providers
    //bind(classOf[HeadersFilter]).to(classOf[HeadersFilter])
  }

  private case class ConfigStringProvider(propertyName: String, default: Option[String] = None) extends Provider[String] {
    lazy val get =
      configuration.getString(propertyName).orElse(default).getOrElse(throw new RuntimeException(s"No configuration value found for '$propertyName'"))
  }
}
