/*
 * Copyright 2018 HM Revenue & Customs
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
import utils.TestUtil
import views.{Fixture, TestAppConfig}

class no_dataSpec extends GuiceAppSpec with TestAppConfig {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
    val nino = TestUtil.randomNino.toString()
  }

  "no data available view" must {

    "have correct title, heading and GA page view event" in new ViewFixture {

      val view = views.html.errors.no_data(nino)

      val title = Messages("employmenthistory.no.data.title")
      doc.title mustBe title
      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-tax-year"
      doc.getElementById("back-link").text mustBe Messages("lbl.back")
      doc.select("h1").text() mustBe Messages("employmenthistory.no.data.header",nino)
      doc.getElementsMatchingOwnText(Messages("employmenthistory.no.data.text")).text mustBe Messages("employmenthistory.no.data.text")
      doc.getElementById("selectClient").attr("href") mustBe "/tax-history/select-client"
      doc.getElementById("selectTaxYear").attr("href") mustBe "/tax-history/select-tax-year"

      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }
  }

}
