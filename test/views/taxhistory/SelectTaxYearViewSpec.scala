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

import form.SelectTaxYearForm.selectTaxYearForm
import models.taxhistory.SelectTaxYear
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.Html
import support.GuiceAppSpec
import views.{BaseViewSpec, Fixture}
import views.html.taxhistory.select_tax_year

class SelectTaxYearViewSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/tax-history/select-tax-year").withCSRFToken

  private val maxChar: Int                             = 100
  private val name: Option[String]                     = Some("Test Name")
  private val selectedTaxYear: SelectTaxYear           = SelectTaxYear(Some("2016"))
  private val selectedTaxYearWrongValue: SelectTaxYear = SelectTaxYear(Some("1999"))
  private val postData: JsObject                       = Json.obj("selectTaxYear" -> "2016")
  private val validForm: Form[SelectTaxYear]           = selectTaxYearForm.bind(postData, maxChar)
  private val invalidForm: Form[SelectTaxYear]         = selectTaxYearForm.bind(Json.obj("selectTaxYear" -> ""), maxChar)

  private val viewViaApply: Html = inject[select_tax_year].apply(validForm, List.empty, noSelectedTaxYear, name, nino)

  private val viewViaRender: Html =
    inject[select_tax_year].render(validForm, List.empty, noSelectedTaxYear, name, nino, request, messages, appConfig)

  private val viewViaF: Html = inject[select_tax_year].f(
    validForm,
    List.empty,
    noSelectedTaxYear,
    name,
    nino
  )(request, messages, appConfig)

  private class ViewFixture(renderedView: Html = viewViaApply) extends Fixture {
    val view: Html = renderedView
  }

  "SelectTaxYearView" should {
    Seq((viewViaApply, ".apply"), (viewViaRender, ".render"), (viewViaF, ".f")).foreach { case (view, method) =>
      s"have correct heading and GA pageview event for $method" in new ViewFixture(view) {
        document(this.view).select("h1").text() should include(messages("employmenthistory.select.tax.year.h1"))
      }

      s"have correct title when no form errors occur for $method" in new ViewFixture(view) {
        document(this.view).title shouldBe expectedPageTitle(messages("employmenthistory.select.tax.year.title"))
      }
    }

    "have correct title when form errors occur" in new ViewFixture {
      override val view: Html =
        inject[select_tax_year].apply(invalidForm, List.empty, noSelectedTaxYear, name, nino)
      document(view).title shouldBe expectedErrorPageTitle(messages("employmenthistory.select.tax.year.title"))
    }

    "show correct content on the page" in new ViewFixture {
      val options: List[(String, String)] = List("2016" -> "value", "2015" -> "value1")

      override val view: Html  = inject[select_tax_year].apply(validForm, options, noSelectedTaxYear, name, nino)
      val radioGroup: Elements = document(view).select("input[type='radio']")
      radioGroup.size() shouldBe options.size
      options.map(_._1).foreach { year =>
        val inputRadio: Element = document(view).getElementById(s"selectTaxYear-$year")
        attributes(inputRadio).keySet should not contain "checked"
      }
      document(view)
        .getElementsMatchingOwnText(messages("employmenthistory.select.tax.year.h1", s"${name.get}"))
        .hasText        shouldBe true
    }

    "show correct content on the page fill from cache with wrong value" in new ViewFixture {
      val options: List[(String, String)] = List("2016" -> "value", "2015" -> "value1")

      override val view: Html  =
        inject[select_tax_year].apply(validForm, options, selectedTaxYearWrongValue, name, nino)
      val radioGroup: Elements = document(view).select("input[type='radio']")
      radioGroup.size() shouldBe options.size
      options.map(_._1).foreach { year =>
        val inputRadio: Element = document(view).getElementById(s"selectTaxYear-$year")
        attributes(inputRadio).keySet should not contain "checked"
      }
      document(view)
        .getElementsMatchingOwnText(messages("employmenthistory.select.tax.year.h1", s"${name.get}"))
        .hasText        shouldBe true
    }

    "show correct content on the page fill from cache with correct value" in new ViewFixture {
      val options: List[(String, String)] = List("2016" -> "value", "2015" -> "value1")

      override val view: Html  = inject[select_tax_year].apply(validForm, options, selectedTaxYear, name, nino)
      val radioGroup: Elements = document(view).select("input[type='radio']")
      radioGroup.size() shouldBe options.size
      options.map(_._1).foreach { year =>
        val inputRadio: Element = document(view).getElementById(s"selectTaxYear-$year")
        if (year == selectedTaxYear.taxYear.get) {
          attributes(inputRadio).keySet should contain("checked")
        } else {
          attributes(inputRadio).keySet should not contain "checked"
        }
      }
      document(view)
        .getElementsMatchingOwnText(messages("employmenthistory.select.tax.year.h1", s"${name.get}"))
        .hasText        shouldBe true
    }

    "show correct date content on the page" in new ViewFixture {
      val options: List[(String, String)] = List("2016" -> "2016 to 2017", "2015" -> "2015 to 2016")
      override val view: Html             = inject[select_tax_year].apply(validForm, options, noSelectedTaxYear, name, nino)

      def radioLabel(i: Int): String =
        s"#main-content > div > div > form > div > fieldset > div > div:nth-child($i) > label"

      document(view).select(radioLabel(1)).text() shouldBe "2016 to 2017"
      document(view).select(radioLabel(2)).text() shouldBe "2015 to 2016"
    }

    "show correct content on the page for form with error" in new ViewFixture {
      val options: List[(String, String)] = List("2016" -> "value", "2015" -> "value1")

      override val view: Html =
        inject[select_tax_year].apply(invalidForm, options, noSelectedTaxYear, name, nino)

      val radioGroup: Elements = document(view).select("input[type='radio']")
      radioGroup.size() shouldBe options.size

      val inputRadio: Element = document(view).getElementById("selectTaxYear-2016")
      attributes(inputRadio).keySet should not contain "checked"

      document(view).getElementsMatchingOwnText(messages("employmenthistory.select.tax.year")).hasText shouldBe true
      document(view).getElementsByClass("govuk-error-summary__title").text                             shouldBe messages(
        "employmenthistory.select.tax.year.error.heading"
      )
      document(view).getElementById("selectTaxYear-error").text contains messages(
        "employmenthistory.select.tax.year.error.message"
      )
    }

    "display navigation bar with correct links" in new ViewFixture {
      document(this.view).getElementById("nav-home").text         shouldBe messages("nav.home")
      document(this.view).getElementById("nav-client").text       shouldBe messages("nav.client")
      validateConditionalContent("nav-year")
      document(this.view).getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
    }
  }
}
