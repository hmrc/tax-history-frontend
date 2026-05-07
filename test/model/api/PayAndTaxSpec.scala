/*
 * Copyright 2026 HM Revenue & Customs
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

import java.util.UUID
import play.api.libs.json.Json
import support.BaseSpec
import utils.TestUtil

import java.time.LocalDate

class PayAndTaxSpec extends TestUtil with BaseSpec {

  lazy val payAndTax: PayAndTax = PayAndTax(
    payAndTaxId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf559"),
    taxablePayTotal = Some(BigDecimal(76543.21)),
    taxTotal = Some(BigDecimal(6666.66)),
    studentLoan = None,
    paymentDate = Some(LocalDate.parse("2016-02-20"))
  )

  "PayAndTax" should {

    "generate employmentId when none is supplied" in {
      val payAndTaxWithoutEmploymentId = PayAndTax(
        taxablePayTotal = Some(BigDecimal(1212.12)),
        taxTotal = Some(BigDecimal(34.34)),
        studentLoan = None,
        paymentDate = Some(LocalDate.parse("2016-02-20"))
      )

      payAndTaxWithoutEmploymentId.payAndTaxId.toString.nonEmpty shouldBe true
      payAndTaxWithoutEmploymentId.payAndTaxId                  shouldNot be(payAndTax.payAndTaxId)
    }

    "generate a correct list of earlier year updates" in {
      val payAndTaxWithEyus = PayAndTax(
        taxablePayTotal = Some(BigDecimal(23450.12)),
        taxTotal = Some(BigDecimal(2856.44)),
        studentLoan = Some(1254.00),
        studentLoanIncludingEYU = Some(2024.21),
        paymentDate = Some(LocalDate.parse("2016-02-20")),
        earlierYearUpdates = List(
          EarlierYearUpdate(
            earlierYearUpdateId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf559"),
            studentLoanEYU = Some(BigDecimal(234.55)),
            receivedDate = LocalDate.parse("2016-05-10")
          ),
          EarlierYearUpdate(
            earlierYearUpdateId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf551"),
            studentLoanEYU = Some(BigDecimal(534.66)),
            receivedDate = LocalDate.parse("2016-06-15")
          ),
          EarlierYearUpdate(
            earlierYearUpdateId = UUID.fromString("7407debb-5aa2-445d-8633-1875a2ebf539"),
            studentLoanEYU = None,
            receivedDate = LocalDate.parse("2016-12-12")
          )
        )
      )

      payAndTaxWithEyus.earlierYearUpdatesWithStudentLoans.size                          should be(2)
      payAndTaxWithEyus.earlierYearUpdatesWithStudentLoans.map(_.studentLoanEYU.get).sum should be(769.21)
    }

    "ignore EYU fields when deserialising JSON and only use base submission totals" in {
      val jsonWithEyuData = Json.parse(
        """{
          |  "payAndTaxId": "7407debb-5aa2-445d-8633-1875a2ebf559",
          |  "taxablePayTotal": 5000.00,
          |  "taxablePayTotalIncludingEYU": 4000.00,
          |  "taxTotal": 1000.00,
          |  "taxTotalIncludingEYU": 800.00,
          |  "studentLoan": null,
          |  "paymentDate": "2016-02-20",
          |  "earlierYearUpdates": [
          |    {
          |      "earlierYearUpdateId": "11111111-1111-1111-1111-111111111111",
          |      "taxablePayEYU": -600.00,
          |      "taxEYU": -120.00,
          |      "receivedDate": "2016-06-17"
          |    },
          |    {
          |      "earlierYearUpdateId": "22222222-2222-2222-2222-222222222222",
          |      "taxablePayEYU": -400.00,
          |      "taxEYU": -80.00,
          |      "receivedDate": "2016-06-17"
          |    }
          |  ]
          |}""".stripMargin
      )

      val result = jsonWithEyuData.as[PayAndTax]

      result.taxablePayTotal shouldBe Some(BigDecimal(5000.00))
      result.taxTotal        shouldBe Some(BigDecimal(1000.00))
    }
  }
}
