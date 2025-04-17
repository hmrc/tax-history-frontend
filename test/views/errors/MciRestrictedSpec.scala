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

package views.errors

import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import utils.TestUtil
import views.html.errors.mci_restricted
import views.{BaseViewSpec, Fixture}

class MciRestrictedSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  trait ViewFixture extends Fixture {
    val nino: String                               = TestUtil.randomNino.toString()
    val view: HtmlFormat.Appendable                = inject[mci_restricted].apply()(request, messages, appConfig)
    val viewHtmlViaRender: HtmlFormat.Appendable   = inject[mci_restricted].render(request, messages, appConfig)
    val viewHtmlViaFunction: HtmlFormat.Appendable = inject[mci_restricted].f()(request, messages, appConfig)
  }

  "MCIRestrictedView" when {

    "view.apply()" should {

      "have correct content" in new ViewFixture {

        document(view).title mustBe expectedPageTitle(messages("employmenthistory.mci.restricted.title"))
        document(view).getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
        document(view).getElementById("back-link").text mustBe Messages("lbl.back")
        document(view).getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.text")).hasText mustBe true
        document(view)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.telephone"))
          .hasText mustBe true
        document(view)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.telephone.number"))
          .hasText mustBe true
        document(view)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.openingtimes"))
          .hasText mustBe true
        document(view)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.openingtimes.days"))
          .hasText mustBe true
        document(view).select("h1").text() mustBe Messages("employmenthistory.mci.restricted.header")

      }
    }

    "view.render()" should {

      "have correct content" in new ViewFixture {

        document(viewHtmlViaRender).title mustBe expectedPageTitle(messages("employmenthistory.mci.restricted.title"))
        document(viewHtmlViaRender).getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
        document(viewHtmlViaRender).getElementById("back-link").text mustBe Messages("lbl.back")
        document(viewHtmlViaRender)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.text"))
          .hasText mustBe true
        document(viewHtmlViaRender)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.telephone"))
          .hasText mustBe true
        document(viewHtmlViaRender)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.telephone.number"))
          .hasText mustBe true
        document(viewHtmlViaRender)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.openingtimes"))
          .hasText mustBe true
        document(viewHtmlViaRender)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.openingtimes.days"))
          .hasText mustBe true
        document(viewHtmlViaRender).select("h1").text() mustBe Messages("employmenthistory.mci.restricted.header")
      }
    }

    "view.f()" should {

      "have correct content" in new ViewFixture {

        document(viewHtmlViaFunction).title mustBe expectedPageTitle(messages("employmenthistory.mci.restricted.title"))
        document(viewHtmlViaFunction).getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
        document(viewHtmlViaFunction).getElementById("back-link").text mustBe Messages("lbl.back")
        document(viewHtmlViaFunction)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.text"))
          .hasText mustBe true
        document(viewHtmlViaFunction)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.telephone"))
          .hasText mustBe true
        document(viewHtmlViaFunction)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.telephone.number"))
          .hasText mustBe true
        document(viewHtmlViaFunction)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.openingtimes"))
          .hasText mustBe true
        document(viewHtmlViaFunction)
          .getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.openingtimes.days"))
          .hasText mustBe true
        document(viewHtmlViaFunction).select("h1").text() mustBe Messages("employmenthistory.mci.restricted.header")

      }
    }
  }
}
