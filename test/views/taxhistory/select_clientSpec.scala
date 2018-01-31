/*
 * Copyright 2018 HM Revenue & Customs
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

import config.AppConfig
import form.SelectClientForm.selectClientForm
import play.api.i18n.Messages
import play.api.libs.json.Json
import support.GuiceAppSpec
import utils.TestUtil
import views.{Fixture, TestAppConfig}

class select_clientSpec extends GuiceAppSpec with TestUtil with TestAppConfig {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
    lazy val nino = randomNino.toString()
    val postData = Json.obj("clientId" -> nino)
    val validForm = selectClientForm.bind(postData)
    val invalidFormTooShort = selectClientForm.bind(Json.obj("clientId" -> "123456"))
    val invalidFormWrongFormat = selectClientForm.bind(Json.obj("clientId" -> "123456#$&"))

  }

  "select_client view" must {

    "have the correct title and GA page view event" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.title mustBe Messages("employmenthistory.select.client.title")

      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }

    "have the correct heading" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      heading.html mustBe Messages("employmenthistory.select.client.heading")
    }

    "display input field label for accessibility" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.select("span.visuallyhidden").text mustBe Messages("employmenthistory.select.client.clients.nino")
    }

    "display input field hint" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementsByClass("form-hint").get(0).text mustBe Messages("employmenthistory.select.client.nino.hint")
    }

    "display input field" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("clientId").attr("type") mustBe "text"
    }

    "limit input length to no more than 9 characters" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("clientId").attr("maxlength") mustBe "9"
    }

    "capitalise input characters" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("clientId").className must include ("uppercase-only")
    }

    "display continue button" in new ViewFixture {
      val view = views.html.taxhistory.select_client(validForm)
      doc.body.getElementById("continueButton").text mustBe Messages("employmenthistory.select.client.continue")
      doc.body.getElementById("continueButton").attr("type") mustBe "submit"
    }

    "display correct link for too short error hyperlink" in new ViewFixture {
      val view = views.html.taxhistory.select_client(invalidFormTooShort)
      val errorLink = doc.body.getElementById("clientId-error-summary")
      errorLink.text mustBe Messages("employmenthistory.select.client.error.invalid-format")
      errorLink.attr("href") mustBe "#clientId"
    }

    "display correct link for invalid format hyperlink" in new ViewFixture {
      val view = views.html.taxhistory.select_client(invalidFormWrongFormat)
      val errorLink = doc.body.getElementById("clientId-error-summary")
      errorLink.text mustBe Messages("employmenthistory.select.client.error.invalid-format")
      errorLink.attr("href") mustBe "#clientId"
    }
  }
}