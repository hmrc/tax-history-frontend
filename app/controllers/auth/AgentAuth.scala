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

package controllers.auth

import controllers.routes
import form.SelectClientForm.selectClientForm
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals.{affinityGroup, allEnrolments}
import uk.gov.hmrc.auth.core._
import config.FrontendAppConfig.{AfiErrorPage, AfiHomePage, AfiNoAgentServicesAccountPage}
import views.html.select_client

import scala.concurrent.Future
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.frontend.Redirects
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.api.{Application, Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.play.frontend.controller.FrontendController
import javax.inject.{Inject, Singleton}
import config.FrontendAuthConnector

@Singleton
class AgentAuth @Inject()(app: Application, frontendAuthConnector: FrontendAuthConnector) extends FrontendController with AuthorisedFunctions with Redirects {
  //todo redirect to our pages
  override def authConnector: AuthConnector = frontendAuthConnector

  override def config: Configuration = app.configuration

  override def env: Environment = Environment(app.path, app.classloader, app.mode)

  private def redirectToSubPage: Future[Result] = Future successful Redirect(AfiNoAgentServicesAccountPage)

  private def redirectToExitPage: Future[Result] = Future successful Redirect(AfiNoAgentServicesAccountPage)

  private def isAgent(group: AffinityGroup): Boolean = group.toString.contains("Agent")

  private def extractArn(enrolls: Set[Enrolment]): Option[String] =
    enrolls.find(_.key equals "HMRC-AS-AGENT").flatMap(_.identifiers.find(_.key equals "AgentReferenceNumber").map(_.value))

  private type AfiActionWithArn = Request[AnyContent] => String => Future[Result]
  private type AfiActionForPAYE = Boolean => Future[Result]
  lazy val affinityGroupAllEnrolls = affinityGroup and allEnrolments
  lazy val AgentEnrolmentForPAYE: Enrolment = Enrolment("HMRC-AS-AGENT")
  lazy val AuthProviderAgents: AuthProviders = AuthProviders(GovernmentGateway)
  private val isAnAgent = true
  private val isAuthorisedForPAYE = true
  private val isNotAuthorisedForPAYE = false
  def authorisedForPAYE(clientId: Option[String], action: AfiActionForPAYE)(implicit request: Request[AnyContent]): Future[Result] = {
    if (clientId.isDefined) {
      authorised(AgentEnrolmentForPAYE.withIdentifier("MTDITID", clientId.get) and AuthProviderAgents) {
        action(isAuthorisedForPAYE)
      } recoverWith {
        case _ => action(isNotAuthorisedForPAYE)
      }
    } else Future successful Redirect(AfiHomePage)
  }


  def authorisedForAfi(action: AfiActionWithArn): Action[AnyContent] = {
    Action.async { implicit request ⇒
      authorised(AuthProviderAgents).retrieve(affinityGroupAllEnrolls) {
        case Some(affinityG) ~ allEnrols ⇒
          (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
            case (`isAnAgent`, Some(arn)) => action(request)(arn)
            case (`isAnAgent`, None) => redirectToSubPage
            case _ => redirectToExitPage
          }
        case _ => redirectToExitPage
      } recover {
        case e ⇒ handleFailure(e)
      }
    }
  }

  def handleFailure(e: Throwable): Result =
    e match {
      case x: NoActiveSession ⇒
        Logger.warn(s"could not authenticate user due to: No Active Session " + x)
        toGGLogin(AfiHomePage)
      case ex ⇒
        Logger.warn(s"could not authenticate user due to: $ex")
        Redirect(AfiErrorPage)
    }
}