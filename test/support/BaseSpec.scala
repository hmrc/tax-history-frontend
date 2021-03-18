/*
 * Copyright 2021 HM Revenue & Customs
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

package support

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import config.AppConfig
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Environment
import play.api.mvc.MessagesControllerComponents
import play.api.test.Injecting
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

trait BaseSpec extends WordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite with MockitoSugar with PatienceConfiguration with BeforeAndAfterEach with Injecting { this: Suite =>

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def injected[T](c: Class[T]): T = app.injector.instanceOf(c)
  def injected[T](implicit evidence: ClassTag[T]): T = app.injector.instanceOf[T]

  lazy val messagesControllerComponents: MessagesControllerComponents = injected[MessagesControllerComponents]
  lazy val environment: Environment = injected[Environment]

  implicit val appConfig: AppConfig = injected[AppConfig]
  implicit val ec: ExecutionContext = injected[ExecutionContext]

  implicit val actorSystem: ActorSystem = ActorSystem("test")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

}