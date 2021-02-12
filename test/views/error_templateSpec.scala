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

package views

import support.GuiceAppSpec
import uk.gov.hmrc.urls.Link
class error_templateSpec extends GuiceAppSpec with TestAppConfig {

  trait ViewFixture extends Fixture {
    val headingText = "error heading"
    val titleText = "error title"
    val messageText = "error message"
  }

  "error_template view" must {
    "have correct title and heading" in new ViewFixture {
      val view = views.html.error_template(titleText, headingText,messageText,gaEventId = Some("unauthorised"))

      doc.title must be(titleText)
      val foundHeading = doc.body().select("#error-heading")
      foundHeading.size mustBe 1
      foundHeading.get(0).text() mustBe headingText
      val foundMessage = doc.body().select("#error-message")
      foundMessage.size mustBe 1
      foundMessage.get(0).text() mustBe messageText
      doc.select("script").toString contains
      "ga('send', { hitType: 'event', eventCategory: 'content - view', eventAction: 'TaxHistory', eventLabel: 'unauthorised'})" mustBe true

    }

    "include sidebar links" in new ViewFixture {
      val link = Link.toExternalPage(id=Some("sidebar-link"), url="http://www.google.com", value=Some("Back To Google")).toHtml
      val view = views.html.error_template(titleText, headingText,messageText, Some(link))

      val sideBarLinks = doc.select("#sidebar-link")
      sideBarLinks.size mustBe 1
      val sideBarLink = sideBarLinks.get(0)
      sideBarLink.text must include("Back To Google")
    }
  }

}
