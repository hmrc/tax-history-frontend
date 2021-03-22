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

package views.errors

import models.taxhistory.Person
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import views.Fixture
import views.html.errors.no_data

class no_dataSpec extends GuiceAppSpec {


  "no data available view" must {

    "have correct title, heading and GA page view event" in new Fixture {
      val person: Person = Person(Some("first name"), Some("second name"), deceased = Some(false))
      val nino: String = "QQ12345C"
      val view: HtmlFormat.Appendable = inject[no_data].apply(person, nino, 2017)
      val title: String = Messages("employmenthistory.no.data.title")
      doc.title mustBe title
      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"
      doc.getElementById("back-link").text mustBe Messages("lbl.back")
      doc.select("h1").text() mustBe Messages("employmenthistory.header", person.getName.getOrElse(nino))
      // AFID-462 - temporarily disabled due to security issue
      //doc.getElementById("clientNino").text mustBe nino
      doc.getElementsMatchingOwnText(Messages("employmenthistory.no.data.text")).text mustBe Messages("employmenthistory.no.data.text")
      doc.getElementById("selectClient").attr("href") mustBe "/tax-history/select-client"
      doc.getElementById("selectTaxYear").attr("href") mustBe "/tax-history/select-tax-year"

    }
  }

}
