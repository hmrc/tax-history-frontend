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

package views.taxhistory

import form.SelectClientForm.selectClientForm
import models.taxhistory.SelectClient
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.{BaseSpec, GuiceAppSpec}
import utils.TestUtil
import views.html.taxhistory.select_client
import views.{BaseViewSpec, Fixture}

class SelectClientViewSpec extends GuiceAppSpec with BaseViewSpec with BaseSpec with TestUtil {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest("GET", "/tax-history/select-client").withCSRFToken
  private val maxChars                                  = 100

  trait ViewFixture extends Fixture {
    lazy val nino: String                          = randomNino.toString()
    val postData: JsObject                         = Json.obj("clientId" -> nino)
    val validForm: Form[SelectClient]              = selectClientForm.bind(postData, maxChars)
    val invalidFormWrongFormat: Form[SelectClient] =
      selectClientForm.bind(Json.obj("clientId" -> "123456#$&"), maxChars)
  }

  val titleAndHeadingContent = "Enter your client's National Insurance number"

  "select_client view" should {

    "have the correct title when no form errors have occurred" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      document(view).title shouldBe expectedPageTitle(titleAndHeadingContent)
    }

    "have the correct title when form errors have occurred" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(invalidFormWrongFormat)
      document(view).title shouldBe expectedErrorPageTitle(titleAndHeadingContent)
    }

    "have the correct heading" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      document(view).select("h1").text() shouldBe titleAndHeadingContent
    }

    "display input field hint" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      document(view).body.getElementsByClass("govuk-hint").get(0).text shouldBe
        Messages("employmenthistory.select.client.nino.hint")
    }

    "display input field" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      document(view).body.getElementById("clientId").attr("type") shouldBe "text"
    }

    "display continue button" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      document(view).body.getElementById("continueButton").text      shouldBe Messages(
        "employmenthistory.select.client.continue"
      )
      document(view).body.getElementById("continueButton").className shouldBe "govuk-button"
    }

    "display correct error message for invalid nino" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(invalidFormWrongFormat)
      document(view).getElementsByClass("govuk-error-summary__title").text mustBe Messages(
        "employmenthistory.select.client.error.invalid-format.title"
      )
      document(view).getElementById("clientId-error").text contains Messages(
        "employmenthistory.select.client.error.invalid-format"
      )
    }

    "display navigation bar with correct links" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      document(view).getElementById("nav-home").text         shouldBe Messages("nav.home")
      validateConditionalContent("nav-client")
      validateConditionalContent("nav-year")
      document(view).getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
    }
  }
}
