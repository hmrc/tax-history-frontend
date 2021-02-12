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

import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil

class AllowanceSpec extends TestUtil with UnitSpec {



  lazy val allowance1 = Allowance(allowanceId = UUID.fromString("c9923a63-4208-4e03-926d-7c7c88adc7ee"),
    iabdType = "allowanceType",
    amount = BigDecimal(12.00))

  lazy val allowanceList = List(allowance1)

  "Allowance" should {

    "generate allowanceId when none is supplied" in {
      val allowance = Allowance(iabdType = "otherAllowanceType", amount = BigDecimal(10.00))
      allowance.allowanceId.toString.nonEmpty shouldBe true
      allowance.allowanceId shouldNot be(allowance1.allowanceId)
    }

  }
}