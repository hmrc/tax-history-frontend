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
import javax.inject.Inject
import model.api.IncomeSource._
import model.api._
import models.taxhistory.Person
import play.api.mvc.{MessagesControllerComponents, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class EmploymentDetailController @Inject()(val taxHistoryConnector: TaxHistoryConnector,
                                           val citizenDetailsConnector: CitizenDetailsConnector,
                                           override val authConnector: AuthConnector,
                                           override val config: Configuration,
                                           override val env: Environment,
                                           val cc: MessagesControllerComponents
                                          )(implicit val ec: ExecutionContext, val appConfig: AppConfig) extends BaseController(cc) {

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
                          (implicit hc: HeaderCarrier): Future[Option[PayAndTax]] = {
    taxHistoryConnector.getPayAndTaxDetails(nino, taxYear, employmentId).map{ payAndTaxResponse =>
      payAndTaxResponse.status match {
        case NOT_FOUND => None
        case _ => {
          val result = payAndTaxResponse.json.as[PayAndTax]
          if (appConfig.studentLoanFlag) Some(result) else Some(result.copy(studentLoan = None))
        }
      }
    }.recoverWith(recoverWithEmptyDefault("getPayAndTaxDetails", None))
  }

  private def getCompanyBenefits(nino: Nino, taxYear: Int, employmentId: String)
                                (implicit hc: HeaderCarrier): Future[List[CompanyBenefit]] = {
    taxHistoryConnector.getCompanyBenefits(nino, taxYear, employmentId).map { cbResponse =>
      cbResponse.status match {
        case NOT_FOUND => List.empty
        case _ => cbResponse.json.as[List[CompanyBenefit]]
      }
    }.recoverWith(recoverWithEmptyDefault("getCompanyBenefits", List.empty))
  }

  private def getIncomeSource(nino: Nino, taxYear: Int, employmentId: String)
                             (implicit hc: HeaderCarrier): Future[Option[IncomeSource]] = {
    taxHistoryConnector.getIncomeSource(nino, taxYear, employmentId).map { iSResponse =>
      iSResponse.status match {
        case NOT_FOUND => None
        case _ => Some(iSResponse.json.as[IncomeSource])
      }
    }.recoverWith(recoverWithEmptyDefault("getIncomeSource", None))
  }

  private def recoverWithEmptyDefault[U](connectorMethodName: String, emptyValue: U): PartialFunction[Throwable, Future[U]] = {
    case e =>
      logger.warn(s"Failed to call connector method $connectorMethodName", e)
      Future.successful(emptyValue)
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
      employment, companyBenefits, person.getName.getOrElse(nino.nino), incomeSource))
  }

}