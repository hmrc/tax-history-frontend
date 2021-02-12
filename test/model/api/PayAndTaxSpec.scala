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

class PayAndTaxSpec extends TestUtil with UnitSpec {

  lazy val payAndTaxNoEyu = PayAndTax(
    payAndTaxId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf559"),
    taxablePayTotal = Some(BigDecimal(76543.21)),
    taxablePayTotalIncludingEYU = Some(BigDecimal(76543.21)),
    taxTotal = Some(BigDecimal(6666.66)),
    taxTotalIncludingEYU = Some(BigDecimal(6666.66)),
    studentLoan = None,
    studentLoanIncludingEYU = None,
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
        studentLoanIncludingEYU = None,
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = Nil)
      payAndTax.payAndTaxId.toString.nonEmpty shouldBe true
      payAndTax.payAndTaxId shouldNot be(payAndTaxNoEyu.payAndTaxId)
    }

    "generate a correct list of earlier year updates" in {
      val payAndTax = PayAndTax(
        taxablePayTotal = Some(BigDecimal(23450.12)),
        taxablePayTotalIncludingEYU = Some(BigDecimal(23935.89)),
        taxTotal = Some(BigDecimal(2856.44)),
        taxTotalIncludingEYU = Some(BigDecimal(3013.99)),
        studentLoan = Some(1254.00),
        studentLoanIncludingEYU = Some(2023.21),
        paymentDate = Some(new LocalDate("2016-02-20")),
        earlierYearUpdates = List(
          EarlierYearUpdate(
          earlierYearUpdateId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf559"),
          taxablePayEYU = BigDecimal(234.44),
          taxEYU = BigDecimal(145.55),
          studentLoanEYU = Some(BigDecimal(234.55)),
            receivedDate = new LocalDate("2016-05-10")),
        EarlierYearUpdate(
          earlierYearUpdateId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf551"),
          taxablePayEYU = BigDecimal(0.0),
          taxEYU = BigDecimal(12.0),
          studentLoanEYU = Some(BigDecimal(534.66)),
          receivedDate = new LocalDate("2016-06-15")),
          EarlierYearUpdate(
            earlierYearUpdateId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf539"),
            taxablePayEYU = BigDecimal(251.33),
            taxEYU = BigDecimal(0.0),
            studentLoanEYU = None,
            receivedDate = new LocalDate("2016-12-12")
          )
        ))

      payAndTax.earlierYearUpdatesWithStudentLoans.size should be(2)
      payAndTax.earlierYearUpdatesWithNonZeroPayOrTax.size should be(3)

      payAndTax.earlierYearUpdatesWithStudentLoans.map(_.studentLoanEYU.get).sum should be(769.21)
      payAndTax.earlierYearUpdatesWithNonZeroPayOrTax.map(_.taxablePayEYU).sum should be(485.77)
      payAndTax.earlierYearUpdatesWithNonZeroPayOrTax.map(_.taxEYU).sum should be(157.55)
    }
  }
}


