/*
 * Copyright 2022 HM Revenue & Customs
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
    val nino: String                = randomNino.toString()
    val name: String                = "Hazel Young"
    val view: HtmlFormat.Appendable = inject[confirm_details].apply(name, nino)
  }

  "Confirm Details view" should {

    "display the correct title" in new ViewFixture {
      doc.title shouldBe expectedPageTitle("Confirm your client's details")
    }

    "display the correct heading" in new ViewFixture {
      doc.select("h1").text shouldBe "Confirm your client's details"
    }

    "display the correct client's name details" in new ViewFixture {
      doc.getElementById("client-details").child(0).child(0).text shouldBe "Name"
      doc.getElementById("client-details").child(0).child(1).text shouldBe name
    }

    "display the correct client's nino details" in new ViewFixture {
      doc.getElementById("client-details").child(1).child(0).text shouldBe "National Insurance number"
      doc.getElementById("client-details").child(1).child(1).text shouldBe nino
    }

    "display confirm and continue button" in new ViewFixture {
      doc.getElementById("confirm-and-continue-button").text      shouldBe "Confirm and continue"
      doc.getElementById("confirm-and-continue-button").className shouldBe "govuk-button"
    }

    "display cancel link" in new ViewFixture {
      doc.getElementById("cancel-link").text      shouldBe "Cancel"
      doc.getElementById("cancel-link").className shouldBe "govuk-link"
    }

    "display navigation bar with correct links" in new ViewFixture {
      doc.getElementById("nav-home").text         shouldBe "Agent services home"
      validateConditionalContent("nav-client")
      validateConditionalContent("nav-year")
      doc.getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
    }
  }
}
