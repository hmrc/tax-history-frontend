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

import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.html.errors.technical_error
import views.{BaseViewSpec, Fixture}

class TechnicalErrorSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  trait ViewFixture extends Fixture {
    val view: HtmlFormat.Appendable                = inject[technical_error].apply()(request, messages, appConfig)
    val viewHtmlViaRender: HtmlFormat.Appendable   = inject[technical_error].render(request, messages, appConfig)
    val viewHtmlViaFunction: HtmlFormat.Appendable = inject[technical_error].f()(request, messages, appConfig)
  }

  "Technical Error view" when {

    "view.apply()" should {

      "have correct title, heading and GA page view event" in new ViewFixture {

        document(view).title mustBe expectedPageTitle(messages("employmenthistory.technical.error.title"))
        document(view).getElementById("back-link").attr("href").contains(appConfig.agentAccountHomePage) mustBe true
        document(view).getElementById("back-link").text mustBe Messages("lbl.back")
        document(view).select("h1").text() mustBe Messages("employmenthistory.technical.error.header")
        document(view).getElementsMatchingOwnText(Messages("employmenthistory.technical.error.message")).size() mustBe 1
      }
    }

    "view.render()" should {

      "have correct title, heading and GA page view event" in new ViewFixture {

        document(viewHtmlViaRender).title mustBe expectedPageTitle(messages("employmenthistory.technical.error.title"))
        document(viewHtmlViaRender)
          .getElementById("back-link")
          .attr("href")
          .contains(appConfig.agentAccountHomePage) mustBe true
        document(viewHtmlViaRender).getElementById("back-link").text mustBe Messages("lbl.back")
        document(viewHtmlViaRender).select("h1").text() mustBe Messages("employmenthistory.technical.error.header")
        document(viewHtmlViaRender)
          .getElementsMatchingOwnText(Messages("employmenthistory.technical.error.message"))
          .size() mustBe 1
      }
    }

    "view.f()" should {

      "have correct title, heading and GA page view event" in new ViewFixture {

        document(viewHtmlViaFunction).title mustBe expectedPageTitle(
          messages("employmenthistory.technical.error.title")
        )
        document(viewHtmlViaFunction)
          .getElementById("back-link")
          .attr("href")
          .contains(appConfig.agentAccountHomePage) mustBe true
        document(viewHtmlViaFunction).getElementById("back-link").text mustBe Messages("lbl.back")
        document(viewHtmlViaFunction).select("h1").text() mustBe Messages("employmenthistory.technical.error.header")
        document(viewHtmlViaFunction)
          .getElementsMatchingOwnText(Messages("employmenthistory.technical.error.message"))
          .size() mustBe 1
      }
    }
  }

}
