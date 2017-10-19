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


  def getEmploymentDetails(employmentId: String) = Action.async {
    implicit request => {
      val cy1 = TaxYearResolver.currentTaxYear - 1
      val maybeNino = request.session.get("USER_NINO").map(Nino(_))

      authorisedForAgent {

        taxHistoryConnector.getEmploymentDetails(maybeNino.get, cy1, employmentId) map { empDetailsResponse =>
          empDetailsResponse.status match {
            case OK =>
              val payAndTax = empDetailsResponse.json.as[PayAndTax]
              val sidebarLink = Link.toInternalPage(
                url = "/tax-history/agent-account/client-employment-history",
                value = Some(messagesApi("employmenthistory.payerecord.linktext"))).toHtml
              Ok(views.html.taxhistory.employment_detail(cy1, payAndTax, List.empty, Some(sidebarLink)))
            case status => handleHttpFailureResponse(status, maybeNino.get)
          }

        }
      }
    }
  }
}