/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.{Html, HtmlFormat}
import support.GuiceAppSpec
import views.html.govuk_wrapper
import views.models.PageTitle

class GovUkWrapperViewForceNavigationSpec extends GuiceAppSpec with BaseViewSpec {

  override implicit lazy val app =
    new GuiceApplicationBuilder()
      .configure(
        "welsh-enabled" -> true,
        "play-frontend-hmrc.forceServiceNavigation" -> false
      )
      .build()

  private lazy val wrapper = app.injector.instanceOf[govuk_wrapper]

  private def document(view: HtmlFormat.Appendable): Document =
    Jsoup.parse(view.toString())

  private def view(messages: Messages): HtmlFormat.Appendable =
    wrapper.apply(
      title = PageTitle("page title")
    )(
      mainContent = Html("<h1>page heading</h1>")
    )(
      using request = fakeRequest,
      messages = messages,
      appConfig = app.injector.instanceOf[config.AppConfig]
    )

  "GovUkWrapperView when forceNavigation is false" should {

    "render the Welsh language selector" in {
      val doc = document(view(welshMessages))

      doc.select(".hmrc-language-select").isEmpty shouldBe false
    }

    "render html lang as Welsh" in {
      val doc = document(view(welshMessages))

      doc.select("html").attr("lang") shouldBe "cy"
    }
  }
}
