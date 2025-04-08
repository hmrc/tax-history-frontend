/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.BaseController
import model.api.IncomeSource._
import model.api._
import models.taxhistory.Person
import play.api.i18n.Messages
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.DateUtils
import views.models.EmploymentViewDetail

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmploymentDetailController @Inject() (
  val taxHistoryConnector: TaxHistoryConnector,
  val citizenDetailsConnector: CitizenDetailsConnector,
  override val authConnector: AuthConnector,
  override val config: Configuration,
  override val env: Environment,
  val cc: MessagesControllerComponents,
  employmentDetail: views.html.taxhistory.employment_detail,
  dateUtils: DateUtils
)(implicit val ec: ExecutionContext, val appConfig: AppConfig)
    extends BaseController(cc) {

  val loginContinue: String          = appConfig.loginContinue
  val serviceSignout: String         = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  private def renderEmploymentDetailsPage(nino: Nino, taxYear: Int, employmentId: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): Future[Result] =
    retrieveCitizenDetails(citizenDetailsConnector.getPersonDetails(nino)) flatMap {
      case Left(citizenStatus) => redirectToClientErrorPage(citizenStatus)
      case Right(person)       =>
        taxHistoryConnector.getEmployment(nino, taxYear, employmentId) flatMap { empDetailsResponse =>
          (empDetailsResponse.status: @unchecked) match {
            case OK                                                      =>
              loadEmploymentDetailsPage(empDetailsResponse, nino, taxYear, employmentId, person)
            case NOT_FOUND                                               =>
              Future.successful(Redirect(routes.EmploymentSummaryController.getTaxHistory(taxYear)))
            case status if status > OK && status < INTERNAL_SERVER_ERROR =>
              logger.warn(
                s"[EmploymentDetailController][renderEmploymentDetailsPage] Non 200 response calling " +
                  s"taxHistory getEmployment, received status $status"
              )
              Future.successful(handleHttpFailureResponse(status))
            case status if status >= INTERNAL_SERVER_ERROR               =>
              logger.error(
                s"[EmploymentDetailController][renderEmploymentDetailsPage] Error calling taxHistory getEmployment, received status $status"
              )
              Future.successful(handleHttpFailureResponse(status))
          }
        }
    }

  def getEmploymentDetails(employmentId: String, taxYear: Int): Action[AnyContent] = Action.async { implicit request =>
    authorisedForAgent { nino =>
      renderEmploymentDetailsPage(nino, taxYear, employmentId)
    }
  }

  private[controllers] def getPayAndTax(
    nino: Nino,
    taxYear: Int,
    employmentId: String
  )(implicit
    hc: HeaderCarrier,
    messages: Messages
  ): Future[Option[PayAndTax]] =
    taxHistoryConnector
      .getPayAndTaxDetails(nino, taxYear, employmentId)
      .map { payAndTaxResponse =>
        def result = dateUtils.formatEarlierYearUpdateReceivedDate(payAndTaxResponse.json.as[PayAndTax])

        payAndTaxResponse.status match {
          case NOT_FOUND => None
          case _         =>
            payAndTaxResponse.json.validate[PayAndTax] match {
              case JsSuccess(_, _) =>
                logger.info(s"[EmploymentDetailController][getPayAndTax] Successful parse to json")
                Some(result)
              case JsError(errors) =>
                logger.error(
                  s"[EmploymentDetailController][getPayAndTax] Invalid json returned in ${payAndTaxResponse.status}. $errors"
                )
                throw new RuntimeException("Invalid json returned")
            }
        }
      }
      .recoverWith(recoverWithEmptyDefault("getPayAndTaxDetails", None))

  private def getCompanyBenefits(nino: Nino, taxYear: Int, employmentId: String)(implicit
    hc: HeaderCarrier
  ): Future[List[CompanyBenefit]] =
    taxHistoryConnector
      .getCompanyBenefits(nino, taxYear, employmentId)
      .map { cbResponse =>
        cbResponse.status match {
          case NOT_FOUND => List.empty
          case _         =>
            cbResponse.json.validate[List[CompanyBenefit]] match {
              case JsSuccess(result, _) =>
                logger.info(s"[EmploymentDetailController][getCompanyBenefits] Successful parse to json")
                result
              case JsError(errors)      =>
                logger.error(
                  s"[EmploymentDetailController][getCompanyBenefits] Invalid json returned in ${cbResponse.status}. $errors"
                )
                throw new RuntimeException("Invalid json returned")
            }
        }
      }
      .recoverWith(recoverWithEmptyDefault("getCompanyBenefits", List.empty))

  private[controllers] def getIncomeSource(nino: Nino, taxYear: Int, employmentId: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[IncomeSource]] =
    taxHistoryConnector
      .getIncomeSource(nino, taxYear, employmentId)
      .map { isResponse =>
        isResponse.status match {
          case NOT_FOUND => None
          case _         =>
            isResponse.json.validate[IncomeSource] match {
              case JsSuccess(result, _) =>
                logger.info(s"[EmploymentDetailController][getIncomeSource] Successful parse to json")
                Some(result)
              case JsError(errors)      =>
                logger.error(
                  s"[EmploymentDetailController][getIncomeSource] Invalid json returned in ${isResponse.status}. $errors"
                )
                throw new RuntimeException("Invalid json returned")
            }
        }
      }
      .recoverWith(recoverWithEmptyDefault("getIncomeSource", None))

  private[controllers] def recoverWithEmptyDefault[U](
    connectorMethodName: String,
    emptyValue: U
  ): PartialFunction[Throwable, Future[U]] = { case e =>
    logger.error(
      s"[EmploymentDetailController][recoverWithEmptyDefault] Failed to call connector method $connectorMethodName",
      e
    )
    Future.successful(emptyValue)
  }

  private def loadEmploymentDetailsPage(
    empResponse: HttpResponse,
    nino: Nino,
    taxYear: Int,
    employmentId: String,
    person: Person
  )(implicit hc: HeaderCarrier, request: Request[_]) = {
    val employment = dateUtils.formatEmploymentDates(empResponse.json.as[Employment])
    for {
      payAndTax: Option[PayAndTax]          <- getPayAndTax(nino, taxYear, employmentId)
      companyBenefits: List[CompanyBenefit] <- getCompanyBenefits(nino, taxYear, employmentId)
      incomeSource: Option[IncomeSource]    <- getIncomeSource(nino, taxYear, employmentId)
    } yield {
      val employmentViewDetail = EmploymentViewDetail(
        employment.isJobseekersAllowance,
        employment.isOccupationalPension,
        employment.employerName
      )
      Ok(
        employmentDetail(
          taxYear,
          payAndTax,
          employment,
          companyBenefits,
          person.getName.getOrElse(nino.nino),
          incomeSource,
          employmentViewDetail
        )
      )
    }
  }
}
