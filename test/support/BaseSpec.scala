/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.reflect.ClassTag

trait BaseSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar with PatienceConfiguration with BeforeAndAfterEach { this: Suite =>

  implicit val hc = HeaderCarrier()

  def injected[T](c: Class[T]): T = app.injector.instanceOf(c)
  def injected[T](implicit evidence: ClassTag[T]) = app.injector.instanceOf[T]


}