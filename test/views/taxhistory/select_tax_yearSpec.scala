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

import controllers.routes
import form.SelectTaxYearForm.selectTaxYearForm
import models.taxhistory.SelectTaxYear
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.twirl.api.Html
import support.GuiceAppSpec
import views.{Fixture, TestAppConfig}

class select_tax_yearSpec extends GuiceAppSpec with TestAppConfig {

  trait ViewFixture extends Fixture {
    val postData: JsObject = Json.obj("selectTaxYear" -> "2016")
    val validForm: Form[SelectTaxYear] = selectTaxYearForm.bind(postData)
    val name = Some("Test Name")
    val nino = "QQ123456C"
  }

  "select_tax_year view" must {

    "have correct title, heading and GA pageview event" in new ViewFixture {

      val view = views.html.taxhistory.select_tax_year(validForm, List.empty, name, nino)

      doc.title mustBe Messages("employmenthistory.select.tax.year.title")
      doc.getElementById("pre-header").text() must include(Messages("employmenthistory.display.client.name", s"${name.get}"))
      doc.getElementById("header").text() must include(Messages("employmenthistory.select.tax.year.h1"))
      // AFID-462 - temporarily disabled due to security issue
      //doc.getElementsByClass("pre-heading medium-text").size() shouldBe 1
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }

    "Show nino when no name is present" in new ViewFixture {

      val view = views.html.taxhistory.select_tax_year(validForm, List.empty, None, nino)
      doc.getElementById("pre-header").text() must include(Messages("employmenthistory.display.client.name", s"${nino}"))
      doc.getElementsByClass("pre-heading medium-text").size() shouldBe 0
    }

    "show correct content on the page" in new ViewFixture {
      val options = List("2016" -> "value", "2015" -> "value1")

      val view: Html = views.html.taxhistory.select_tax_year(validForm, options, name, nino)
      val radioGroup: Elements = doc.select("input[type='radio']")
      radioGroup.size() must be(options.size)
      val inputRadio: Element = doc.getElementById("selectTaxYear-2016")
      inputRadio.attr("checked") shouldBe "checked"
      doc.getElementsMatchingOwnText(Messages("employmenthistory.select.tax.year.h1", s"${name.get}")).hasText mustBe true

    }

    "show correct content on the page for form with error" in new ViewFixture {
      val invalidForm: Form[SelectTaxYear] = selectTaxYearForm.bind(Json.obj("selectTaxYear" -> ""))
      val options = List("2016" -> "value", "2015" -> "value1")

      val view = views.html.taxhistory.select_tax_year(invalidForm, options, name, nino)
      val radioGroup: Elements = doc.select("input[type='radio']")
      radioGroup.size() must be(options.size)
      val inputRadio: Element = doc.getElementById("selectTaxYear-2016")
      inputRadio.attr("checked") shouldBe ""
      doc.getElementsMatchingOwnText(Messages("employmenthistory.select.tax.year")).hasText mustBe true


      doc.getElementById("selectTaxYear-error-summary").text mustBe Messages("employmenthistory.select.tax.year.error.linktext")
      doc.getElementById("error-summary-heading").text mustBe Messages("employmenthistory.select.tax.year.error.heading")
      doc.select(".error-notification").first().text mustBe Messages("employmenthistory.select.tax.year.error.message")
    }

    "show correct links and text in side bar" in new ViewFixture {
      val view = views.html.taxhistory.select_tax_year(validForm, List.empty, name, nino)

      doc.getElementById("nav-bar").child(0).text shouldBe Messages("employmenthistory.select.client.sidebar.agent-services-home")
      doc.getElementById("nav-bar").child(0).attr("href") shouldBe "fakeurl"

      doc.getElementById("nav-bar").child(3).text shouldBe Messages("employemntHistory.select.tax.year.sidebar.income.and.tax")

      doc.getElementById("nav-bar").child(4).text shouldBe Messages("employemntHistory.select.tax.year.sidebar.change.client")
      doc.getElementById("nav-bar").child(4).attr("href") shouldBe routes.SelectClientController.getSelectClientPage().url
    }
  }

}

