/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import support.GuiceAppSpec
import views.html.govuk_wrapper
import views.models.PageTitle

class GovUkWrapperViewSpec extends GuiceAppSpec with BaseViewSpec {

  private def viewViaApply(messages: Messages = messages): HtmlFormat.Appendable = injected[govuk_wrapper].apply(
    title = PageTitle("page title")
  )(
    mainContent = Html("<h1>page heading</h1>")
  )(
    using request = fakeRequest,
    messages = messages,
    appConfig = appConfig
  )

  private val viewViaRender: HtmlFormat.Appendable = injected[govuk_wrapper].render(
    title = PageTitle("page title"),
    beforeContentHtml = None,
    researchBannerUrl = None,
    signOut = true,
    timeoutActive = true,
    backLink = None,
    mainContent = Html("<h1>page heading</h1>"),
    request = fakeRequest,
    messages = messages,
    appConfig = appConfig
  )

  private val viewViaF: HtmlFormat.Appendable = injected[govuk_wrapper].f(
    PageTitle("page title"),
    None,
    None,
    true,
    true,
    None
  )(Html("<h1>page heading</h1>"))(fakeRequest, messages, appConfig)

  private def document(view: HtmlFormat.Appendable): Document = Jsoup.parse(view.toString())

  private def renderViewTest(method: String, view: HtmlFormat.Appendable): Unit =
    s"$method" should {
      "display the correct title" in {
        document(view).title shouldBe expectedPageTitle("page title")
      }

      "display the correct heading" in {
        document(view).select("h1").text() shouldBe "page heading"
      }
    }

  private def languageTest(scenario: String, language: String, view: HtmlFormat.Appendable): Unit =
    s"$scenario" should {
      s"render the html lang as $language" in {
        document(view).select("html").attr("lang") shouldBe language
      }
    }

  private val selectClientUrl   = "/tax-history/select-client"
  private val confirmDetailsUrl = "/tax-history/select-client-confirm"
  private val selectTaxYearUrl  = "/tax-history/select-tax-year"
  private val recordUrl         = "/tax-history/client-income-record/2024"

  private def viewAtPath(path: String, clientSelected: Boolean = true): HtmlFormat.Appendable = {
    val req =
      if (clientSelected) FakeRequest("GET", path).withSession(ninoSessionKey -> nino)
      else FakeRequest("GET", path)

    injected[govuk_wrapper].apply(
      title = PageTitle("page title")
    )(
      mainContent = Html("<h1>page heading</h1>")
    )(
      using request = req,
      messages = messages,
      appConfig = appConfig
    )
  }

  "GovUkWrapperView" when {
    renderViewTest(".apply", viewViaApply())
    renderViewTest(".render", viewViaRender)
    renderViewTest(".f", viewViaF)
    languageTest("default language", "en", viewViaApply())
    languageTest("language is toggled to Welsh", "cy", viewViaApply(welshMessages))
  }

  "the service navigation" should {

    "hide 'Select year' before a client has been selected" in {
      val doc = document(viewAtPath(selectClientUrl, clientSelected = false))

      doc.getElementById("nav-home").text    shouldBe messages("nav.home")
      doc.getElementById("nav-client").text  shouldBe messages("nav.client")
      Option(doc.getElementById("nav-year")) shouldBe None
    }

    Seq("select client" -> selectClientUrl, "confirm details" -> confirmDetailsUrl).foreach { case (page, url) =>
      s"hide 'Select year' on the $page page even when a NINO is in the session" in {
        Option(document(viewAtPath(url)).getElementById("nav-year")) shouldBe None
      }
    }

    "display 'Select year' once past the client selection pages" in {
      val doc = document(viewAtPath(selectTaxYearUrl))

      doc.getElementById("nav-home").text   shouldBe messages("nav.home")
      doc.getElementById("nav-client").text shouldBe messages("nav.client")
      doc.getElementById("nav-year").text   shouldBe messages("nav.year")
    }

    "mark 'Select client' as the current page on the select client page" in {
      document(viewAtPath(selectClientUrl)).getElementById("nav-client").attr("aria-current") shouldBe "page"
    }

    "mark 'Select year' as the current page on the select tax year page" in {
      val doc = document(viewAtPath(selectTaxYearUrl))

      doc.getElementById("nav-year").attr("aria-current")   shouldBe "page"
      doc.getElementById("nav-client").attr("aria-current") shouldBe ""
    }

    "mark no link as current on a page outside the navigation" in {
      val doc = document(viewAtPath(recordUrl))

      doc.getElementById("nav-home").attr("aria-current")   shouldBe ""
      doc.getElementById("nav-client").attr("aria-current") shouldBe ""
      doc.getElementById("nav-year").attr("aria-current")   shouldBe ""
    }

    "render the Welsh language selector inside the service navigation" in {
      document(viewViaApply(welshMessages))
        .select(".hmrc-service-navigation-language-select")
        .isEmpty shouldBe false
    }
  }
}
