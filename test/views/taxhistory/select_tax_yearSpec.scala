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

package views.taxhistory

import form.SelectTaxYearForm.selectTaxYearForm
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.{Fixture, TestAppConfig}
import controllers.routes

class select_tax_yearSpec extends GuiceAppSpec with TestAppConfig {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
    val postData = Json.obj("selectTaxYear" -> "2016")
    val validForm = selectTaxYearForm.bind(postData)
    val name = "Test Name"
  }

  "select_tax_year view" must {

    "have correct title, heading and GA pageview event" in new ViewFixture {

      val view = views.html.taxhistory.select_tax_year(validForm, List.empty, name)

      doc.title mustBe Messages("employmenthistory.select.tax.year.title")
      doc.getElementById("pre-header").text() must include (Messages("employmenthistory.display.client.name",s"${name}"))
      doc.getElementById("header").text() must include (Messages("employmenthistory.select.tax.year.h1"))
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }

    "show correct content on the page" in new ViewFixture {
      val options = List("2016" -> "value", "2015" -> "value1")

      val view = views.html.taxhistory.select_tax_year(validForm, options, name)
      val radioGroup =  doc.select("input[type='radio']")
      radioGroup.size() must be(options.size)
      val inputRadio = doc.getElementById("selectTaxYear-2016")
      inputRadio.attr("checked") shouldBe "checked"
      doc.getElementsMatchingOwnText(Messages("employmenthistory.select.tax.year.h1",s"${name}")).hasText mustBe true

    }

    "show correct content on the page for form with error" in new ViewFixture {
      val invalidForm = selectTaxYearForm.bind(Json.obj("selectTaxYear" -> ""))
      val options = List("2016" -> "value", "2015" -> "value1")

      val view = views.html.taxhistory.select_tax_year(invalidForm, options, name)
      val radioGroup =  doc.select("input[type='radio']")
      radioGroup.size() must be(options.size)
      val inputRadio = doc.getElementById("selectTaxYear-2016")
      inputRadio.attr("checked") shouldBe ""
      doc.getElementsMatchingOwnText(Messages("employmenthistory.select.tax.year")).hasText mustBe true


      doc.getElementById("selectTaxYear-error-summary").text mustBe Messages("employmenthistory.select.tax.year.error.linktext")
      doc.getElementById("error-summary-heading").text mustBe Messages("employmenthistory.select.tax.year.error.heading")
      doc.select(".error-notification").first().text mustBe Messages("employmenthistory.select.tax.year.error.message")
    }

    "show correct links and text in side bar" in new ViewFixture {
      val view = views.html.taxhistory.select_tax_year(validForm, List.empty, name)

      doc.getElementById("nav-bar").child(0).text shouldBe Messages("employmenthistory.select.client.sidebar.agent-services-home")
      doc.getElementById("nav-bar").child(0).attr("href") shouldBe "fakeurl"

      doc.getElementById("nav-bar").child(3).text shouldBe Messages("employemntHistory.select.tax.year.sidebar.income.and.tax")

      doc.getElementById("nav-bar").child(4).text shouldBe Messages("employemntHistory.select.tax.year.sidebar.change.client")
      doc.getElementById("nav-bar").child(4).attr("href") shouldBe routes.SelectClientController.getSelectClientPage().url
    }
  }

}

