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
import views.{Fixture, GenericTestHelper}

class employments_mainSpec extends GenericTestHelper with MustMatchers {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val name = "Test User"
    val nino = "AA000000A"
    val taxYear = 2017

  }

  "employments_main view" must {
    "have correct title and heading" in new ViewFixture {

      val employments = Seq(Employment("AA12341234", "Test Employer Name", 25000.0, 2000.0, Some(1000.0), Some(250.0)))
      val view = views.html.taxhistory.employments_main(name, nino, taxYear, employments)

      val title = Messages("employmenthistory.title")

      doc.title mustBe title
      heading.html must be(Messages("employmenthistory.heading", name, nino, taxYear+"/"+(taxYear+1)))

    }

    "include employment breakdown" in new ViewFixture {

      val employments = Seq(Employment("AA12341234", "Test Employer Name", 25000.0, 2000.0, Some(1000.0), Some(250.0)))
      val view = views.html.taxhistory.employments_main(name, nino, taxYear, employments)

      val tableRowPay = doc.select(".employment-table tbody tr").get(0)

      tableRowPay.text must include(employments.head.taxablePayTotal.toString())

    }
  }

}
