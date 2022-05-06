/*
 * Copyright 2022 HM Revenue & Customs
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
import model.api._
import models.taxhistory.Person
import play.api.i18n.Messages
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.DateUtils

import java.time.{Instant, ZoneOffset}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmploymentSummaryController @Inject()(
   val taxHistoryConnector: TaxHistoryConnector,
   val citizenDetailsConnector: CitizenDetailsConnector,
   override val authConnector: AuthConnector,
   override val config: Configuration,
   override val env: Environment,
   val cc: MessagesControllerComponents,
   implicit val appConfig: AppConfig,
   employmentSummary: views.html.taxhistory.employment_summary,
   dateUtils: DateUtils
 )(implicit val ec: ExecutionContext) extends BaseController(cc) {

  val loginContinue: String = appConfig.loginContinue
  val serviceSignout: String = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  def signIn: Action[AnyContent] = Action { _ =>
    Redirect(appConfig.loginContinue).withNewSession
  }

  def getTaxHistory(taxYear: Int): Action[AnyContent] = Action.async {
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
          val employments: List[Employment] = getEmploymentsFromResponse(empResponse)
          val allowanceFuture = taxHistoryConnector.getAllowances(ninoField, taxYear)
          val taxAccountFuture = taxHistoryConnector.getTaxAccount(ninoField, taxYear)
          val statePensionFuture = taxHistoryConnector.getStatePension(ninoField, taxYear)
          val allPayAndTaxFuture = taxHistoryConnector.getAllPayAndTax(ninoField, taxYear)

          (for {
            allowanceResponse <- allowanceFuture
            taxAccountResponse <- taxAccountFuture
            statePensionResponse <- statePensionFuture
            allPayAndTaxResponse <- allPayAndTaxFuture
            incomeTotals <- buildIncomeTotals(employments, getAllPayAndTaxFromResponse(allPayAndTaxResponse).toList)
          } yield (allowanceResponse, taxAccountResponse, statePensionResponse, incomeTotals)).map {
            dataResponse =>
              Ok(employmentSummary(
                ninoField.nino,
                taxYear,
                employments,
                getAllowancesFromResponse(allowancesResponse = dataResponse._1),
                person,
                getTaxAccountFromResponse(taxAccountResponse = dataResponse._2),
                getStatePensionsFromResponse(statePensionResponse = dataResponse._3),
                incomeTotals = dataResponse._4,
                dateUtils.format(Instant.now().atOffset(ZoneOffset.UTC).toLocalDate)))
          }
        case status if status > OK && status < INTERNAL_SERVER_ERROR =>
          logger.warn(s"[EmploymentSummaryController][retrieveTaxHistoryData] Non 200 response calling taxHistory" +
            s" getEmploymentsAndPensions, received status $status")
          Future.successful(handleHttpFailureResponse(status, Some(taxYear)))
        case status if status >= INTERNAL_SERVER_ERROR =>
          logger.error(s"[EmploymentSummaryController][retrieveTaxHistoryData] Error calling taxHistory getEmploymentsAndPensions, received status $status")
          Future.successful(handleHttpFailureResponse(status, Some(taxYear)))
      }
    }
  }

  private def getTaxAccountFromResponse(taxAccountResponse: HttpResponse) = {
    taxAccountResponse.status match {
      case OK => taxAccountResponse.json.asOpt[TaxAccount]
      case status => logger.info(s"Tax Account Status: $status")
        None
    }
  }

  private def getAllowancesFromResponse(allowancesResponse: HttpResponse) = {
    allowancesResponse.status match {
      case OK => allowancesResponse.json.as[List[Allowance]]
      case status => logger.info(s"Allowance Status: $status")
        List.empty
    }
  }

  private def formatEmploymentDates(employment: Employment)(implicit messages: Messages): Employment = {
    val startDateFormatted = dateUtils.getStartDate(employment)
    val endDateFormatted = dateUtils.getEndDate(employment)
    employment.copy(startDateFormatted = Some(startDateFormatted), endDateFormatted = Some(endDateFormatted))
  }

  private def getEmploymentsFromResponse(empResponse: HttpResponse)(implicit messages: Messages) =
    empResponse.json.as[List[Employment]].map(formatEmploymentDates)

  private def getAllPayAndTaxFromResponse(patResponse: HttpResponse) = {
    patResponse.status match {
      case OK => patResponse.json.as[Map[String, PayAndTax]]
      case status => logger.info(s"All Pay And Tax Status: $status")
        List.empty
    }
  }

  private def formatStatePensionStartDate(statePension: StatePension)(implicit messages: Messages) = {
    statePension.startDate.fold(statePension)(date => statePension.copy(startDateFormatted = Some(dateUtils.format(date))))
  }

  private def getStatePensionsFromResponse(statePensionResponse: HttpResponse)(implicit messages: Messages) = {
    statePensionResponse.status match {
      case OK => statePensionResponse.json.asOpt[StatePension].fold(None: Option[StatePension])(statePension => Some(formatStatePensionStartDate(statePension)))
      case status => logger.info(s"State Pension Status: $status")
        None
    }
  }

  private[controllers] def buildIncomeTotals(allEmployments: List[Employment], allPayAndTax: List[(String, PayAndTax)]): Future[Option[TotalIncome]] = {
    {
      if(allPayAndTax.nonEmpty) {
        val (pensions, employments) = allPayAndTax.partition { pat =>
          val matchedRecord: Option[Employment] = allEmployments.find(_.employmentId.toString == pat._1)
          matchedRecord.exists(_.isOccupationalPension)
        }

        def pickTaxablePayTotalIncludingEYU(payAndTax: PayAndTax): BigDecimal = payAndTax.taxablePayTotalIncludingEYU.getOrElse(BigDecimal(0))
        def pickTaxTotalIncludingEYU(payAndTax: PayAndTax): BigDecimal = payAndTax.taxTotalIncludingEYU.getOrElse(BigDecimal(0))

        val employmentsPayAndTax: Seq[PayAndTax] = employments.map(_._2)
        val pensionsPayAndTax: Seq[PayAndTax] = pensions.map(_._2)

        val employmentIncomeAndTax = allPayAndTax.map(tupleEmploymentIdAndPayTax =>
          EmploymentIncomeAndTax(tupleEmploymentIdAndPayTax._1,
            pickTaxablePayTotalIncludingEYU(tupleEmploymentIdAndPayTax._2),
            pickTaxTotalIncludingEYU(tupleEmploymentIdAndPayTax._2)))

        Future successful Some(TotalIncome(
          employmentIncomeAndTax,
          employmentTaxablePayTotalIncludingEYU = employmentsPayAndTax.map(pickTaxablePayTotalIncludingEYU).sum,
          pensionTaxablePayTotalIncludingEYU = pensionsPayAndTax.map(pickTaxablePayTotalIncludingEYU).sum,
          employmentTaxTotalIncludingEYU = employmentsPayAndTax.map(pickTaxTotalIncludingEYU).sum,
          pensionTaxTotalIncludingEYU = pensionsPayAndTax.map(pickTaxTotalIncludingEYU).sum
        ))
      } else {
        Future successful None
      }
    } recover {
      case e =>
        logger.error(s"[EmploymentSummaryController][buildIncomeTotals] buildIncomeTotals failed with ${e.getMessage}")
        None
    }
  }
}
