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

import models.taxhistory._
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import uk.gov.hmrc.urls.Link
import utils.{Currency, DateHelper, TestUtil}
import views.{Fixture, GenericTestHelper}

class employment_summarySpec extends GenericTestHelper with MustMatchers with Constants {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino = TestUtil.randomNino.toString()
    val taxYear = 2017
    val person = Some(Person(Some("James"),Some("Dean"),false))

  }

  "employment_summary view" must {

    "have correct title and heading" in new ViewFixture {
      val view = views.html.taxhistory.employment_summary(nino, taxYear, payePartialModel, None)

      val title = Messages("employmenthistory.title")
      doc.title mustBe title
      doc.getElementsByTag("h1").first().html must be(Messages("employmenthistory.header", nino))
      doc.getElementsByClass("heading-secondary").first().html must be(Messages("employmenthistory.taxyear", taxYear.toString,
        (taxYear+1).toString))
      doc.select("script").toString contains
        "ga('send', {hitType: 'event', eventCategory: 'content - view', eventAction: 'TaxHistory', eventLabel: 'EmploymentDetails'}" mustBe true
    }
  }
}

trait Constants {

  val startDate = new LocalDate("2016-01-21")
  val endDate = new LocalDate("2016-11-01")

  val emp1 =  Employment("12341234", "Test Employer Name",  startDate, None, Some(25000.0), Some(2000.0), List.empty,
    List.empty)
  val emp2 = Employment("11111111", "Test Employer Name",  startDate, None, Some(25000.0), Some(2000.0), List.empty,
    List.empty)
  val employments = List(emp1,emp2)


  val partialEmploymentList = List(Employment("12341234", "Test Employer Name", startDate, Some(endDate),None, None))
  val payePartialModel = PayAsYouEarnDetails(partialEmploymentList, List.empty)
}
