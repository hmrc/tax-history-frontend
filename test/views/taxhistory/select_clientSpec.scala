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

import form.SelectClientForm.selectClientForm
import models.taxhistory.SelectClient
import org.jsoup.nodes.Element
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import support.GuiceAppSpec
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil
import views.{Fixture, TestAppConfig}

class select_clientSpec extends GuiceAppSpec with UnitSpec with TestUtil with TestAppConfig {

  trait ViewFixture extends Fixture {
    lazy val nino = randomNino.toString()
    val postData: JsObject = Json.obj("clientId" -> nino)
    val validForm: Form[SelectClient] = selectClientForm.bind(postData)
    val invalidFormTooShort: Form[SelectClient] = selectClientForm.bind(Json.obj("clientId" -> "123456"))
    val invalidFormWrongFormat: Form[SelectClient] = selectClientForm.bind(Json.obj("clientId" -> "123456#$&"))

  }

  "select_client view" should {

    "have the correct title and GA page view event" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.title shouldBe Messages("employmenthistory.select.client.title")

      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" shouldBe true
    }

    "have the correct heading" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("title").text() shouldBe Messages("employmenthistory.select.client.heading")
    }

    "display input field label for accessibility" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.select("span.visuallyhidden").text shouldBe Messages("employmenthistory.select.client.clients.nino")
    }

    "display input field hint" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementsByClass("form-hint").get(0).text shouldBe Messages("employmenthistory.select.client.nino.hint")
    }

    "display input field" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("clientId").attr("type") shouldBe "text"
    }

    "limit input length to no more than 9 characters" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("clientId").attr("maxlength") shouldBe "9"
    }

    "capitalise input characters" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("clientId").className should include("uppercase-only")
    }

    "display continue button" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("continueButton").text shouldBe Messages("employmenthistory.select.client.continue")
      doc.body.getElementById("continueButton").attr("type") shouldBe "submit"
    }

    "display correct link for too short error hyperlink" in new ViewFixture {
      val view = views.html.taxhistory.select_client(invalidFormTooShort)
      val errorLink: Element = doc.body.getElementById("clientId-error-summary")
      errorLink.text shouldBe Messages("employmenthistory.select.client.error.invalid-format")
      errorLink.attr("href") shouldBe "#clientId"
    }

    "display correct link for invalid format hyperlink" in new ViewFixture {
      val view = views.html.taxhistory.select_client(invalidFormWrongFormat)
      val errorLink: Element = doc.body.getElementById("clientId-error-summary")
      errorLink.text shouldBe Messages("employmenthistory.select.client.error.invalid-format")
      errorLink.attr("href") shouldBe "#clientId"
    }

    "display sidebar with correct link(s)" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      val agentServicesHomeLink: Element = doc.body.getElementById("nav-bar-desktop").child(0)
      agentServicesHomeLink.text shouldBe Messages("employmenthistory.select.client.sidebar.agent-services-home")
      agentServicesHomeLink.attr("href") shouldBe "fakeurl"
    }
  }
}