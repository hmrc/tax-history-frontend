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

package views.errors

import models.taxhistory.Person
import org.jsoup.nodes.Element
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import utils.TestUtil
import views.html.errors.no_data
import views.{BaseViewSpec, Fixture}

class NoDataSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  trait NoDataFixture extends Fixture {
    val firstName      = "testFirstName"
    val surname        = "testSurname"
    val person: Person = Person(Some(firstName), Some(surname), deceased = Some(false))
    val nino: String   = TestUtil.randomNino.toString()
    val taxYear        = 2017

    val view: HtmlFormat.Appendable                = inject[no_data].apply(person, nino, taxYear)
    val viewHtmlViaRender: HtmlFormat.Appendable   =
      inject[no_data].render(person, nino, taxYear, request, messages, appConfig)
    val viewHtmlViaFunction: HtmlFormat.Appendable =
      inject[no_data].f(person, nino, taxYear)(request, messages, appConfig)
  }

  "NoDataAvailable view" when {

    "view.apply()" should {

      "have the correct title" in new NoDataFixture {
        document(view).title mustBe expectedPageTitle(messages("employmenthistory.no.data.title"))
      }

      "have the correct header section" in new NoDataFixture {
        val preHeaderElement: Element          = document(view).getElementById("pre-header")
        val preHeaderWithoutHiddenText: String = preHeaderElement.ownText()
        val preHeader: String                  = preHeaderElement.text()

        heading.text() mustBe messages("employmenthistory.no.data.header")
        preHeaderWithoutHiddenText mustBe s"$firstName $surname"
        preHeader mustBe s"This section relates to $firstName $surname"
      }

      "have correct heading and GA page view event" in new NoDataFixture {
        document(view).getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"
        document(view).getElementById("back-link").text mustBe Messages("lbl.back")
        document(view).getElementsMatchingOwnText(Messages("employmenthistory.no.data.text")).text mustBe
          Messages("employmenthistory.no.data.text")
        document(view).getElementById("selectClient").attr("href") mustBe "/tax-history/select-client"
        document(view).getElementById("selectTaxYear").attr("href") mustBe "/tax-history/select-tax-year"
      }
    }

    "view.render()" should {

      "have the correct title" in new NoDataFixture {
        document(viewHtmlViaRender).title mustBe expectedPageTitle(messages("employmenthistory.no.data.title"))
      }

      "have the correct header section" in new NoDataFixture {
        val preHeaderElement: Element          = document(viewHtmlViaRender).getElementById("pre-header")
        val preHeaderWithoutHiddenText: String = preHeaderElement.ownText()
        val preHeader: String                  = preHeaderElement.text()

        heading.text() mustBe messages("employmenthistory.no.data.header")
        preHeaderWithoutHiddenText mustBe s"$firstName $surname"
        preHeader mustBe s"This section relates to $firstName $surname"
      }

      "have correct heading and GA page viewHtmlViaRender event" in new NoDataFixture {
        document(viewHtmlViaRender).getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"
        document(viewHtmlViaRender).getElementById("back-link").text mustBe Messages("lbl.back")
        document(viewHtmlViaRender).getElementsMatchingOwnText(Messages("employmenthistory.no.data.text")).text mustBe
          Messages("employmenthistory.no.data.text")
        document(viewHtmlViaRender).getElementById("selectClient").attr("href") mustBe "/tax-history/select-client"
        document(viewHtmlViaRender).getElementById("selectTaxYear").attr("href") mustBe "/tax-history/select-tax-year"
      }
    }

    "view.f()" should {

      "have the correct title" in new NoDataFixture {
        document(viewHtmlViaFunction).title mustBe expectedPageTitle(messages("employmenthistory.no.data.title"))
      }

      "have the correct header section" in new NoDataFixture {
        val preHeaderElement: Element          = document(viewHtmlViaFunction).getElementById("pre-header")
        val preHeaderWithoutHiddenText: String = preHeaderElement.ownText()
        val preHeader: String                  = preHeaderElement.text()

        heading.text() mustBe messages("employmenthistory.no.data.header")
        preHeaderWithoutHiddenText mustBe s"$firstName $surname"
        preHeader mustBe s"This section relates to $firstName $surname"
      }

      "have correct heading and GA page viewHtmlViaFunction event" in new NoDataFixture {
        document(viewHtmlViaFunction).getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"
        document(viewHtmlViaFunction).getElementById("back-link").text mustBe Messages("lbl.back")
        document(viewHtmlViaFunction).getElementsMatchingOwnText(Messages("employmenthistory.no.data.text")).text mustBe
          Messages("employmenthistory.no.data.text")
        document(viewHtmlViaFunction).getElementById("selectClient").attr("href") mustBe "/tax-history/select-client"
        document(viewHtmlViaFunction).getElementById("selectTaxYear").attr("href") mustBe "/tax-history/select-tax-year"
      }
    }
  }
}
