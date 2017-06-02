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

import javax.inject._

import config.ConfigDecorator
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent._

@Singleton
class LocalPageVisibilityPredicateFactory @Inject() (
  configDecorator: ConfigDecorator
) {

  def build = {

    val strongCredentialPredicate = new LocalStrongCredentialPredicate {

      override lazy val successUrl  = Some(configDecorator.taxHistoryFrontendHost + "/tax-history-frontend")
      override lazy val registerUrl = configDecorator.companyAuthHost + "/coafe/two-step-verification/register"
      override lazy val failureUrl  = configDecorator.pertaxFrontendHost + "/personal-account/identity-check-failed"
      override lazy val continueUrl = configDecorator.taxHistoryFrontendHost + "/tax-history-frontend"
    }

    val confidenceLevelPredicate = new LocalConfidenceLevelPredicate {

      override lazy val upliftUrl     = configDecorator.identityVerificationUpliftUrl
      override lazy val origin        = "TAXHIST"
      override lazy val failureUrl    = configDecorator.pertaxFrontendHost + "/personal-account/identity-check-failed"
      override lazy val completionUrl = configDecorator.taxHistoryFrontendHost + "/tax-history-frontend"
    }

    new CompositePageVisibilityPredicate {
      override def children: Seq[PageVisibilityPredicate] = Seq(strongCredentialPredicate, confidenceLevelPredicate)
    }
  }


}

trait LocalStrongCredentialPredicate extends PageVisibilityPredicate with CredentialStrengthChecker {

  def successUrl: Option[String]

  def registerUrl: String
  def failureUrl: String
  def continueUrl: String

  def apply(authContext: AuthContext, request: Request[AnyContent]) = {

    implicit val req = request

    if ( userHasStrongCredentials(authContext) ) Future.successful(PageIsVisible)
    else Future.successful(new PageBlocked(Future.successful(
      Redirect(
        registerUrl,
        Map("continue" -> Seq(continueUrl), "failure" -> Seq(failureUrl))
      )
    )))
  }


}

trait LocalConfidenceLevelPredicate extends PageVisibilityPredicate with ConfidenceLevelChecker {

  def upliftUrl: String
  def origin: String
  def failureUrl: String
  def completionUrl: String

  def apply(authContext: AuthContext, request: Request[AnyContent]): Future[PageVisibilityResult] = {
    implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
    if ( userHasHighConfidenceLevel(authContext) ) {
      Future.successful(PageIsVisible)
    } else {
      Future.successful(new PageBlocked(Future.successful(buildIVUpliftUrl(ConfidenceLevel.L200))))
    }
  }

  private def buildIVUpliftUrl(confidenceLevel: ConfidenceLevel) =
    Redirect(
      upliftUrl,
      Map("origin" -> Seq(origin),
        "confidenceLevel" -> Seq(confidenceLevel.level.toString),
        "completionURL" -> Seq(completionUrl),
        "failureURL" -> Seq(failureUrl)
      )
    )

}
