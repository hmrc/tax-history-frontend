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

import play.api.{Configuration, Environment, Mode}
import play.api.http.HeaderNames.LOCATION
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.test.WithApplication
import support.BaseSpec

import java.io.File

class AuthRedirectsSpec extends BaseSpec {

  trait Dev {
    val mode: Mode = Mode.Dev
  }

  trait Prod {
    val mode: Mode = Mode.Prod
  }

  trait BaseUri {
    val ggLoginService: String = "http://localhost:9553"
    val ggLoginPath: String    = "/bas-gateway/sign-in"
  }

  trait Setup extends WithApplication with BaseUri {
    def mode: Mode

    def extraConfig: Map[String, Any] = Map()

    trait TestRedirects extends AuthRedirects {

      val env: Environment = Environment(new File("."), getClass.getClassLoader, mode)

      val config: Configuration = Configuration.from(
        Map(
          "appName"  -> "app",
          "run.mode" -> mode.toString
        ) ++ extraConfig
      )
    }

    object Redirect extends TestRedirects

    def validate(redirect: Result)(expectedLocation: String): Unit = {
      redirect.header.status            shouldBe SEE_OTHER
      redirect.header.headers(LOCATION) shouldBe expectedLocation
    }
  }

  "AuthRedirects" when {
    "redirecting with defaults from config" should {
      "redirect to GG login in Dev" in new Setup with Dev {
        validate(Redirect.toGGLogin("/continue"))(
          expectedLocation = s"$ggLoginService$ggLoginPath?continue_url=%2Fcontinue&origin=app"
        )
      }

      "redirect to GG login in Prod" in new Setup with Prod {
        validate(Redirect.toGGLogin("/continue"))(
          expectedLocation = s"$ggLoginPath?continue_url=%2Fcontinue&origin=app"
        )
      }

      "allow to override the host defaults" in new Setup with Dev {
        override def extraConfig: Map[String, String] =
          Map("Dev.external-url.bas-gateway-frontend.host" -> "http://localhost:9999")

        validate(Redirect.toGGLogin("/continue"))(
          expectedLocation = s"http://localhost:9999$ggLoginPath?continue_url=%2Fcontinue&origin=app"
        )
      }

      "allow to override the origin default in configuration" in new Setup with Dev {
        override def extraConfig: Map[String, String] = Map("sosOrigin" -> "customOrigin")

        validate(Redirect.toGGLogin("/continue"))(
          expectedLocation = s"$ggLoginService$ggLoginPath?continue_url=%2Fcontinue&origin=customOrigin"
        )
      }

      "allow to override the origin default in code" in new Setup with Dev {
        object CustomRedirect extends TestRedirects {
          override val origin = "customOrigin"
        }

        validate(CustomRedirect.toGGLogin("/continue"))(
          expectedLocation = s"$ggLoginService$ggLoginPath?continue_url=%2Fcontinue&origin=customOrigin"
        )
      }
    }
  }
}
