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
import views.html.helpers.link

class LinkSpec extends GuiceAppSpec with BaseViewSpec {

  private val link: String       = "aLink"
  private val messageKey: String = "aMessageKey"
  private val id: String         = "anId"

  private val viewViaApply: Html = injected[link].apply(link, messageKey, Some(id), isExternal = true)

  private val viewViaRender: Html =
    injected[link].render(link, messageKey, Some(id), "govuk-link", isExternal = true, messages)

  private val viewViaF: Html = injected[link].f(link, messageKey, Some(id), "govuk-link", true)(messages)

  private class ViewFixture(renderedView: Html) extends Fixture {
    val view: Html = renderedView
  }

  "Link" when {
    def test(method: String, view: Html): Unit =
      s"$method" should {
        "have the correct link and link text" in new ViewFixture(view) {
          document(this.view).select("a").attr("href") shouldBe link
          document(this.view)
            .select(s"a[href=$link]")
            .text()                                    shouldBe s"${messages(messageKey)} ${messages("site.opensInNewWindowOrTab")}"
        }

        "default to govuk-link class, have the supplied id and the target attribute when isExternal is true" in new ViewFixture(
          view
        ) {
          document(this.view).select("a").attr("id")     shouldBe id
          document(this.view).select("a").attr("target") shouldBe "_blank"
          document(this.view).select("a").attr("class")  shouldBe "govuk-link"
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
