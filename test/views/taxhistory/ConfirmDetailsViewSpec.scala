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

package views.taxhistory

import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.{BaseSpec, GuiceAppSpec}
import utils.TestUtil
import views.html.taxhistory.confirm_details
import views.{BaseViewSpec, Fixture}

class ConfirmDetailsViewSpec extends GuiceAppSpec with BaseViewSpec with BaseSpec with TestUtil {

  implicit val request: Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/tax-history/select-client-confirm").withCSRFToken

  trait ViewFixture extends Fixture {
    val nino: String                               = randomNino.toString()
    val name: String                               = "Hazel Young"
    val view: HtmlFormat.Appendable                = inject[confirm_details].apply(name, nino)
    val viewHtmlViaRender: HtmlFormat.Appendable   =
      inject[confirm_details].render(name, nino, fakeRequest, messages, appConfig)
    val viewHtmlViaFunction: HtmlFormat.Appendable =
      inject[confirm_details].f(name, nino)(fakeRequest, messages, appConfig)
  }

  "Confirm Details view" should {

    "view .render()" should {
      "render correctly with correct heading" in new ViewFixture {
        document(viewHtmlViaRender).title             shouldBe expectedPageTitle("Confirm your client's details")
        document(viewHtmlViaRender).select("h1").text shouldBe "Confirm your client's details"
      }
    }

    "view .f()" when {
      "supplied the correct parameters create the view with correct heading" in new ViewFixture {
        document(viewHtmlViaFunction).title           shouldBe expectedPageTitle("Confirm your client's details")
        document(viewHtmlViaRender).select("h1").text shouldBe "Confirm your client's details"
      }
    }

    "display the correct title" in new ViewFixture {
      document(view).title shouldBe expectedPageTitle("Confirm your client's details")
    }

    "display the correct heading" in new ViewFixture {
      document(view).select("h1").text shouldBe "Confirm your client's details"
    }

    "display the correct client's name details" in new ViewFixture {
      document(view).getElementById("client-details").child(0).child(0).text shouldBe "Name"
      document(view).getElementById("client-details").child(0).child(1).text shouldBe name
    }

    "display the correct client's nino details" in new ViewFixture {
      document(view).getElementById("client-details").child(1).child(0).text shouldBe "National Insurance number"
      document(view).getElementById("client-details").child(1).child(1).text shouldBe nino
    }

    "display confirm and continue button" in new ViewFixture {
      document(view).getElementById("confirm-and-continue-button").text      shouldBe "Confirm and continue"
      document(view).getElementById("confirm-and-continue-button").className shouldBe "govuk-button"
    }

    "display cancel link" in new ViewFixture {
      document(view).getElementById("cancel-link").text      shouldBe "Cancel"
      document(view).getElementById("cancel-link").className shouldBe "govuk-link"
    }

    "display navigation bar with correct links" in new ViewFixture {
      document(view).getElementById("nav-home").text         shouldBe "Agent services home"
      validateConditionalContent("nav-client")
      validateConditionalContent("nav-year")
      document(view).getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
    }
  }
}
