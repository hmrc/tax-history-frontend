/*
 * Copyright 2023 HM Revenue & Customs
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

  private val viewViaApply: HtmlFormat.Appendable = injected[SignedOut].apply()(
    request = request,
    messages = messages,
    appConfig = appConfig
  )

  private val viewViaRender: HtmlFormat.Appendable = injected[SignedOut].render(
    request = request,
    messages = messages,
    appConfig = appConfig
  )

  private val viewViaF: HtmlFormat.Appendable = injected[SignedOut].f()(fakeRequest, messages, appConfig)

  private class ViewFixture(renderedView: HtmlFormat.Appendable) extends Fixture {
    val view: HtmlFormat.Appendable = renderedView
    val href: String                = "/tax-history/sign-in"
    val linkText: String            = "Sign in"
  }

  "SignedOutView" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" should {
        "have the correct title" in new ViewFixture(view) {
          document(this.view).title shouldBe expectedPageTitle("For your security, we signed you out")
        }

        "have the correct heading" in new ViewFixture(view) {
          document(this.view).select("h1").text() shouldBe "For your security, we signed you out"
        }

        "have the correct signIn link references" in new ViewFixture(view) {
          val aRefText: Element = document(this.view).select(s"a[href=$href]").first()
          aRefText.text.trim() mustBe linkText
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply),
      (".render", viewViaRender),
      (".f", viewViaF)
    )

    input.foreach(args => (test _).tupled(args))
  }
}
