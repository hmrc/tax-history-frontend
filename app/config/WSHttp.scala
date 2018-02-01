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

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import play.api.{Configuration, Environment}
import play.api.Mode.Mode
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.{AppName, RunMode}
import uk.gov.hmrc.play.http.ws._

@ImplementedBy(classOf[WSHttp])
trait WSHttpT extends HttpGet with WSGet
  with HttpPut with WSPut
  with HttpPost with WSPost
  with HttpDelete with WSDelete
  with HttpPatch with WSPatch
  with AppName with RunMode

@Singleton
class WSHttp @Inject() (val environment: Environment, val runModeConfiguration: Configuration, val appNameConfiguration: Configuration) extends WSHttpT {
  val mode: Mode = environment.mode
  override val hooks = NoneRequired
}
