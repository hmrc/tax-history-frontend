/*
 * Copyright 2025 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import views.html.errors._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClientErrorController @Inject() (
  val citizenDetailsConnector: CitizenDetailsConnector,
  val cc: MessagesControllerComponents,
  override val authConnector: AuthConnector,
  override val config: Configuration,
  override val env: Environment,
  implicit val appConfig: AppConfig,
  notAuthorised: not_authorised,
  mciRestricted: mci_restricted,
  deceased: deceased,
  noData: no_data,
  technicalError: technical_error,
  noAgentServicesAccount: no_agent_services_account
)(implicit ec: ExecutionContext)
    extends BaseController(cc) {

  lazy val loginContinue: String          = appConfig.loginContinue
  lazy val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  def getNotAuthorised: Action[AnyContent] = Action.async { implicit request =>
    getNinoFromSession(request).fold(redirectToSelectClientPage) { _ =>
      request.getQueryString("issue") match {
        case Some(issue) =>
          logger.error(s"[ClientErrorController][getNotAuthorised] Form Error: $issue")
          throw new Exception(s"Invalid Form Data was submitted to Fast-Track Authorisations")
        case None        =>
          Future successful Ok(notAuthorised(getNinoFromSession(request)))
      }
    }
  }

  def getMciRestricted: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(mciRestricted()))
  }

  def getDeceased: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(deceased()))
  }

  def getNoData(taxYear: Int): Action[AnyContent] = Action.async { implicit request =>
    getNinoFromSession(request).fold(redirectToSelectClientPage) { nino =>
      retrieveCitizenDetails(citizenDetailsConnector.getPersonDetails(nino)).flatMap {
        case Left(status)  => redirectToClientErrorPage(status)
        case Right(person) => Future.successful(Ok(noData(person, nino.toString, taxYear)))
      }
    }
  }

  def getTechnicalError: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(technicalError()))
  }

  def getNoAgentServicesAccountPage: Action[AnyContent] = Action.async { implicit request =>
    Future successful Ok(noAgentServicesAccount())
  }

}
