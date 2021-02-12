/*
 * Copyright 2021 HM Revenue & Customs
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

package support

import org.scalatest.{Matchers, WordSpec}

class CallOpsSpec extends WordSpec with Matchers {

  "addParamsToUrl" should {
    val url = "http://localhost:9996/tax-history/select-client"

    "return same url when no params provided and " when {
      "no query exists" in {
        CallOps.addParamsToUrl(url) shouldBe url
      }

      "query exists" in {
        CallOps.addParamsToUrl(s"$url?foo=bar") shouldBe s"$url?foo=bar"
      }

      "there are only parameters with no values" in {
        CallOps.addParamsToUrl(url, "foo" -> None) shouldBe url
        CallOps.addParamsToUrl(url, "foo" -> None, "bar" -> None) shouldBe url
      }
    }

    "adds params to existing url" when {
      "there is no query part" in {
        CallOps.addParamsToUrl(url, "foo" -> Some("bar?")) shouldBe s"$url?foo=bar%3F"
      }

      "there is an existing query part" in {
        CallOps.addParamsToUrl(s"$url?foo=bar", "baz" -> Some("qwaggly")) shouldBe s"$url?foo=bar&baz=qwaggly"
      }

      "the url ends with ?" in {
        CallOps.addParamsToUrl(s"$url?", "foo" -> Some("bar")) shouldBe s"$url?foo=bar"
      }

      "the url ends with &" in {
        CallOps.addParamsToUrl(s"$url?foo=bar&", "baz" -> Some("qwaggly")) shouldBe s"$url?foo=bar&baz=qwaggly"
      }

      "one of the parameters to add has no value" in {
        CallOps.addParamsToUrl(url, "foo" -> None, "baz" -> Some("qwaggly")) shouldBe s"$url?baz=qwaggly"
        CallOps.addParamsToUrl(url, "foo" -> Some("bar"), "baz" -> None) shouldBe s"$url?foo=bar"
      }

      "there is an existing query part and one of the parameters to add has no value" in {
        CallOps
          .addParamsToUrl(s"$url?foo=bar", "baz" -> Some("qwaggly"), "fnords" -> None) shouldBe s"$url?foo=bar&baz=qwaggly"
        CallOps
          .addParamsToUrl(s"$url?foo=bar", "fnords" -> None, "baz" -> Some("qwaggly")) shouldBe s"$url?foo=bar&baz=qwaggly"
      }
    }
  }
}
