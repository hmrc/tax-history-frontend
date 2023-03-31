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

import org.jsoup.select.Elements
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.html.error_template
import views.{BaseViewSpec, Fixture}

class ErrorTemplateSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  trait ViewFixture extends Fixture {

    val headingText = "error heading"
    val titleText   = "error title"
    val messageText = "error message"

    def view: HtmlFormat.Appendable =
      inject[error_template].apply(titleText, headingText, messageText, gaEventId = Some("unauthorised"))

    def viewHtmlViaRender: HtmlFormat.Appendable =
      inject[error_template].render(
        titleText,
        headingText,
        messageText,
        gaEventId = Some("unauthorised"),
        request,
        messages,
        appConfig
      )

    def viewHtmlViaFunction: HtmlFormat.Appendable =
      inject[error_template].f(
        titleText,
        headingText,
        messageText,
        Some("unauthorised")
      )(request, messages, appConfig)
  }

  "error_template view" must {

    "view .render()" should {

      "render correctly" in new ViewFixture {

        document(viewHtmlViaRender).title mustBe expectedPageTitle(titleText)

        val foundHeading: Elements = document(view).body().select("#error-heading")
        foundHeading.size mustBe 1
        foundHeading.get(0).text() mustBe headingText

        val foundMessage: Elements = document(view).body().select("#error-message")
        foundMessage.size mustBe 1
        foundMessage.get(0).text() mustBe messageText
      }
    }

    "view .f()" when {

      "supplied the correct parameters create the view" in new ViewFixture {

        document(viewHtmlViaFunction).title mustBe expectedPageTitle(titleText)

        val foundHeading: Elements = document(view).body().select("#error-heading")
        foundHeading.size mustBe 1
        foundHeading.get(0).text() mustBe headingText

        val foundMessage: Elements = document(view).body().select("#error-message")
        foundMessage.size mustBe 1
        foundMessage.get(0).text() mustBe messageText
      }
    }

    "have correct title and heading" in new ViewFixture {

      document(view).title mustBe expectedPageTitle(titleText)

      val foundHeading: Elements = document(view).body().select("#error-heading")
      foundHeading.size mustBe 1
      foundHeading.get(0).text() mustBe headingText

      val foundMessage: Elements = document(view).body().select("#error-message")
      foundMessage.size mustBe 1
      foundMessage.get(0).text() mustBe messageText
    }
  }

}
