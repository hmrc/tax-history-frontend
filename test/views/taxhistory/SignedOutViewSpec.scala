/*
 * Copyright 2022 HM Revenue & Customs
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

package views.taxhistory

import org.jsoup.nodes.Element
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.html.taxhistory.SignedOut
import views.{BaseViewSpec, Fixture}
import play.api.test.CSRFTokenHelper.CSRFRequest

class SignedOutViewSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/tax-history/we-signed-you-out").withCSRFToken

  trait ViewFixture extends Fixture {
    val href: String     = "/tax-history/sign-in"
    val linkText: String = messages("signedOut.signIn")
  }

  "signedOutView" must {

    "have the correct title" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[SignedOut].apply()
      doc.title shouldBe expectedPageTitle(messages("signedOut.title"))
    }

    "have the correct heading" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[SignedOut].apply()
      doc.select("h1").text() shouldBe messages("signedOut.title")
    }

    "have the correct signIn link references" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[SignedOut].apply()
      val aRefText: Element           = doc.select(s"a[href=$href]").first()
      aRefText.text.trim() mustBe linkText.trim
    }

  }
}
