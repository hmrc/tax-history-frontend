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

import config.{ConfigDecorator, FrontendAuthConnector}
import connectors.TaxHistoryConnector
import models.taxhistory.Employment
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.{Configuration, Environment, Logger}
import play.api.libs.json.Json
import play.api.mvc.{Action, Result}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{Enrolment, _}
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.{Actions, DelegationAwareActions}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.BadGatewayException
import uk.gov.hmrc.time.TaxYearResolver
import play.api.i18n.Messages.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait BaseController extends FrontendController with Actions with Redirects with I18nSupport

class MainController @Inject()(
                                val configDecorator: ConfigDecorator,
                                val taxHistoryConnector: TaxHistoryConnector,
                                override val authConnector: FrontendAuthConnector,
                                override val config: Configuration,
                                override val env: Environment,
                                implicit val messagesApi: MessagesApi
                              ) extends BaseController with AuthorisedFunctions {

  lazy val ggSignInRedirect: Result = toGGLogin(s"${configDecorator.loginContinue}")

  def get() = Action.async {

    implicit request => {
      val nino = request.session.get("USER_NINO").map(Nino(_))
      authorised(Enrolment("HMRC-AS-AGENT") and AuthProviders(GovernmentGateway)) {

        val cy1 = TaxYearResolver.currentTaxYear - 1
        nino match {
          case Some(nino) =>
            taxHistoryConnector.getTaxHistory(nino, cy1) map {
              historyResponse => historyResponse.status match {
                case OK => {
                  val taxHistory = historyResponse.json.as[Seq[Employment]]
                  Ok(views.html.taxhistory.employments_main(nino.nino, cy1, taxHistory)).removingFromSession("USER_NINO")
                }
                case NOT_FOUND => {
                  Logger.warn(messagesApi("employmenthistory.notfound.message"))
                  Ok(views.html.error_template(messagesApi("employmenthistory.notfound.title"),messagesApi("employmenthistory.notfound.title"),messagesApi("employmenthistory.notfound.message"))).removingFromSession("USER_NINO")
                }
                case UNAUTHORIZED => {
                  Logger.warn(messagesApi("employmenthistory.unauthorised.message"))
                  Ok(views.html.error_template(messagesApi("employmenthistory.unauthorised.title"),messagesApi("employmenthistory.unauthorised.title"),messagesApi("employmenthistory.unauthorised.message"))).removingFromSession("USER_NINO")
                }
                case _ => {
                  Logger.warn(messagesApi("employmenthistory.technicalerror.message"))
                  Ok(views.html.error_template(messagesApi("employmenthistory.technicalerror.title"),messagesApi("employmenthistory.technicalerror.title"),messagesApi("employmenthistory.technicalerror.message"))).removingFromSession("USER_NINO")
                }
              }
            }
          case _ =>
            Logger.warn(messagesApi("employmenthistory.nonino.message"))
            Future.successful(Ok(views.html.error_template(messagesApi("employmenthistory.nonino.title"),messagesApi("employmenthistory.nonino.title"),messagesApi("employmenthistory.nonino.message"))))
        }
      }.recoverWith {
        case _: BadGatewayException => {
          Logger.warn(messagesApi("employmenthistory.technicalerror.message")+" : Due to BadGatewayException")
          Future.successful(Ok(views.html.error_template(messagesApi("employmenthistory.technicalerror.title"),messagesApi("employmenthistory.technicalerror.title"),messagesApi("employmenthistory.technicalerror.message"))).removingFromSession("USER_NINO"))
        }
        case _: MissingBearerToken => {
          Logger.warn(messagesApi("employmenthistory.technicalerror.message")+" : Due to MissingBearerToken")
          Future.successful(ggSignInRedirect)
        }
        case e =>
          Logger.warn("Exception thrown :"+e.getMessage)
          Future.successful(ggSignInRedirect)
      }
    }
  }
}
