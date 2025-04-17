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

package views.helpers

import play.twirl.api.Html
import support.GuiceAppSpec
import views.{BaseViewSpec, Fixture}
import views.html.helpers.p

class PSpec extends GuiceAppSpec with BaseViewSpec {

  private val id: String    = "anId"
  private val content: Html = Html("test")

  private val viewViaApply: Html = injected[p].apply(content = content, id = Some(id))

  private val viewViaRender: Html = injected[p].render(content, "govuk-body", Some(id))

  private val viewViaF: Html = injected[p].f(content, "govuk-body", Some(id))

  private class ViewFixture(renderedView: Html) extends Fixture {
    val view: Html = renderedView
  }

  "P" when {
    def test(method: String, view: Html): Unit =
      s"$method" should {
        "have the correct content" in new ViewFixture(view) {
          document(this.view).select("p").text() shouldBe content.toString()
        }

        "default to govuk-body class and have the supplied id" in new ViewFixture(view) {
          document(this.view).select("p").attr("id")    shouldBe id
          document(this.view).select("p").attr("class") shouldBe "govuk-body"
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
