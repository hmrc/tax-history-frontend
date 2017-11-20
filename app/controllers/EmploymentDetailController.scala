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
import model.api.{CompanyBenefit, Employment, PayAndTax}
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.urls.Link

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmploymentDetailController @Inject()(
                                            val taxHistoryConnector: TaxHistoryConnector,
                                            val citizenDetailsConnector: CitizenDetailsConnector,
                                            override val authConnector: FrontendAuthConnector,
                                            override val config: Configuration,
                                            override val env: Environment,
                                            implicit val messagesApi: MessagesApi
                                          ) extends BaseController {


  def getEmploymentDetails(employmentId: String, taxYear: Int) = Action.async {
    implicit request =>
      authorisedForAgent { nino =>
        taxHistoryConnector.getEmployment(nino, taxYear, employmentId) flatMap { empDetailsResponse =>
          empDetailsResponse.status match {
            case OK =>
              loadEmploymentDetailsPage(empDetailsResponse, nino, taxYear, employmentId)
            case NOT_FOUND => Future.successful(Redirect(routes.EmploymentSummaryController.getTaxHistory(taxYear)))
            case status => Future.successful(handleHttpFailureResponse(status, nino))
          }
        }
      }
  }

  private def getPayAndTax(nino: Nino, taxYear: Int, employmentId: String)
                          (implicit hc: HeaderCarrier, request: Request[_]): Future[Option[PayAndTax]] = {
    {
      taxHistoryConnector.getPayAndTaxDetails(nino, taxYear, employmentId) map { payAndTaxResponse =>
        Some(payAndTaxResponse.json.as[PayAndTax])
      }
    }.recoverWith {
      case _ => Future.successful(None)
    }
  }

  private def getCompanyBenefits(nino: Nino, taxYear: Int, employmentId: String)
                                (implicit hc: HeaderCarrier, request: Request[_]): Future[List[CompanyBenefit]] = {
    {
      taxHistoryConnector.getCompanyBenefits(nino, taxYear, employmentId) map { cbResponse =>
        cbResponse.json.as[List[CompanyBenefit]]
      }
    }.recoverWith {
      case _ => Future.successful(List.empty)
    }
  }

  private def loadEmploymentDetailsPage(empResponse: HttpResponse, nino: Nino, taxYear: Int, employmentId: String)
                                       (implicit hc: HeaderCarrier, request: Request[_]) = {
    val employment = empResponse.json.as[Employment]
    val sidebarLink = Link.toInternalPage(
        url = controllers.routes.EmploymentSummaryController.getTaxHistory(taxYear).url,
        value = Some(messagesApi("employmenthistory.payerecord.linktext")),
        id = Some("back-link")
    ).toHtml
   for {
     payAndTax <- getPayAndTax(nino, taxYear, employmentId)
     companyBenefits <- getCompanyBenefits(nino, taxYear, employmentId)
   } yield Ok(views.html.taxhistory.employment_detail(taxYear, payAndTax, employment, companyBenefits, Some(sidebarLink)))

  }
}