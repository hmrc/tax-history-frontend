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

package config

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.i18n.Langs
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class ConfigDecorator @Inject()(configuration: Configuration, langs: Langs) extends ServicesConfig {

  lazy val companyAuthHost = configuration.getString(s"external-url.company-auth.host").getOrElse("")
  lazy val taxHistoryFrontendHost = configuration.getString(s"external-url.tax-history-frontend.host").getOrElse("")
  lazy val gg_web_context = configuration.getString(s"external-url.gg.web-context").getOrElse("gg")



}