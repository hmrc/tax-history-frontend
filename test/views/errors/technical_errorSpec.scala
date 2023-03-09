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
import views.{BaseViewSpec, Fixture}
import views.html.errors.technical_error

class technical_errorSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  "Technical Error view" must {

    "have correct title, heading and GA page view event" in new Fixture {

      val view: HtmlFormat.Appendable = inject[technical_error].apply()

      doc.title mustBe expectedPageTitle(messages("employmenthistory.technical.error.title"))
      doc.getElementById("back-link").attr("href").contains(appConfig.agentAccountHomePage) mustBe true
      doc.getElementById("back-link").text mustBe Messages("lbl.back")
      doc.select("h1").text() mustBe Messages("employmenthistory.technical.error.header")
      doc.getElementsMatchingOwnText(Messages("employmenthistory.technical.error.message")).size() mustBe 1
    }
  }

}
