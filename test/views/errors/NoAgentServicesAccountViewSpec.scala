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

package views.errors

import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.html.errors.no_agent_services_account
import views.{BaseViewSpec, Fixture}

class NoAgentServicesAccountViewSpec extends GuiceAppSpec with BaseViewSpec {

  private val titleAndHeadingContent: String = "Your agent business does not have an agent services account"
  private val pContent: String               =
    "If you are authorised to act on behalf of your agent business, you can create an agent services account."

  private val viewHtmlViaApply: HtmlFormat.Appendable    =
    inject[no_agent_services_account].apply()(fakeRequest, messages, appConfig)
  private val viewHtmlViaRender: HtmlFormat.Appendable   =
    inject[no_agent_services_account].render(fakeRequest, messages, appConfig)
  private val viewHtmlViaFunction: HtmlFormat.Appendable =
    inject[no_agent_services_account].f()(fakeRequest, messages, appConfig)

  private class ViewFixture(renderedView: HtmlFormat.Appendable) extends Fixture {
    val view: HtmlFormat.Appendable = renderedView
  }

  "NoAgentServicesAccountView" when {
    def test(method: String, view: HtmlFormat.Appendable): Unit =
      s"$method" should {
        "have the correct title" in new ViewFixture(view) {
          document(this.view).title shouldBe expectedPageTitle(titleAndHeadingContent)
        }

        "have the correct heading" in new ViewFixture(view) {
          document(this.view).select("h1").text() shouldBe titleAndHeadingContent
        }

        "have the correct p element content" in new ViewFixture(view) {
          document(this.view).select("#main-content > div > div > p").text() shouldBe pContent
        }

        "have the correct a element content" in new ViewFixture(view) {
          document(this.view)
            .select(
              "#main-content > div > div > p> a"
            )
            .text() shouldBe "create an agent services account"
        }

        "have the correct a element link" in new ViewFixture(view) {
          document(this.view)
            .select(
              "#main-content > div > div > p> a"
            )
            .attr("href") shouldBe "https://www.gov.uk/guidance/get-an-hmrc-agent-services-account"
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable)] = Seq(
      (".apply", viewHtmlViaApply),
      (".render", viewHtmlViaRender),
      (".f", viewHtmlViaFunction)
    )

    input.foreach(args => (test _).tupled(args))
  }
}
