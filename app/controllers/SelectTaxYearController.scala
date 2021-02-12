/*
 * Copyright 2021 HM Revenue & Customs
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

import config.AppConfig
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import form.SelectTaxYearForm.selectTaxYearForm
import javax.inject.Inject
import model.api.IndividualTaxYear
import models.taxhistory.SelectTaxYear
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.time.TaxYear
import utils.DateHelper
import views.html.taxhistory.select_tax_year

import scala.concurrent.{ExecutionContext, Future}

class SelectTaxYearController @Inject()(
                                         val taxHistoryConnector: TaxHistoryConnector,
                                         val citizenDetailsConnector: CitizenDetailsConnector,
                                         override val authConnector: AuthConnector,
                                         override val config: Configuration,
                                         override val env: Environment,
                                         val cc: MessagesControllerComponents,
                                         implicit val appConfig: AppConfig
                                       )(implicit val ec: ExecutionContext) extends BaseController(cc) {

  val loginContinue: String = appConfig.loginContinue
  val serviceSignout: String = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  private def getTaxYears(taxYearList: List[IndividualTaxYear])(implicit request: Request[_]) = {
    taxYearList.map {
      taxYear =>
        taxYear.year.toString -> Messages("employmenthistory.select.tax.year.option",
          DateHelper.formatDate(TaxYear(taxYear.year).starts),
          DateHelper.formatDate(TaxYear(taxYear.year).finishes))
    }
  }

  private def fetchTaxYearsAndRenderPage(form: Form[SelectTaxYear], httpStatus: Status, nino: Nino, clientName:Option[String])
                                        (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    taxHistoryConnector.getTaxYears(nino) map { taxYearResponse =>
      taxYearResponse.status match {
        case OK => {
          val taxYears = getTaxYears(taxYearResponse.json.as[List[IndividualTaxYear]])
          httpStatus(select_tax_year(form, taxYears, clientName, nino.toString))
        }
        case status => {
          handleHttpFailureResponse(status, nino)
        }
      }
    }
  }

  private def renderSelectTaxYearPage(nino: Nino, form: Form[SelectTaxYear], httpStatus: Status)
                                     (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    retrieveCitizenDetails(nino, citizenDetailsConnector.getPersonDetails(nino)) flatMap {
      case Left(citizenStatus) => redirectToClientErrorPage(citizenStatus)
      case Right(p) => fetchTaxYearsAndRenderPage(form, httpStatus, nino, p.getName)
    }
  }

  def getSelectTaxYearPage: Action[AnyContent] = Action.async { implicit request =>
    authorisedForAgent { nino =>
      renderSelectTaxYearPage(nino, selectTaxYearForm, Ok)
    }
  }

  def submitSelectTaxYearPage(): Action[AnyContent] = Action.async { implicit request =>
    selectTaxYearForm.bindFromRequest().fold(
      formWithErrors ⇒ {
        getNinoFromSession(request) match {
          case Some(nino) => renderSelectTaxYearPage(nino, formWithErrors, BadRequest)
          case None => Future.successful(Redirect(routes.SelectClientController.getSelectClientPage()))
        }
      },
      validFormData => authorisedForAgent { _ =>
        Future.successful(Redirect(routes.EmploymentSummaryController.getTaxHistory(
          validFormData.taxYear.getOrElse(throw new NoSuchElementException()).toInt)))
      }
    )
  }
}


