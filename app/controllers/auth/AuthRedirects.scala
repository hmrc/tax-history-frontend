/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.auth

import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.{Configuration, Environment, Mode}

trait AuthRedirects {

  /* Taken from bootstrap-play library and modified for Play >= 2.5 to avoid depending on global configuration/environment
   * and thus not depend on the play-config API which still uses the deprecated globals. */

  def config: Configuration

  def env: Environment

  private lazy val envPrefix =
    if (env.mode.equals(Mode.Test)) {
      "Test"
    } else {
      config
        .getOptional[String]("run.mode")
        .getOrElse("Dev")
    }

  private val hostDefaults: Map[String, String] = Map(
    "Dev.external-url.bas-gateway-frontend.host" -> "http://localhost:9553"
  )

  private def host(): String = {
    val key = s"$envPrefix.external-url.bas-gateway-frontend.host"
    config.getOptional[String](key).orElse(hostDefaults.get(key)).getOrElse("")
  }

  private def ggLoginUrl: String = host() + "/bas-gateway/sign-in"

  private final lazy val defaultOrigin: String =
    config
      .getOptional[String]("sosOrigin")
      .orElse(config.getOptional[String]("appName"))
      .getOrElse("undefined")

  def origin: String = defaultOrigin

  def toGGLogin(continueUrl: String): Result =
    Redirect(
      ggLoginUrl,
      Map(
        "continue_url" -> Seq(continueUrl),
        "origin"       -> Seq(origin)
      )
    )
}
