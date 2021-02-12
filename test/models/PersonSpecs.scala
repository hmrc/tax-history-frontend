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

package models

import models.taxhistory.Person
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.play.test.UnitSpec

class PersonSpecs  extends UnitSpec{

  "GetName from Person Model" should {
    "unable to retrieve name when Person is not populated" in {
      val person = Person(None ,None, Some(false))
      person.getName shouldBe None
    }

    "able to retrieve name when Person is populated" in {
      val person = Person(Some("FirstName") ,Some("LastName"), Some(false))
      person.getName shouldBe Some("FirstName LastName")
    }

    "make name title case when name is populated and uppercase" in {
      val person = Person(Some("FIRSTNAME") ,Some("LASTNAME"), Some(false))
      person.getName shouldBe Some("Firstname Lastname")
    }

    "able to retrieve name when Person is populated and deceased" in {
      val person = Person(Some("FirstName") ,Some("LastName"), Some(true))
      person.getName shouldBe Some("FirstName LastName")
    }

    "not able to retrieve name when Person's lastName is not populated" in {
      val person = Person(Some("FirstName") ,None, Some(false))
      person.getName shouldBe None

    }

    "not able to retrieve name when Person's first Name is not populated" in {
      val person = Person(None ,Some("LastName"), Some(false))
      person.getName shouldBe None
    }

    "unable to retrieve name when Person values are empty strings" in {
      val person = Person(Some("") ,Some(""), Some(false))
      person.getName shouldBe Some(" ")
    }

    "parse json correctly when null values present" in {
      val person = Person(None , None, Some(false))
      val json = Json.parse(
        """{
               "firstName":null,
               "lastName":null,
               "deceased":false
        }""")
      Person.formats.reads(json) shouldBe JsSuccess(person)
    }

    "parse json correctly when no deceased field is present" in {
      val person = Person(None , None, None)
      val json = Json.parse(
        """{
               "firstName":null,
               "lastName":null
        }""")
      Person.formats.reads(json) shouldBe JsSuccess(person)
    }
  }

}
