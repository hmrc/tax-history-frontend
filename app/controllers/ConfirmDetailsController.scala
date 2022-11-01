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
import connectors.CitizenDetailsConnector
import javax.inject.Inject
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import views.html.taxhistory.confirm_details

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDetailsController @Inject() (
  override val authConnector: AuthConnector,
  val citizenDetailsConnector: CitizenDetailsConnector,
  override val config: Configuration,
  override val env: Environment,
  val cc: MessagesControllerComponents,
  implicit val appConfig: AppConfig,
  confirmDetails: confirm_details
)(implicit val ec: ExecutionContext)
    extends BaseController(cc) {

  val loginContinue: String          = appConfig.loginContinue
  val serviceSignout: String         = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  private def renderConfirmDetailsPage(nino: Nino)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] =
    retrieveCitizenDetails(nino, citizenDetailsConnector.getPersonDetails(nino)).flatMap {
      case Left(citizenStatus) =>
        redirectToClientErrorPage(citizenStatus)
      case Right(person)       =>
        Future.successful(Ok(confirmDetails(person.getName.getOrElse(nino.nino), nino.nino)))
    }

  def getConfirmDetailsPage: Action[AnyContent] = Action.async { implicit request =>
    authorisedForAgent { nino =>
      renderConfirmDetailsPage(nino)
    }
  }

  def submitConfirmDetailsPage: Action[AnyContent] = Action.async { implicit request =>
    authorisedForAgent { _ =>
      Future.successful(Redirect(routes.SelectTaxYearController.getSelectTaxYearPage()))
    }
  }
}
