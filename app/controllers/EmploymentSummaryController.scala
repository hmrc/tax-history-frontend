/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import config.FrontendAuthConnector
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import model.api.{Allowance, Employment, TaxAccount}
import models.taxhistory.Person
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmploymentSummaryController @Inject()(
                                             val taxHistoryConnector: TaxHistoryConnector,
                                             val citizenDetailsConnector: CitizenDetailsConnector,
                                             override val authConnector: FrontendAuthConnector,
                                             override val config: Configuration,
                                             override val env: Environment,
                                             implicit val messagesApi: MessagesApi
                                           ) extends BaseController {

  def getTaxHistory(taxYear: Int) = Action.async {
    implicit request => {
      authorisedForAgent { nino =>
        for {
          maybePerson <- retrieveCitizenDetails(nino, citizenDetailsConnector.getPersonDetails(nino))
          taxHistoryResponse <- renderTaxHistoryPage(nino, maybePerson, taxYear)
        } yield taxHistoryResponse
      }
    }
  }


  private def renderTaxHistoryPage(ninoField: Nino, maybePerson: Either[Int, Person], taxYear: Int)
                                  (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    maybePerson match {
      case Left(status) => redirectToClientErrorPage(status)
      case Right(person) => retrieveTaxHistoryData(ninoField, Some(person), taxYear)
    }
  }

  private def retrieveTaxHistoryData(ninoField: Nino, person: Option[Person], taxYear: Int)
                                    (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    taxHistoryConnector.getEmploymentsAndPensions(ninoField, taxYear) flatMap { empResponse =>
      empResponse.status match {
        case OK =>
          (for {
            allowanceResponse <- taxHistoryConnector.getAllowances(ninoField, taxYear)
            taxAccountResponse <- taxHistoryConnector.getTaxAccount(ninoField, taxYear)
          } yield (allowanceResponse, taxAccountResponse)).map {
            dataResponse =>
              val employments = empResponse.json.as[List[Employment]]
              val allowanceStatus = dataResponse._1.status
              val taxAccountStatus = dataResponse._2.status

              val allowances = if(allowanceStatus == (OK | NOT_FOUND)) dataResponse._1.json.as[List[Allowance]] else {
                Logger.info(s"Unexpected Allowance Status: $allowanceStatus")
                List.empty
              }
              val taxAccount = if(dataResponse._2.status == (OK | NOT_FOUND)) dataResponse._2.json.asOpt[TaxAccount] else {
                Logger.info(s"Unexpected Tax Account Status: $taxAccountStatus")
                None
              }

              Ok(views.html.taxhistory.employment_summary(ninoField.nino, taxYear,
                employments, allowances, person, taxAccount))
          }
        case status => Future.successful(handleHttpFailureResponse(status, ninoField))
      }
    }
  }
}