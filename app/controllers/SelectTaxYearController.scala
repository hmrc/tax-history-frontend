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

import config.FrontendAuthConnector
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import form.SelectTaxYearForm.selectTaxYearForm
import model.api.IndividualTaxYear
import models.taxhistory.{Person, SelectTaxYear}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.time.TaxYear
import utils.DateHelper
import views.html.taxhistory.select_tax_year

import scala.concurrent.Future

class SelectTaxYearController @Inject()(
                                         val taxHistoryConnector: TaxHistoryConnector,
                                         val citizenDetailsConnector: CitizenDetailsConnector,
                                         override val authConnector: FrontendAuthConnector,
                                         override val config: Configuration,
                                         override val env: Environment,
                                         implicit val messagesApi: MessagesApi
                                       ) extends BaseController {



  private def getTaxYears(taxYearList: List[IndividualTaxYear]) = {
    taxYearList.map {
      taxYear =>
        taxYear.year.toString -> Messages("employmenthistory.select.tax.year.option",
          DateHelper.formatDate(TaxYear(taxYear.year).starts),
          DateHelper.formatDate(TaxYear(taxYear.year).finishes))
    }
  }

  private def fetchTaxYearsAndRenderPage(form: Form[SelectTaxYear], httpStatus: Status, person: Person, nino: Nino)
                                        (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    taxHistoryConnector.getTaxYears(nino) map { taxYearResponse =>
      taxYearResponse.status match {
        case OK => {
          val taxYears = getTaxYears(taxYearResponse.json.as[List[IndividualTaxYear]])
          val formData = httpStatus match {
            case Ok => selectTaxYearForm.bind(Json.obj(
              "selectTaxYear" -> taxYears.head._1
            ))
            case _ => form
          }
          httpStatus(select_tax_year(formData,
            person.getName.fold(nino.nino)(x => x), taxYears))
        }
        case status => handleHttpFailureResponse(status, nino)
      }
    }
  }

  private def renderSelectTaxYearPage(nino: Nino, form: Form[SelectTaxYear], httpStatus: Status)
                                     (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    retrieveCitizenDetails(nino, citizenDetailsConnector.getPersonDetails(nino)) flatMap {
      case Left(citizenStatus) => redirectToClientErrorPage(citizenStatus)
      case Right(person) => fetchTaxYearsAndRenderPage(form, httpStatus, person, nino)
    }
  }

  def getSelectTaxYearPage: Action[AnyContent] = Action.async { implicit request =>
    authorisedForAgent {nino =>
      renderSelectTaxYearPage(nino, selectTaxYearForm, Ok)
    }
  }

  def submitSelectTaxYearPage(): Action[AnyContent] = Action.async { implicit request =>
    selectTaxYearForm.bindFromRequest().fold(
      formWithErrors ⇒ {
        val maybeNino = request.session.get("USER_NINO").map(Nino(_))
        maybeNino match {
          case Some(nino) => renderSelectTaxYearPage(nino, formWithErrors, BadRequest)
          case None => Future.successful(Redirect(routes.SelectClientController.getSelectClientPage()))
        }
      },
      validFormData => authorisedForAgent {_ =>
        Future.successful(Redirect(routes.EmploymentSummaryController.getTaxHistory(
          validFormData.taxYear.getOrElse(throw new NoSuchElementException()).toInt)))
      }
    )
  }
}


