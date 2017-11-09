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
import models.taxhistory.Person
import org.joda.time.LocalDate
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
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

  private val APRIL = 4
  private val DATE_06 = 6
  private val DATE_05 = 5

  private def getTaxYears(taxYearList: List[IndividualTaxYear]) = {
    taxYearList.map {
      taxYear =>
        taxYear.year.toString -> Messages("employmenthistory.select.tax.year.option",
          DateHelper.formatDate(new LocalDate(taxYear.year, APRIL, DATE_06)),
          DateHelper.formatDate(new LocalDate(taxYear.year + 1, APRIL , DATE_05)))
    }
  }

  val taxYearList = List(IndividualTaxYear(year = 2016,
    allowancesURI = "/2016/allowances",
    employmentsURI = "/2016/employments"),
    IndividualTaxYear(year = 2015,
      allowancesURI = "/2015/allowances",
      employmentsURI = "/2015/employments"))

  private def renderSelectClientPage(nino: Nino, response: Either[Int, Person])
                            (implicit hc: HeaderCarrier, request: Request[_]): Future[Result]= {
    response match {
      case Left(status) => redirectToClientErrorPage(status)
      case Right(person) => {
        val taxTears = getTaxYears(taxYearList)
        val preSelectedForm = selectTaxYearForm.bind(Json.obj(
          "selectTaxYear" -> taxTears.head._1
        ))
        Future.successful(Ok(select_tax_year(preSelectedForm, person.getName.fold(nino.nino)(x => x), taxTears)))
      }
    }
  }

  def getSelectTaxYearPage: Action[AnyContent] = Action.async { implicit request =>
    val maybeNino = request.session.get("USER_NINO").map(Nino(_))
    authorisedForAgent {
      maybeNino match {
        case Some(nino) => {
            retrieveCitizenDetails(nino, citizenDetailsConnector) flatMap { response =>
              renderSelectClientPage(nino, response)
            }
        }
        case None => {
          Future.successful(Redirect(routes.SelectClientController.getSelectClientPage()))
        }
      }
    }
  }

  def submitSelectTaxYearPage(): Action[AnyContent] = Action.async {implicit request =>
    selectTaxYearForm.bindFromRequest().fold(
      formWithErrors ⇒ {
        val nino = request.session.get("USER_NINO").map(Nino(_)).get
        retrieveCitizenDetails(nino, citizenDetailsConnector) flatMap {
            case Left(status) => redirectToClientErrorPage(status)
            case Right(person) => Future.successful(BadRequest(select_tax_year(formWithErrors,
              person.getName.fold(nino.nino)(x => x), getTaxYears(taxYearList))))
        }
      },
      validFormData => {
        Future.successful(Redirect(routes.EmploymentSummaryController.getTaxHistory(validFormData.taxYear.toInt)))
      }
    )
  }
}
