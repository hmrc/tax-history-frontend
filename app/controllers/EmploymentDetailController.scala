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

package controllers

import java.util.UUID
import javax.inject.Inject

import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import form.SelectClientForm.selectClientForm
import model.api.{Allowance, Employment, PayAndTax, EarlierYearUpdate}
import models.taxhistory.Person
import org.joda.time.LocalDate
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.time.TaxYearResolver
import uk.gov.hmrc.urls.Link

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmploymentDetailController @Inject()(
                                             val taxHistoryConnector: TaxHistoryConnector,
                                             val citizenDetailsConnector: CitizenDetailsConnector,
                                             override val authConnector: FrontendAuthConnector,
                                             override val config: Configuration,
                                             override val env: Environment,
                                             implicit val messagesApi: MessagesApi
                                           ) extends BaseController {

  def getEmploymentDetails() = Action.async {
    implicit request => {
      val cy1 = TaxYearResolver.currentTaxYear - 1

      val emp1 =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "578/U662",
        employerName = "Aviva Pensions",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2017-01-01"))
      )

      val eyu1 = EarlierYearUpdate(
        taxablePayEYU = 0,
        taxEYU = 8.99,
        receivedDate = LocalDate.parse("2016-01-21")
      )

      val eyu2 = EarlierYearUpdate(
        taxablePayEYU = 10,
        taxEYU = 18.99,
        receivedDate = LocalDate.parse("2016-05-21")
      )

      val eyuList = List(eyu1, eyu2)

      val payAndTax = PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxTotal = Some(979.36),
        earlierYearUpdates = eyuList
      )

      val sidebarLink = Link.toInternalPage(
        url = "client-employment-history",
        value = Some(messagesApi("employmenthistory.payerecord.linktext"))).toHtml

      Future.successful(Ok(views.html.taxhistory.employment_detail(cy1, emp1, payAndTax, Some(sidebarLink))))
    }
  }


}