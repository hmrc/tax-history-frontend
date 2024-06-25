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

package views.helpers

import play.api.data.FormError
import play.twirl.api.Html
import support.GuiceAppSpec
import views.{BaseViewSpec, Fixture}
import views.html.helpers.errorSummary

class ErrorSummarySpec extends GuiceAppSpec with BaseViewSpec {

  private val errors: Seq[FormError] = Seq(FormError("", "error message"))

  private val viewViaApply: Html = injected[errorSummary].apply(errors)

  private val viewViaRender: Html = injected[errorSummary].render(errors, None, None, messages)

  private val viewViaF: Html = injected[errorSummary].f(errors, None, None)(messages)

  private class ViewFixture(renderedView: Html) extends Fixture {
    val view: Html = renderedView
  }

  "ErrorSummary" when {
    def test(method: String, view: Html): Unit =
      s"$method" should {
        "have the correct heading" in new ViewFixture(view) {
          document(this.view).select("h2").text() shouldBe messages("error.summary.title")
        }

        "have the correct link and link text" in new ViewFixture(view) {
          document(this.view).select("a").attr("href")   shouldBe "#"
          document(this.view).select("a[href=#]").text() shouldBe "error message"
        }
      }

    val input: Seq[(String, Html)] = Seq(
      (".apply", viewViaApply),
      (".render", viewViaRender),
      (".f", viewViaF)
    )

    input.foreach(args => (test _).tupled(args))
  }
}
