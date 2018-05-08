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
import play.api.{Configuration, Environment}
import views.html.errors._
import form.FastTrackForm.fastTrackForm

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

  def getNotAuthorised: Action[AnyContent] = Action.async {
    implicit request =>
      getNinoFromSession(request).fold(redirectToSelectClientPage){
        _ => Future successful Ok(not_authorised(getNinoFromSession(request), fastTrackForm))
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
