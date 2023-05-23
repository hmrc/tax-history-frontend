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

import form.SelectTaxYearForm.selectTaxYearForm
import models.taxhistory.SelectTaxYear
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.html.taxhistory.select_tax_year
import views.{BaseViewSpec, Fixture}

class SelectTaxYearViewSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/tax-history/select-tax-year").withCSRFToken

  private val titleAndHeadingContent: String           = "Select the tax year you want to view"
  private val validForm: Form[SelectTaxYear]           = selectTaxYearForm.bind(Map("selectTaxYear" -> "2016"))
  private val invalidForm: Form[SelectTaxYear]         = selectTaxYearForm.bind(Map("selectTaxYear" -> ""))
  private val taxYearOptions: List[(String, String)]   = List("2016" -> "2016 to 2017", "2015" -> "2015 to 2016")
  private val selectedTaxYear: SelectTaxYear           = SelectTaxYear(Some("2016"))
  private val selectedTaxYearWrongValue: SelectTaxYear = SelectTaxYear(Some("1999"))

  private def viewViaApply(form: Form[SelectTaxYear] = validForm): HtmlFormat.Appendable =
    injected[select_tax_year].apply(
      form = form,
      taxYears = taxYearOptions,
      taxYearFromSession = noSelectedTaxYear
    )(
      request = request,
      messages = messages,
      appConfig = appConfig
    )

  private def viewViaRender(form: Form[SelectTaxYear] = validForm): HtmlFormat.Appendable =
    injected[select_tax_year].render(
      form = form,
      taxYears = taxYearOptions,
      taxYearFromSession = noSelectedTaxYear,
      request = request,
      messages = messages,
      appConfig = appConfig
    )

  private def viewViaF(form: Form[SelectTaxYear] = validForm): HtmlFormat.Appendable = injected[select_tax_year].f(
    form,
    taxYearOptions,
    noSelectedTaxYear
  )(fakeRequest, messages, appConfig)

  private class ViewFixture(renderedView: HtmlFormat.Appendable = viewViaApply()) extends Fixture {
    val view: HtmlFormat.Appendable = renderedView
  }

  "SelectTaxYearView" when {
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
      "show correct content on the page" in new ViewFixture {
        val radioGroup: Elements = document(view).select("input[type='radio']")

        radioGroup.size() shouldBe taxYearOptions.size
        taxYearOptions.map(_._1).foreach { year =>
          val inputRadio: Element = document(view).getElementById(s"selectTaxYear-$year")
          attributes(inputRadio).keySet shouldNot contain("checked")
        }
        document(view)
          .getElementsMatchingOwnText(titleAndHeadingContent)
          .hasText        shouldBe true

        def radioLabel(i: Int): String =
          s"#main-content > div > div > div > div > form > div > fieldset > div > div:nth-child($i) > label"

        document(view).select(radioLabel(1)).text() shouldBe "2016 to 2017"
        document(view).select(radioLabel(2)).text() shouldBe "2015 to 2016"
      }

      "show correct content on the page fill from cache with wrong value" in new ViewFixture {
        override val view: HtmlFormat.Appendable =
          inject[select_tax_year].apply(validForm, taxYearOptions, selectedTaxYearWrongValue)
        val radioGroup: Elements                 = document(view).select("input[type='radio']")

        radioGroup.size() shouldBe taxYearOptions.size
        taxYearOptions.map(_._1).foreach { year =>
          val inputRadio: Element = document(view).getElementById(s"selectTaxYear-$year")
          attributes(inputRadio).keySet shouldNot contain("checked")
        }
        document(view)
          .getElementsMatchingOwnText(titleAndHeadingContent)
          .hasText        shouldBe true
      }

      "show correct content on the page fill from cache with correct value" in new ViewFixture {
        override val view: HtmlFormat.Appendable =
          inject[select_tax_year].apply(validForm, taxYearOptions, selectedTaxYear)
        val radioGroup: Elements                 = document(view).select("input[type='radio']")

        radioGroup.size() shouldBe taxYearOptions.size
        taxYearOptions.map(_._1).foreach { year =>
          val inputRadio: Element = document(view).getElementById(s"selectTaxYear-$year")
          if (year == selectedTaxYear.taxYear.get) {
            attributes(inputRadio).keySet should contain("checked")
          } else {
            attributes(inputRadio).keySet shouldNot contain("checked")
          }
        }
        document(view)
          .getElementsMatchingOwnText(titleAndHeadingContent)
          .hasText        shouldBe true
      }

      "show correct content on the page for form with error" in new ViewFixture {
        override val view: HtmlFormat.Appendable =
          inject[select_tax_year].apply(invalidForm, taxYearOptions, noSelectedTaxYear)
        val radioGroup: Elements                 = document(view).select("input[type='radio']")

        radioGroup.size() shouldBe taxYearOptions.size

        val inputRadio: Element = document(view).getElementById("selectTaxYear-2016")
        attributes(inputRadio).keySet shouldNot contain("checked")

        document(view).getElementsMatchingOwnText("Select a tax year").hasText shouldBe true
        document(view).getElementsByClass("govuk-error-summary__title").text   shouldBe "There is a problem"
        document(view).getElementById("selectTaxYear-error").text              shouldBe "Error: Select a tax year"
      }

      "display navigation bar with correct links" in new ViewFixture {
        document(view).getElementById("nav-home").text         shouldBe "Agent services home"
        document(view).getElementById("nav-client").text       shouldBe "Select client"
        validateConditionalContent("nav-year")
        document(view).getElementById("nav-home").attr("href") shouldBe appConfig.agentAccountHomePage
      }
    }
  }
}
