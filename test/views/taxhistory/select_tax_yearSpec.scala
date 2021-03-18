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
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.Fixture
import views.html.taxhistory.select_tax_year

class select_tax_yearSpec extends GuiceAppSpec {

  private val maxChar: Int = 100

  trait ViewFixture extends Fixture {
    val postData: JsObject = Json.obj("selectTaxYear" -> "2016")
    val validForm: Form[SelectTaxYear] = selectTaxYearForm.bind(postData, maxChar)
    val name: Option[String] = Some("Test Name")
    val nino: String = "QQ123456C"
  }

  "select_tax_year view" must {

    "have correct title, heading and GA pageview event" in new ViewFixture {

      val view: HtmlFormat.Appendable = inject[select_tax_year].apply(validForm, List.empty, name, nino)

      doc.title mustBe Messages("employmenthistory.select.tax.year.title")
      doc.getElementById("pre-header").text() must include(Messages("employmenthistory.display.client.name", s"${name.get}"))
      doc.select("h1").text() must include(Messages("employmenthistory.select.tax.year.h1"))
      // AFID-462 - temporarily disabled due to security issue
      //doc.getElementsByClass("pre-heading medium-text").size() shouldBe 1
    }

    "Show nino when no name is present" in new ViewFixture {

      val view: HtmlFormat.Appendable = inject[select_tax_year].apply(validForm, List.empty, None, nino)
      doc.getElementById("pre-header").text() must include(Messages("employmenthistory.display.client.name", s"$nino"))
      doc.getElementsByClass("pre-heading medium-text").size() shouldBe 0
    }

    "show correct content on the page" in new ViewFixture {
      val options = List("2016" -> "value", "2015" -> "value1")

      val view: HtmlFormat.Appendable = inject[select_tax_year].apply(validForm, options, name, nino)
      val radioGroup: Elements = doc.select("input[type='radio']")
      radioGroup.size() must be(options.size)
      val inputRadio: Element = doc.getElementById("selectTaxYear-2016")
      inputRadio.attr("checked") shouldBe ""
      doc.getElementsMatchingOwnText(Messages("employmenthistory.select.tax.year.h1", s"${name.get}")).hasText mustBe true

    }

    "show correct content on the page for form with error" in new ViewFixture {
      val invalidForm: Form[SelectTaxYear] = selectTaxYearForm.bind(Json.obj("selectTaxYear" -> ""), maxChar)
      val options = List("2016" -> "value", "2015" -> "value1")

      val view: HtmlFormat.Appendable = inject[select_tax_year].apply(invalidForm, options, name, nino)
      val radioGroup: Elements = doc.select("input[type='radio']")
      radioGroup.size() must be(options.size)
      val inputRadio: Element = doc.getElementById("selectTaxYear-2016")
      inputRadio.attr("checked") shouldBe ""
      doc.getElementsMatchingOwnText(Messages("employmenthistory.select.tax.year")).hasText mustBe true

      doc.getElementById("error-summary-title").text mustBe Messages("employmenthistory.select.tax.year.error.heading")
      doc.getElementById("selectTaxYear-error").text contains Messages("employmenthistory.select.tax.year.error.message")
    }

    "show correct links and text in side bar" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[select_tax_year].apply(validForm, List.empty, name, nino)

      doc.getElementById("nav-bar").child(0).select("a").text shouldBe Messages("employmenthistory.select.client.sidebar.agent-services-home")
      doc.getElementById("nav-bar").child(0).select("div").text shouldBe Messages("employmenthistory.sidebar.links.more-options")
      doc.getElementById("nav-bar").child(0).select("a").attr("href") shouldBe appConfig.agentAccountHomePage

      doc.getElementById("nav-bar").child(1).select("a").text shouldBe Messages("employemntHistory.select.tax.year.sidebar.change.client")
      doc.getElementById("nav-bar").child(1).select("div").text shouldBe Messages("employemntHistory.select.tax.year.sidebar.income.and.tax")
      doc.getElementById("nav-bar").child(1).select("a").attr("href") shouldBe routes.SelectClientController.getSelectClientPage().url

    }
  }

}

