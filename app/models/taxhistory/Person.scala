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

package models.taxhistory

import play.api.libs.json.{Json, OFormat}


case class Person(firstName:Option[String], lastName:Option[String], deceased:Option[Boolean]){

  def getName: Option[String] = {
    for {
      f <- this.firstName
      l <- this.lastName
    }yield Person.uppercaseToTitleCase(f) + " " + Person.uppercaseToTitleCase(l)
  }

}

object Person {
  implicit val formats: OFormat[Person] = Json.format[Person]

  def uppercaseToTitleCase(s: String): String = {
    if (!s.exists(_.isLower)) s.toLowerCase.capitalize else s
  }
}