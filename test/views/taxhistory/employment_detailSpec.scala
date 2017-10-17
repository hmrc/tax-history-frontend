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

import java.util.UUID

import model.api.Employment
import model.api.{Allowance, Employment}
import models.taxhistory._
import org.joda.time.LocalDate
import org.scalatest.MustMatchers
import play.api.i18n.Messages
import uk.gov.hmrc.time.TaxYearResolver
import utils.{DateHelper, TestUtil}
import views.{Fixture, GenericTestHelper}

class employment_detailSpec extends GenericTestHelper with MustMatchers with DetailConstants {

  trait ViewFixture extends Fixture {
    implicit val requestWithToken = addToken(request)

    val nino = TestUtil.randomNino.toString()
    val taxYear = 2017
    val person = Some(Person(Some("James"),Some("Dean"),false))
  }

  "employment_detail view" must {

    "have correct title and heading should only show one h1" in new ViewFixture {

      val view = views.html.taxhistory.employment_detail(taxYear, emp1)

      val title = Messages("employmenthistory.title")
      doc.title mustBe title
      doc.getElementsByTag("h1").html must be("employer-1")

    }

    "have correct employment details" in new ViewFixture {

      val view = views.html.taxhistory.employment_detail(taxYear, emp1)
      val payeRef = doc.select(".employment-table tbody tr").get(0)
      val startDate = doc.select(".employment-table tbody tr").get(1)
      val endDate = doc.select(".employment-table tbody tr").get(2)

      payeRef.text must include("paye-1")
      startDate.text must include("21 January 2016")
      endDate.text must include("1 January 2017")

      println(doc.select(".employment-table tbody tr").get(1))

    }
  }

}

trait DetailConstants {

  val startDate = new LocalDate("2016-01-21")
  val endDate = new LocalDate("2016-11-01")

  val emp1 =  Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = LocalDate.parse("2016-01-21"),
    endDate = Some(LocalDate.parse("2017-01-01")))

  val emp2 =  Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-2",
    employerName = "employer-2",
    startDate = LocalDate.parse("2016-01-21"),
    endDate = Some(LocalDate.parse("2017-01-01")))

  val employments = List(emp1,emp2)

  val allowance1 = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "FlatRateJobExpenses",
    amount = BigDecimal(12.00))
  val allowance2 = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "ProfessionalSubscriptions",
    amount = BigDecimal(22.00))
  val allowance3 = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "EarlierYearsAdjustment",
    amount = BigDecimal(32.00))

  val allowances = List(allowance1, allowance2, allowance3)

}
