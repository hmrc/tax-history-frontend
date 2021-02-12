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

class not_authorisedSpec extends GuiceAppSpec with TestAppConfig {

  trait ViewFixture extends Fixture with TestUtil {
    val nino = randomNino
  }

  "not authorised view" must {

    "have correct title, heading and GA page view event" in new ViewFixture {

      val view = views.html.errors.not_authorised(Some(nino))

      val title = Messages("employmenthistory.not.authorised.title")
      doc.title mustBe title
      doc.getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
      doc.getElementById("back-link").text mustBe Messages("lbl.back")
      doc.select("h1").text() mustBe Messages("employmenthistory.not.authorised.header")
      doc.getElementsMatchingOwnText(Messages("employmenthistory.not.authorised.text1", nino.value)) should not be empty
      doc.getElementsMatchingOwnText(Messages("employmenthistory.not.authorised.text2")) should not be empty
      doc.getElementsMatchingOwnText(Messages("employmenthistory.not.authorised.text3")) should not be empty
      doc.getElementsMatchingOwnText(Messages("employmenthistory.not.authorised.select.client.link.text")).attr("href") mustBe "/tax-history/select-client"
      doc.getElementsMatchingOwnText(Messages("employmenthistory.not.authorised.invite.client.link.text"))  should not be empty
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
      doc.getElementById("service").`val` mustBe "PERSONAL-INCOME-RECORD"
      doc.getElementById("clientIdentifier").`val` mustBe s"$nino"
      doc.getElementsByTag("form").attr("action") mustBe "fakeurl?continue=%2Ftax-history%2Fselect-client&error=%2Ftax-history%2Fnot-authorised"
    }
  }

}
