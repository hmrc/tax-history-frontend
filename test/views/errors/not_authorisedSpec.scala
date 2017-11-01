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

package views.errors

import play.api.i18n.Messages
import support.GuiceAppSpec
import utils.TestUtil
import views.Fixture

class not_authorisedSpec extends GuiceAppSpec {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)
    val nino = TestUtil.randomNino.toString()
  }

  "not authorised view" must {

    "have correct title, heading and GA page view event" in new ViewFixture {

      val view = views.html.errors.not_authorised(nino)

      val title = Messages("employmenthistory.not.authorised.title")
      doc.title mustBe title
      doc.select("h1").text() mustBe Messages("employmenthistory.not.authorised.header",nino)
      doc.getElementsMatchingOwnText(Messages("lbl.select.new.client")).attr("href") mustBe "/tax-history/agent-account/select-client"
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }
  }

}
