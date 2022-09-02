/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import utils.TestUtil
import views.{BaseViewSpec, Fixture}
import views.html.errors.no_data

class no_dataSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  trait noDataFixture extends Fixture {
    val firstName      = "testFirstName"
    val surname        = "testSurname"
    val person: Person = Person(Some(firstName), Some(surname), deceased = Some(false))
    val nino: String   = TestUtil.randomNino.toString()
    val taxYear        = 2017

    val view: HtmlFormat.Appendable = inject[no_data].apply(person, nino, taxYear)

  }

  "no data available view" must {

    "have the correct title" in new noDataFixture {
      doc.title mustBe expectedPageTitle(messages("employmenthistory.no.data.title"))
    }

    "have the correct header section" in new noDataFixture {
      val preHeaderElement           = doc.getElementById("pre-header")
      val preHeaderWithoutHiddenText = preHeaderElement.ownText()
      val preHeader                  = preHeaderElement.text()

      heading.text() mustBe messages("employmenthistory.header")
      preHeaderWithoutHiddenText mustBe s"$firstName $surname"
      preHeader mustBe s"This section relates to $firstName $surname"
    }

    "have correct heading and GA page view event" in new noDataFixture {
      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"
      doc.getElementById("back-link").text mustBe Messages("lbl.back")
      doc.getElementsMatchingOwnText(Messages("employmenthistory.no.data.text")).text mustBe Messages(
        "employmenthistory.no.data.text"
      )
      doc.getElementById("selectClient").attr("href") mustBe "/tax-history/select-client"
      doc.getElementById("selectTaxYear").attr("href") mustBe "/tax-history/select-tax-year"
    }
  }

}
