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

import form.SelectClientForm.selectClientForm
import models.taxhistory.SelectClient
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.Html
import support.GuiceAppSpec
import views.{BaseViewSpec, Fixture}
import views.html.taxhistory.select_client

class SelectClientViewSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest("GET", "/tax-history/select-client").withCSRFToken

  private val maxChars: Long                             = 100
  private val postData: JsObject                         = Json.obj("clientId" -> nino)
  private val validForm: Form[SelectClient]              = selectClientForm.bind(postData, maxChars)
  private val invalidFormWrongFormat: Form[SelectClient] =
    selectClientForm.bind(Json.obj("clientId" -> "123456#$&"), maxChars)
  private val titleAndHeadingContent: String             = "Enter your client's National Insurance number"

  private val viewViaApply: Html = inject[select_client].apply(validForm)

  private val viewViaRender: Html = inject[select_client].render(validForm, request, messages, appConfig)

  private val viewViaF: Html = inject[select_client].f(validForm)(request, messages, appConfig)

  private class ViewFixture(renderedView: Html = viewViaApply) extends Fixture {
    val view: Html = renderedView
  }

  "SelectClientView" should {
    Seq((viewViaApply, ".apply"), (viewViaRender, ".render"), (viewViaF, ".f")).foreach { case (view, method) =>
      s"have the correct title when no form errors have occurred for $method" in new ViewFixture(view) {
        document(this.view).title shouldBe expectedPageTitle(titleAndHeadingContent)
      }

      s"have the correct heading for $method" in new ViewFixture(view) {
        document(this.view).select("h1").text() shouldBe titleAndHeadingContent
      }
    }

    "have the correct title when form errors have occurred" in new ViewFixture {
      override val view: Html = inject[select_client].apply(invalidFormWrongFormat)
      document(view).title shouldBe expectedErrorPageTitle(titleAndHeadingContent)
    }

    "display input field hint" in new ViewFixture {
      document(this.view).body.getElementsByClass("govuk-hint").get(0).text shouldBe
        messages("employmenthistory.select.client.nino.hint")
    }

    "display input field" in new ViewFixture {
      document(this.view).body.getElementById("clientId").attr("type") shouldBe "text"
    }

    "display input field without data in session" in new ViewFixture {
      override val view: Html =
        inject[select_client].apply(validForm)(fakeRequestWithTaxYear, messages, appConfig)
      document(view).body.getElementById("clientId").attr("type") shouldBe "text"
      document(view).body.getElementById("clientId").text()       shouldBe ""
    }

    "display input field with data in session" in new ViewFixture {
      override val view: Html = inject[select_client].apply(validForm)(fakeRequestWithNino, messages, appConfig)
      document(view).body.getElementById("clientId").attr("type")  shouldBe "text"
      document(view).body.getElementById("clientId").attr("value") shouldBe nino
    }

    "display continue button" in new ViewFixture {
      document(this.view).body.getElementById("continueButton").text      shouldBe messages(
        "employmenthistory.select.client.continue"
      )
      document(this.view).body.getElementById("continueButton").className shouldBe "govuk-button"
    }

    "display correct error message for invalid nino" in new ViewFixture {
      override val view: Html = inject[select_client].apply(invalidFormWrongFormat)
      document(view).getElementsByClass("govuk-error-summary__title").text shouldBe messages(
        "employmenthistory.select.client.error.invalid-format.title"
      )
      document(view).getElementById("clientId-error").text contains messages(
        "employmenthistory.select.client.error.invalid-format"
      )
    }

    "display navigation bar with correct links" in new ViewFixture {
      document(this.view).getElementById("nav-home").text         shouldBe messages("nav.home")
      validateConditionalContent("nav-client")
      validateConditionalContent("nav-year")
      document(this.view).getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
    }
  }
}
