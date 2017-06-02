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

import java.security.cert.X509Certificate

import play.api.mvc.{AnyContent, Headers, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext



case class LocalContext(request: Request[AnyContent], authContext: AuthContext) extends Request[AnyContent] {

  override def body: AnyContent = request.body
  override def secure: Boolean = request.secure
  override def uri: String = request.uri
  override def queryString: Map[String, Seq[String]] = request.queryString
  override def remoteAddress: String = request.remoteAddress
  override def method: String = request.method
  override def headers: Headers = request.headers
  override def path: String = request.path
  override def version: String = request.version
  override def tags: Map[String, String] = request.tags
  override def id: Long = request.id
  override def clientCertificateChain: Option[Seq[X509Certificate]] = request.clientCertificateChain

  def nino = authContext.principal.accounts.paye.map(_.nino)
}