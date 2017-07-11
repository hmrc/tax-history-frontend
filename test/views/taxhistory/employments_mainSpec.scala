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

import models.taxhistory.Employment
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.urls.Link
import views.{Fixture, GenericTestHelper}

class employments_mainSpec extends GenericTestHelper with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino = "AA000000A"
    val taxYear = 2017

  }

  "employments_main view" must {
    "have correct title and heading" in new ViewFixture {

      val employments = Seq(Employment("AA12341234", "Test Employer Name", Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0)))
      val view = views.html.taxhistory.employments_main( nino, taxYear, employments)

      val title = Messages("employmenthistory.title")
      heading.html must be(nino)
      doc.body().getElementById("taxYear").text() must be(Messages("employmenthistory.taxyear",taxYear+"/"+(taxYear+1)))

    }

    "include employment breakdown" in new ViewFixture {

      val employments = Seq(Employment("AA12341234", "Test Employer Name", Some(25000.0), Some(2000.0), Some(1000.0), Some(250.0)))
      val view = views.html.taxhistory.employments_main(nino, taxYear, employments)

      val tableRowPay = doc.select(".employment-table tbody tr").get(0)

      tableRowPay.text must include(employments.head.taxablePayTotal.get.toString())
    }

    "allow partial employment top be displayed" in new ViewFixture {

      val employments = Seq(Employment("AA12341234", "Test Employer Name", None, None, None, None))
      val view = views.html.taxhistory.employments_main(nino, taxYear, employments)

      val tableRowPay = doc.select(".employment-table tbody tr").get(0)

      tableRowPay.text must include(messagesApi("employmenthistory.nopaydata"))
    }

    "include sidebar links" in new ViewFixture {
      val link = Link.toExternalPage(id=Some("sidebarLink"), url="http://www.google.com", value=Some("Back To Google")).toHtml
      val employments = Seq()
      val view = views.html.taxhistory.employments_main(nino, taxYear, employments, Some(link))

      val sideBarLinks = doc.select("#sidebarLink")
      sideBarLinks.size mustBe 1
      val sideBarLink = sideBarLinks.get(0)
      sideBarLink.text must include("Back To Google")
    }
  }

}
