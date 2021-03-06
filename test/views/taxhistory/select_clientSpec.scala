/*
 * Copyright 2021 HM Revenue & Customs
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
import org.jsoup.nodes.Element
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.HtmlFormat
import support.{BaseSpec, GuiceAppSpec}
import utils.TestUtil
import views.Fixture
import views.html.taxhistory.select_client

class select_clientSpec extends GuiceAppSpec with BaseSpec with TestUtil {

  trait ViewFixture extends Fixture {
    lazy val nino: String = randomNino.toString()
    val postData: JsObject = Json.obj("clientId" -> nino)
    val validForm: Form[SelectClient] = selectClientForm.bind(postData, 100)
    val invalidFormWrongFormat: Form[SelectClient] = selectClientForm.bind(Json.obj("clientId" -> "123456#$&"), 100)

  }

  "select_client view" should {

    "have the correct title" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      doc.title shouldBe Messages("employmenthistory.select.client.title")

    }

    "have the correct heading" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      doc.select("h1").text() shouldBe Messages("employmenthistory.select.client.heading")
    }

   /* "display input field label for accessibility" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      doc.select("span.visuallyhidden").text shouldBe Messages("employmenthistory.select.client.clients.nino")
    }*/

    "display input field hint" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      doc.body.getElementsByClass("govuk-hint").get(0).text shouldBe Messages("employmenthistory.select.client.nino.hint")
    }

    "display input field" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      doc.body.getElementById("clientId").attr("type") shouldBe "text"
    }

    "limit input length to no more than 9 characters" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      doc.body.getElementById("clientId").attr("maxlength") shouldBe "9"
    }

    "display continue button" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      doc.body.getElementById("continueButton").text shouldBe Messages("employmenthistory.select.client.continue")
      doc.body.getElementById("continueButton").className shouldBe "govuk-button"
    }

    "display correct error message for invalid nino" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(invalidFormWrongFormat)
      doc.getElementById("error-summary-title").text mustBe Messages("employmenthistory.select.client.error.invalid-format.title")
      doc.getElementById("clientId-error").text contains Messages("employmenthistory.select.client.error.invalid-format")
    }

    "display sidebar with correct link(s)" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_client].apply(validForm)
      val agentServicesHomeLink: Element = doc.body.getElementById("nav-bar").child(0)
      agentServicesHomeLink.select("a").text shouldBe Messages("employmenthistory.select.client.sidebar.agent-services-home")
      agentServicesHomeLink.select("div").text shouldBe Messages("employmenthistory.sidebar.links.more-options")
      agentServicesHomeLink.select("a").attr("href") shouldBe appConfig.agentAccountHomePage
    }
  }
}
