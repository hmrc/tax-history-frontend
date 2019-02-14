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


import javax.inject.{Inject, Named, Singleton}

import play.api.{Configuration, Environment}
import play.api.Mode.Mode
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.LoadAuditingConfig
import uk.gov.hmrc.play.config.AppName


@Singleton
class FrontendAuditConnector @Inject() (val configuration: Configuration, val environment: Environment) extends AuditConnector {
  override lazy val auditingConfig = LoadAuditingConfig(configuration, environment.mode, s"auditing")
}
