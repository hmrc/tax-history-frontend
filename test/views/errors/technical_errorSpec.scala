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

class technical_errorSpec extends GuiceAppSpec with TestAppConfig {


  "Technical Error view" must {

    "have correct title, heading and GA page view event" in new Fixture {

      val view = views.html.errors.technical_error()

      val title = Messages("employmenthistory.technical.error.title")
      doc.title mustBe title
      doc.getElementById("back-link").attr("href").contains(appConfig.agentAccountHomePage) mustBe true
      doc.getElementById("back-link").text mustBe Messages("lbl.back")
      doc.select("h1").text() mustBe Messages("employmenthistory.technical.error.header")
      doc.getElementsMatchingOwnText(Messages("employmenthistory.technical.error.message")).size() mustBe 1
      doc.select("script").toString contains
        "ga('send', 'pageview', { 'anonymizeIp': true })" mustBe true
    }
  }

}
