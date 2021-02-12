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

package model.api

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

import java.util.UUID

import org.joda.time.LocalDate
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil

class EarlierYearUpdateSpec extends TestUtil with UnitSpec {



  lazy val earlierYearUpdate1 =  EarlierYearUpdate(
    earlierYearUpdateId = UUID.fromString("cf1886e7-ae56-4ec2-84a6-926d64ace287"),
    taxablePayEYU = BigDecimal(6543.21),
    taxEYU = BigDecimal(123.45),
    receivedDate = new LocalDate("2016-06-26"))


  "Employment" should {

    "generate employmentId when none is supplied" in {
      val eyu = EarlierYearUpdate(
        taxablePayEYU = BigDecimal(1.11),
        taxEYU = BigDecimal(22.22),
        receivedDate = new LocalDate("2015-05-29"))

      eyu.earlierYearUpdateId.toString.nonEmpty shouldBe true
      eyu.earlierYearUpdateId shouldNot be(earlierYearUpdate1.earlierYearUpdateId)
    }
  }
}


