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
import model.api._
import models.taxhistory.Person
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class EmploymentSummaryController @Inject()(
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
              Ok(views.html.taxhistory.employment_summary(
                ninoField.nino,
                taxYear,
                employments,
                getAllowancesFromResponse(allowancesResponse = dataResponse._1),
                person,
                getTaxAccountFromResponse(taxAccountResponse = dataResponse._2),
                getStatePensionsFromResponse(statePensionResponse = dataResponse._3),
                incomeTotals = dataResponse._4))
          }
        case status => Future.successful(handleHttpFailureResponse(status, ninoField, Some(taxYear)))
      }
    }
  }

  private def getTaxAccountFromResponse(taxAccountResponse: HttpResponse) = {
    taxAccountResponse.status match {
      case OK => taxAccountResponse.json.asOpt[TaxAccount]
      case status =>
        Logger.info(s"Tax Account Status: $status")
        None
    }
  }

  private def getAllowancesFromResponse(allowancesResponse: HttpResponse) = {
    allowancesResponse.status match {
      case OK => allowancesResponse.json.as[List[Allowance]]
      case status =>
        Logger.info(s"Allowance Status: $status")
        List.empty
    }
  }

  private def getEmploymentsFromResponse(empResponse: HttpResponse) =
    empResponse.json.as[List[Employment]]

  private def getAllPayAndTaxFromResponse(patResponse: HttpResponse) = {
    patResponse.status match {
      case OK => patResponse.json.as[Map[String, PayAndTax]]
      case status =>
        Logger.info(s"All Pay And Tax Status: $status")
        List.empty
    }
  }


  private def getStatePensionsFromResponse(statePensionResponse: HttpResponse) = {
    statePensionResponse.status match {
      case OK => statePensionResponse.json.asOpt[StatePension]
      case status =>
        Logger.info(s"State Pension Status: $status")
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

        Future successful Some(TotalIncome(
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
        logger.warn(s"buildIncomeTotals failed with ${e.getMessage}")
        None
    }
  }
}