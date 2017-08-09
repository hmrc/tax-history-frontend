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

import config.FrontendAppConfig.{AfiErrorPage, AfiHomePage, AfiNoAgentServicesAccountPage}
import play.api.Logger
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.Retrievals.{affinityGroup, allEnrolments}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait AgentAuth extends FrontendController with AuthorisedFunctions with Redirects {

  def redirectToSubPage: Future[Result] = Future successful Redirect(AfiNoAgentServicesAccountPage)

  def redirectToExitPage: Future[Result] = Future successful Redirect(AfiNoAgentServicesAccountPage)

  def isAgent(group: AffinityGroup): Boolean = group.toString.contains("Agent")

  def extractArn(enrolls: Set[Enrolment]): Option[String] =
    enrolls.find(_.key equals "HMRC-AS-AGENT").flatMap(_.identifiers.find(_.key equals "AgentReferenceNumber").map(_.value))

  type AfiActionWithArn = Request[AnyContent] => Future[Result]
  lazy val affinityGroupAllEnrolls = affinityGroup and allEnrolments
  lazy val AgentEnrolmentForPAYE: Enrolment = Enrolment("HMRC-AS-AGENT")
  lazy val AuthProviderAgents: AuthProviders = AuthProviders(GovernmentGateway)
  val isAnAgent:Boolean  = true
  val isAuthorisedForPAYE = true
  val isNotAuthorisedForPAYE = false

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
