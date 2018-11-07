/*
 * Copyright 2018 HM Revenue & Customs
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
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestUtil

class PayAndTaxSpec extends TestUtil with UnitSpec {

  lazy val payAndTaxNoEyu = PayAndTax(
    payAndTaxId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf559"),
    taxablePayTotal = Some(BigDecimal(76543.21)),
    taxablePayTotalIncludingEYU = Some(BigDecimal(76543.21)),
    taxTotal = Some(BigDecimal(6666.66)),
    taxTotalIncludingEYU = Some(BigDecimal(6666.66)),
    studentLoan = None,
    paymentDate = Some(new LocalDate("2016-02-20")),
    earlierYearUpdates = Nil)


  "PayAndTax" should {

    "generate employmentId when none is supplied" in {
      val payAndTax = PayAndTax(
        taxablePayTotal = Some(BigDecimal(1212.12)),
        taxablePayTotalIncludingEYU = Some(BigDecimal(1212.12)),
        taxTotal = Some(BigDecimal(34.34)),
        taxTotalIncludingEYU = Some(BigDecimal(34.34)),
        studentLoan = None,
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = Nil)
      payAndTax.payAndTaxId.toString.nonEmpty shouldBe true
      payAndTax.payAndTaxId shouldNot be(payAndTaxNoEyu.payAndTaxId)
    }
  }
}


