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
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.html.taxhistory.select_client
import views.{BaseViewSpec, Fixture}

class SelectClientViewSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest("GET", "/tax-history/select-client").withCSRFToken

  private val titleAndHeadingContent: String  = "Enter your client's National Insurance number"
  private val validForm: Form[SelectClient]   = selectClientForm.bind(Map("clientId" -> nino))
  private val invalidForm: Form[SelectClient] = selectClientForm.bind(Map("clientId" -> "123456#$&"))

  private def viewViaApply(form: Form[SelectClient] = validForm): HtmlFormat.Appendable =
    injected[select_client].apply(form)(
      request = request,
      messages = messages,
      appConfig = appConfig
    )

  private def viewViaRender(form: Form[SelectClient] = validForm): HtmlFormat.Appendable =
    injected[select_client].render(
      sCForm = form,
      request = request,
      messages = messages,
      appConfig = appConfig
    )

  private def viewViaF(form: Form[SelectClient] = validForm): HtmlFormat.Appendable =
    injected[select_client].f(form)(fakeRequest, messages, appConfig)

  private class ViewFixture(renderedView: HtmlFormat.Appendable = viewViaApply()) extends Fixture {
    val view: HtmlFormat.Appendable = renderedView
  }

  "SelectClientView" when {
    def test(method: String, validFormView: HtmlFormat.Appendable, invalidFormView: HtmlFormat.Appendable): Unit =
      s"$method" should {
        "have the correct title when no form errors have occurred" in new ViewFixture(validFormView) {
          document(view).title shouldBe expectedPageTitle(titleAndHeadingContent)
        }

        "have the correct title when form errors have occurred" in new ViewFixture(invalidFormView) {
          document(view).title shouldBe expectedErrorPageTitle(titleAndHeadingContent)
        }

        "have the correct heading when no form errors have occurred" in new ViewFixture(validFormView) {
          heading.text() shouldBe titleAndHeadingContent
        }

        "have the correct heading when form errors have occurred" in new ViewFixture(invalidFormView) {
          heading.text() shouldBe titleAndHeadingContent
        }
      }

    val input: Seq[(String, HtmlFormat.Appendable, HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply(), viewViaApply(invalidForm)),
      (".render", viewViaRender(), viewViaRender(invalidForm)),
      (".f", viewViaF(), viewViaF(invalidForm))
    )

    input.foreach(args => (test _).tupled(args))

    ".apply" should {
      "display input field hint" in new ViewFixture {
        document(view).body.getElementsByClass("govuk-hint").get(0).text shouldBe "For example, QQ123456C"
      }

      "display input field without data in session" in new ViewFixture {
        override val view: HtmlFormat.Appendable =
          inject[select_client].apply(validForm)(fakeRequest, messages, appConfig)

        document(view).body.getElementById("clientId").attr("type") shouldBe "text"
        document(view).body.getElementById("clientId").text()       shouldBe ""
      }

      "display input field with data in session" in new ViewFixture {
        override val view: HtmlFormat.Appendable =
          inject[select_client].apply(validForm)(fakeRequestWithNino, messages, appConfig)

        document(view).body.getElementById("clientId").attr("type")  shouldBe "text"
        document(view).body.getElementById("clientId").attr("value") shouldBe nino
      }

      "display continue button" in new ViewFixture {
        document(view).body.getElementById("continueButton").text      shouldBe "Continue"
        document(view).body.getElementById("continueButton").className shouldBe "govuk-button"
      }

      "display correct error message for invalid nino" in new ViewFixture {
        override val view: HtmlFormat.Appendable = inject[select_client].apply(invalidForm)

        document(view).getElementsByClass("govuk-error-summary__title").text mustBe "There is a problem"
        document(view)
          .getElementById(
            "clientId-error"
          )
          .text shouldBe "Error: National Insurance number must be 2 letters, 6 numbers, then A, B, C or D"
      }

      "display navigation bar with correct links" in new ViewFixture {
        document(view).getElementById("nav-home").text         shouldBe "Agent services home"
        validateConditionalContent("nav-client")
        validateConditionalContent("nav-year")
        document(view).getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
      }
    }
  }
}
