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
import connectors.CitizenDetailsConnector
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment, Logger, Mode}
import views.html.errors._

import scala.concurrent.Future

class ClientErrorController @Inject()(val citizenDetailsConnector: CitizenDetailsConnector,
                                      override val authConnector: FrontendAuthConnector,
                                      override val config: Configuration,
                                      override val env: Environment,
                                      implicit val messagesApi: MessagesApi,
                                      implicit val appConfig: AppConfig) extends BaseController {

  val loginContinue: String = appConfig.loginContinue
  val serviceSignout: String = appConfig.serviceSignOut
  val agentSubscriptionStart: String = appConfig.agentSubscriptionStart

  val isDevEnv = if (env.mode.equals(Mode.Test)) false else config.getString("run.mode").forall(Mode.Dev.toString.equals)

  def getNotAuthorised: Action[AnyContent] = Action.async {
    implicit request =>
      getNinoFromSession(request).fold(redirectToSelectClientPage){
        _ => request.getQueryString("issue") match {
          case Some(issue) =>
            Logger(getClass).error(s"Form Error: ${issue}")
            throw new Exception(s"Invalid Form Data was submitted to Fast-Track Authorisations")
        case None =>
          Future successful Ok(not_authorised(getNinoFromSession(request), isDevEnv))
        }
      }
  }

  def getMciRestricted: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(mci_restricted()))
  }

  def getDeceased: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(deceased()))
  }

  def getNoData(taxYear:Int): Action[AnyContent] = Action.async {
    implicit request => {
      getNinoFromSession(request).fold(redirectToSelectClientPage) {
        nino => retrieveCitizenDetails(nino, citizenDetailsConnector.getPersonDetails(nino)).flatMap{
            case Left(status) => redirectToClientErrorPage(status)
            case Right(person) => Future.successful(Ok(no_data(person,nino.toString,taxYear)))
        }
      }
    }
  }

  def getTechnicalError: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(views.html.errors.technical_error()))
  }

  def getNoAgentServicesAccountPage: Action[AnyContent] = Action.async {
    implicit request =>
      Future successful Ok(no_agent_services_account())
  }

}
