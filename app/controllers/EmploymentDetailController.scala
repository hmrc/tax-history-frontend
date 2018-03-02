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

import config.{AppConfig, FrontendAuthConnector}
import connectors.{CitizenDetailsConnector, TaxHistoryConnector}
import model.api.IncomeSource._
import model.api._
import models.taxhistory.Person
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmploymentDetailController @Inject()(val taxHistoryConnector: TaxHistoryConnector,
                                           val citizenDetailsConnector: CitizenDetailsConnector,
                                           override val authConnector: FrontendAuthConnector,
                                           override val config: Configuration,
                                           override val env: Environment,
                                           implicit val messagesApi: MessagesApi,
                                           implicit val appConfig: AppConfig
                                          ) extends BaseController {

  val loginContinue: String = appConfig.loginContinue
  val serviceSignout: String = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  private def renderEmploymentDetailsPage(nino: Nino, taxYear: Int, employmentId: String)
                                         (implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    retrieveCitizenDetails(nino, citizenDetailsConnector.getPersonDetails(nino)) flatMap {
      case Left(citizenStatus) => redirectToClientErrorPage(citizenStatus)
      case Right(person) =>
        taxHistoryConnector.getEmployment(nino, taxYear, employmentId) flatMap { empDetailsResponse =>
          empDetailsResponse.status match {
            case OK =>
              loadEmploymentDetailsPage(empDetailsResponse, nino, taxYear, employmentId, person)
            case NOT_FOUND => Future.successful(Redirect(routes.EmploymentSummaryController.getTaxHistory(taxYear)))
            case status => Future.successful(handleHttpFailureResponse(status, nino))
          }
        }
    }
  }

  def getEmploymentDetails(employmentId: String, taxYear: Int): Action[AnyContent] = Action.async {
    implicit request =>
      authorisedForAgent { nino =>
        renderEmploymentDetailsPage(nino, taxYear, employmentId)
      }
  }

  private def getPayAndTax(nino: Nino, taxYear: Int, employmentId: String)
                          (implicit hc: HeaderCarrier, request: Request[_]): Future[Option[PayAndTax]] = {
    {
      taxHistoryConnector.getPayAndTaxDetails(nino, taxYear, employmentId) map { payAndTaxResponse =>
        val result = payAndTaxResponse.json.as[PayAndTax]
        if (appConfig.studentLoanFlag) Some(result) else Some(result.copy(studentLoan = None))
      }
    }.recoverWith {
      case e =>
        logger.warn(s"getPayAndTaxDetails failed with ${e.getMessage}")
        Future.successful(None)
    }
  }

  private def getCompanyBenefits(nino: Nino, taxYear: Int, employmentId: String)
                                (implicit hc: HeaderCarrier, request: Request[_]): Future[List[CompanyBenefit]] = {
    {
      taxHistoryConnector.getCompanyBenefits(nino, taxYear, employmentId) map { cbResponse =>
        cbResponse.json.as[List[CompanyBenefit]]
      }
    }.recoverWith {
      case e =>
        logger.warn(s"getCompanyBenefits failed with ${e.getMessage}")
        Future.successful(List.empty)
    }
  }

  private def getActualOrEstimateFlag(companyBenefits: List[CompanyBenefit]): Boolean = {
    val P9D = 3
    val P11D = 21
    val Assessed_P11D = 28
    val P11D_P9D = 29

    companyBenefits.exists(cb => cb.source.contains(P9D)
      || cb.source.contains(P11D)
      || cb.source.contains(Assessed_P11D)
      || cb.source.contains(P11D_P9D))

  }

  private def getIncomeSource(nino: Nino, taxYear: Int, employmentId: String)
                             (implicit hc: HeaderCarrier, request: Request[_]): Future[Option[IncomeSource]] = {
    {
      taxHistoryConnector.getIncomeSource(nino, taxYear, employmentId) map { iSResponse =>
        if (iSResponse.body.isEmpty) {
          None
        } else {
          Some(iSResponse.json.as[IncomeSource])
        }
      }
    }.recoverWith {
      case e =>
        logger.warn(s"getIncomeSource failed with ${e.getMessage}")
        Future.successful(None)
    }
  }

  private def loadEmploymentDetailsPage(empResponse: HttpResponse,
                                        nino: Nino,
                                        taxYear: Int,
                                        employmentId: String,
                                        person: Person)(implicit hc: HeaderCarrier, request: Request[_]) = {
    val employment = empResponse.json.as[Employment]
    for {
      payAndTax <- getPayAndTax(nino, taxYear, employmentId)
      companyBenefits <- getCompanyBenefits(nino, taxYear, employmentId)
      incomeSource <- getIncomeSource(nino, taxYear, employmentId)
    } yield Ok(views.html.taxhistory.employment_detail(taxYear, payAndTax,
      employment, companyBenefits, person.getName.getOrElse(nino.nino), getActualOrEstimateFlag(companyBenefits), incomeSource))
  }

}