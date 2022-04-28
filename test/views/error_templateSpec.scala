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

package views

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import support.GuiceAppSpec
import uk.gov.hmrc.urls.Link
import views.html.error_template
class error_templateSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  trait ViewFixture extends Fixture {
    val headingText = "error heading"
    val titleText = "error title"
    val messageText = "error message"
  }

  "error_template view" must {
    "have correct title and heading" in new ViewFixture {
      val view: HtmlFormat.Appendable = inject[error_template].apply(titleText, headingText,messageText,gaEventId = Some("unauthorised"))
      doc.title mustBe expectedPageTitle(titleText)
      val foundHeading: Elements = doc.body().select("#error-heading")
      foundHeading.size mustBe 1
      foundHeading.get(0).text() mustBe headingText
      val foundMessage: Elements = doc.body().select("#error-message")
      foundMessage.size mustBe 1
      foundMessage.get(0).text() mustBe messageText

    }

    "include sidebar links" in new ViewFixture {
      val link: Html = Link.toExternalPage(id=Some("sidebar-link"), url="http://www.google.com", value=Some("Back To Google")).toHtml
      val view: HtmlFormat.Appendable = inject[error_template].apply(titleText, headingText,messageText, Some(link))

      val sideBarLinks: Elements = doc.select("#sidebar-link")
      sideBarLinks.size mustBe 1
      val sideBarLink: Element = sideBarLinks.get(0)
      sideBarLink.text must include("Back To Google")
    }
  }

}
