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

import com.google.inject.name.Named
import config.AppConfig
import play.api.http.Status._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Request, RequestHeader}
import play.api.{Configuration, Environment}
import play.twirl.api.Html
import uk.gov.hmrc.http.{JsValidationException, Upstream4xxResponse}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.{AuthRedirects, HttpAuditEvent}
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ErrorHandler @Inject()(
   val env: Environment,
   val auditConnector: AuditConnector,
   val messagesApi: MessagesApi,
   errorTemplate: views.html.error_template,
   @Named("appName") val appName: String
)(implicit val config: Configuration, val appConfig: AppConfig)
  extends FrontendErrorHandler with I18nSupport with AuthRedirects with ErrorAuditing {

  lazy val authenticationRedirect: String = config
    .getOptional[String]("authentication.login-callback.url")
    .getOrElse(
      throw new IllegalStateException(s"No value found for configuration property: authentication.login-callback.url"))

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html =
  errorTemplate(pageTitle, heading, message)
}

object EventTypes {

  val RequestReceived: String = "RequestReceived"
  val TransactionFailureReason: String = "transactionFailureReason"
  val ServerInternalError: String = "ServerInternalError"
  val ResourceNotFound: String = "ResourceNotFound"
  val ServerValidationError: String = "ServerValidationError"
}

trait ErrorAuditing extends HttpAuditEvent {

  import EventTypes._

  def auditConnector: AuditConnector

  private val unexpectedError = "Unexpected error"
  private val notFoundError = "Resource Endpoint Not Found"
  private val badRequestError = "Request bad format exception"

  def auditServerError(request: RequestHeader, ex: Throwable)(implicit ec: ExecutionContext): Unit = {
    val eventType = ex match {
      case Upstream4xxResponse(_, NOT_FOUND, _, _)   => ResourceNotFound
      case _: JsValidationException => ServerValidationError
      case _                        => ServerInternalError
    }
    val transactionName = ex match {
      case Upstream4xxResponse(_, NOT_FOUND, _, _) => notFoundError
      case _                    => unexpectedError
    }
    auditConnector.sendEvent(
      dataEvent(eventType, transactionName, request, Map(TransactionFailureReason -> ex.getMessage))(
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)))
  }

  def auditClientError(request: RequestHeader, statusCode: Int, message: String)(
    implicit ec: ExecutionContext): Unit = {
    import play.api.http.Status._
    statusCode match {
      case NOT_FOUND =>
        auditConnector.sendEvent(
          dataEvent(ResourceNotFound, notFoundError, request, Map(TransactionFailureReason -> message))(
            HeaderCarrierConverter.fromRequestAndSession(request, request.session)))
      case BAD_REQUEST =>
        auditConnector.sendEvent(
          dataEvent(ServerValidationError, badRequestError, request, Map(TransactionFailureReason -> message))(
            HeaderCarrierConverter.fromRequestAndSession(request, request.session)))
      case _ =>
    }
  }
}
