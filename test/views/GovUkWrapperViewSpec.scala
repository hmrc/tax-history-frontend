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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import support.GuiceAppSpec
import views.html.govuk_wrapper
import views.models.PageTitle

class GovUkWrapperViewSpec extends GuiceAppSpec with BaseViewSpec {

  private def viewViaApply(messages: Messages = messages): HtmlFormat.Appendable = injected[govuk_wrapper].apply(
    title = PageTitle("page title")
  )(
    mainContent = Html("<h1>page heading</h1>")
  )(
    request = fakeRequest,
    messages = messages,
    appConfig = appConfig
  )

  private val viewViaRender: HtmlFormat.Appendable = injected[govuk_wrapper].render(
    title = PageTitle("page title"),
    beforeContentHtml = None,
    researchBannerUrl = None,
    signOut = true,
    timeoutActive = true,
    backLink = None,
    mainContent = Html("<h1>page heading</h1>"),
    request = fakeRequest,
    messages = messages,
    appConfig = appConfig
  )

  private val viewViaF: HtmlFormat.Appendable = injected[govuk_wrapper].f(
    PageTitle("page title"),
    None,
    None,
    true,
    true,
    None
  )(Html("<h1>page heading</h1>"))(fakeRequest, messages, appConfig)

  private def document(view: HtmlFormat.Appendable): Document = Jsoup.parse(view.toString())

  private def renderViewTest(method: String, view: HtmlFormat.Appendable): Unit = {
    s"$method" should {
      "display the correct title" in {
        document(view).title shouldBe expectedPageTitle("page title")
      }

      "display the correct heading" in {
        document(view).select("h1").text() shouldBe "page heading"
      }
    }
  }

  private def languageTest(scenario: String, language: String, view: HtmlFormat.Appendable): Unit = {
    s"$scenario" should {
      s"render the html lang as $language" in {
        document(view).select("html").attr("lang") shouldBe language
      }
    }
  }

  "GovUkWrapperView" when {
    renderViewTest(".apply", viewViaApply())
    renderViewTest(".render", viewViaRender)
    renderViewTest(".f", viewViaF)
    languageTest("default language", "en", viewViaApply())
    languageTest("language is toggled to Welsh", "cy", viewViaApply(welshMessages))
  }
}
