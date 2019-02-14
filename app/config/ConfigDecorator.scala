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

import javax.inject.{Inject, Singleton}

import play.api.{Configuration, Environment}
import play.api.Mode.Mode
import play.api.i18n.Langs
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class ConfigDecorator @Inject()(val runModeConfiguration: Configuration, val environment: Environment, langs: Langs) extends ServicesConfig {

  val mode: Mode = environment.mode

  lazy val companyAuthHost = runModeConfiguration.getString(s"external-url.company-auth.host").getOrElse("")
  lazy val gg_web_context = runModeConfiguration.getString(s"external-url.gg.web-context").getOrElse("gg")
}
