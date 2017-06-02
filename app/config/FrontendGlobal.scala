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

import play.api.{Application, Configuration, GlobalSettings}
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.audit.http.config.ErrorAuditingSettings
import uk.gov.hmrc.play.config.RunMode
import uk.gov.hmrc.play.frontend.bootstrap.Routing.RemovingOfTrailingSlashes
import uk.gov.hmrc.play.graphite.GraphiteConfig

//TODO - remove this global object
object FrontendGlobal extends GlobalSettings with GraphiteConfig
  with RemovingOfTrailingSlashes with ErrorAuditingSettings with RunMode {

  private lazy val configuration = StaticGlobalDependencies.deps.configuration
  override lazy val auditConnector = StaticGlobalDependencies.deps.frontendAuditConnector

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"microservice.metrics")
  override def appName: String = configuration.getString("appName").getOrElse("APP NAME NOT SET")
}
