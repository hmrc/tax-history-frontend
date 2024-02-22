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

package controllers.auth

import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.{Configuration, Environment, Mode}

//TODO try to remove this trait as it is used only to build login URL
// copy pasted from https://github.com/hmrc/bootstrap-play/blob/236897a17b5919d1353cc5695af9e2bd78a5ef28/bootstrap-common-play-28/src/main/scala/uk/gov/hmrc/play/bootstrap/config/AuthRedirects.scala
trait AuthRedirects {

  /* Since this is a library for Play >= 2.5 we avoid depending on global configuration/environment
   * and thus do not depend on the play-config API which still uses the deprecated globals. */

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
    "Dev.external-url.bas-gateway-frontend.host"           -> "http://localhost:9553",
    "Dev.external-url.citizen-auth-frontend.host"          -> "http://localhost:9029",
    "Dev.external-url.identity-verification-frontend.host" -> "http://localhost:9938",
    "Dev.external-url.stride-auth-frontend.host"           -> "http://localhost:9041"
  )

  private def host(service: String): String = {
    val key = s"$envPrefix.external-url.$service.host"
    config.getOptional[String](key).orElse(hostDefaults.get(key)).getOrElse("")
  }

  private def ggLoginUrl: String = host("bas-gateway-frontend") + "/bas-gateway/sign-in"

  private def strideLoginUrl: String = host("stride-auth-frontend") + "/stride/sign-in"

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

  def toStrideLogin(successUrl: String, failureUrl: Option[String] = None): Result =
    Redirect(
      strideLoginUrl,
      Map(
        "successURL" -> Seq(successUrl),
        "origin"     -> Seq(origin)
      ) ++ failureUrl.map(f => Map("failureURL" -> Seq(f))).getOrElse(Map())
    )

}
