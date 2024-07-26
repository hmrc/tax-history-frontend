/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import com.codahale.metrics.Timer
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.OK
import support.BaseSpec
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.concurrent.Future

trait BaseConnectorSpec extends BaseSpec with BeforeAndAfterEach with GuiceOneAppPerSuite with ScalaFutures {

  val system: ActorSystem = ActorSystem("test")

  val mockHttpClient: HttpClientV2       = mock(classOf[HttpClientV2])
  val mockRequestBuilder: RequestBuilder = mock(classOf[RequestBuilder])
  val mockTimerContext: Timer.Context    = mock(classOf[Timer.Context])

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockHttpClient)
    reset(mockTimerContext)
    reset(mockRequestBuilder)

    when(mockHttpClient.get(any())(any())).thenReturn(mockRequestBuilder)
    when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
  }

  def buildHttpResponse(body: String, status: Int = OK): HttpResponse =
    HttpResponse(status, body = body, Map.empty[String, Seq[String]])

  def buildHttpResponse(status: Int): HttpResponse =
    HttpResponse(status, body = "", Map.empty[String, Seq[String]])

  def mockExecuteMethod(body: String, status: Int): Unit =
    when(mockRequestBuilder.execute(any[HttpReads[HttpResponse]], any()))
      .thenReturn(Future.successful(buildHttpResponse(body, status)))

  def mockExecuteMethod(status: Int): Unit =
    when(mockRequestBuilder.execute(any[HttpReads[HttpResponse]], any()))
      .thenReturn(Future.successful(buildHttpResponse("", status)))

}
