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
import utils.TestUtil
import views.{Fixture, TestAppConfig}

class mci_restrictedSpec extends GuiceAppSpec with TestAppConfig {

  trait ViewFixture extends Fixture {
    val nino = TestUtil.randomNino.toString()
  }

  "MCI restricted view" must {

    "have correct title, heading and GA page view event" in new ViewFixture {

      val view = views.html.errors.mci_restricted()

      val title = Messages("employmenthistory.mci.restricted.title")
      doc.title mustBe title
      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
      doc.getElementById("back-link").text mustBe Messages("lbl.back")
      doc.getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.text")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.telephone")).hasText mustBe true
      doc.getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.textphone")).hasText mustBe true
      doc.select("h1").text() mustBe Messages("employmenthistory.mci.restricted.header")
      doc.getElementsMatchingOwnText(Messages("employmenthistory.mci.restricted.select.client.link.text")).attr("href") mustBe "/tax-history/select-client"
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }
  }

}
