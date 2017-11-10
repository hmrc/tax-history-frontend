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

import javax.inject.Inject

import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import form.SelectClientForm.selectClientForm
import model.api.{Allowance, Employment}
import models.taxhistory.Person
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.time.TaxYearResolver
import uk.gov.hmrc.urls.Link

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
      val maybeNino = request.session.get("USER_NINO").map(Nino(_))
      authorisedForAgent {
        maybeNino match {
          case Some(nino) => {
            for {
              maybePerson <- retrieveCitizenDetails(nino, citizenDetailsConnector)
              taxHistoryResponse <- renderTaxHistoryPage(nino, maybePerson, taxYear)
            } yield taxHistoryResponse
          }
          case None => {
            Logger.warn("No nino supplied.")
            val navLink = Link.toInternalPage(
              url = FrontendAppConfig.AfiHomePage,
              value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
            Future.successful(Ok(views.html.taxhistory.select_client(selectClientForm, Some(navLink))))
          }
        }
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
        case OK => {
          taxHistoryConnector.getAllowances(ninoField, taxYear) map { allowanceResponse =>
            allowanceResponse.status match {
              case OK | NOT_FOUND =>
                val employments = empResponse.json.as[List[Employment]]
                val allowances = allowanceResponse.json.as[List[Allowance]]

                Ok(views.html.taxhistory.employment_summary(ninoField.nino, taxYear,
                  employments, allowances, person))//.removingFromSession("USER_NINO")
              case status => handleHttpFailureResponse(status, ninoField)
            }
          }
        }
        case status => Future.successful(handleHttpFailureResponse(status, ninoField))
      }
    }
  }
}