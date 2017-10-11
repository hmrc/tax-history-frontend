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
import model.api.Employment
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

  def getTaxHistory() = Action.async {
    implicit request => {
      val nino = request.session.get("USER_NINO").map(Nino(_))
      authorisedForAgent {
        for {maybePerson <- retrieveCitizenDetails(nino)
             taxHistoryResponse <- renderTaxHistoryPage(nino, maybePerson)}
          yield {
            taxHistoryResponse
          }
      }
    }
  }

  def retrieveCitizenDetails(ninoField: Option[Nino])(implicit hc: HeaderCarrier, request: Request[_]): Future[Either[Int, Person]] = ninoField match {
    case Some(nino) => {
      citizenDetailsConnector.getPersonDetails(nino) map {
        personResponse =>
          personResponse.status match {
            case OK => {
              val person = personResponse.json.as[Person]
              if (person.deceased) Left(LOCKED) else Right(person)
            }
            case status => Left(status)
          }
      }
    }.recoverWith {
      case _ => Future.successful(Left(BAD_REQUEST))
    }
    case _ => Future.successful(Left(BAD_REQUEST))
  }

  def renderTaxHistoryPage(ninoField: Option[Nino], maybePerson: Either[Int, Person])
                          (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    maybePerson match {
      case Left(status) => status match {
        case LOCKED => Future.successful(handleHttpResponse("notfound",
          FrontendAppConfig.AfiHomePage, Some(ninoField.fold("")(nino => nino.toString()))))
        case _ => retrieveTaxHistoryData(ninoField, None)
      }
      case Right(person) => retrieveTaxHistoryData(ninoField, Some(person))
    }
  }

  def retrieveTaxHistoryData(ninoField: Option[Nino], person: Option[Person])
                            (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = ninoField match {
    case Some(nino) =>
      val cy1 = TaxYearResolver.currentTaxYear - 1
      taxHistoryConnector.getTaxHistory(nino, cy1) map {
        historyResponse =>
          historyResponse.status match {
            case OK => {
              val employments = historyResponse.json.as[List[Employment]]
              val sidebarLink = Link.toInternalPage(
                url = FrontendAppConfig.AfiHomePage,
                value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
              Ok(views.html.taxhistory.employment_summary(nino.nino, cy1,
                employments, person, Some(sidebarLink))).removingFromSession("USER_NINO")
            }
            case NOT_FOUND => {
              handleHttpResponse("notfound", FrontendAppConfig.AfiHomePage, Some(nino.toString()))
            }
            case UNAUTHORIZED => {
              handleHttpResponse("unauthorised",
                controllers.routes.SelectClientController.getSelectClientPage().url, Some(nino.toString()))
            }
            case s => {
              Logger.warn("Error response returned with status:" + s)
              handleHttpResponse("technicalerror", FrontendAppConfig.AfiHomePage, None)
            }
          }
      }
    case _ =>
      Logger.warn("No nino supplied.")
      val sidebarLink = Link.toInternalPage(
        url = FrontendAppConfig.AfiHomePage,
        value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
      Future.successful(Ok(views.html.taxhistory.select_client(selectClientForm, Some(sidebarLink))))
  }
}