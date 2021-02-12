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

import play.api.i18n.Messages
import support.GuiceAppSpec
import views.{Fixture, TestAppConfig}

class deceasedSpec extends GuiceAppSpec with TestAppConfig {


  "Deceased view" must {

    "have correct title, heading and GA page view" in new Fixture {

      val view = views.html.errors.deceased()

      val title = Messages("employmenthistory.deceased.title")
      doc.title mustBe title
      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
      doc.getElementById("back-link").text mustBe Messages("lbl.back")
      doc.select("h1").text() mustBe Messages("employmenthistory.deceased.header")
      doc.getElementsMatchingOwnText(Messages("employmenthistory.deceased.text"))
      doc.getElementsMatchingOwnText(Messages("employmenthistory.deceased.select.client.link.text")).attr("href") mustBe "/tax-history/select-client"
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }
  }

}

