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

package models

import models.taxhistory.Person
import uk.gov.hmrc.play.test.UnitSpec

class PersonSpecs  extends UnitSpec{

  "GetName from Person Model" should {
    "unable to retrieve name when Person is not populated" in {
      val person = Person(None ,None)
      person.getName shouldBe None

    }

    "able to retrieve name when Person is populated" in {
      val person = Person(Some("FirstName") ,Some("LastName"))
      person.getName shouldBe Some("FirstName LastName")

    }

    "not able to retrieve name when Person's lastName is not populated" in {
      val person = Person(Some("FirstName") ,None)
      person.getName shouldBe None

    }
    "not able to retrieve name when Person's first Name is not populated" in {
      val person = Person(None ,Some("LastName"))
      person.getName shouldBe None

    }
  }

}