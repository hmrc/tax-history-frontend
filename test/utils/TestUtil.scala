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

package utils

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.{Generator, Nino}

import scala.io.Source
import scala.util.Random

object TestUtil extends TestUtil

trait TestUtil {

  def loadFile(path:String): JsValue = {
    val jsonString = Source.fromURL(getClass.getResource(path)).mkString
    Json.parse(jsonString)
  }

  def randomNino = Nino(new Generator(new Random()).nextNino.value.replaceFirst("MA", "AA"))
}
