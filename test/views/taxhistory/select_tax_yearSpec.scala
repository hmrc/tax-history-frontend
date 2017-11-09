/*
 * Copyright 2017 HM Revenue & Customs
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
import support.GuiceAppSpec
import views.Fixture

class select_tax_yearSpec extends GuiceAppSpec {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
    val postData = Json.obj("selectTaxYear" -> "2015")
    val validForm = selectTaxYearForm.bind(postData)
  }

  "select_tax_year view" must {

    "have correct title, heading and GA pageview event" in new ViewFixture {

      val view = views.html.taxhistory.select_tax_year(validForm, "Name", List.empty)

      doc.title mustBe Messages("employmenthistory.select.tax.year.title")
      doc.select("h1").text() mustBe Messages("employmenthistory.select.tax.year.header", "Name")
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }

    "show correct content on the page" in new ViewFixture {
      val view = views.html.taxhistory.select_tax_year(validForm, "Name", List())



    }
  }

}

