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

class EmploymentSpec extends TestUtil with UnitSpec {

  lazy val employment1 = Employment(
    employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    payeReference = "paye-1",
    employerName = "employer-1",
    startDate = Some(LocalDate.parse("2016-01-21")),
    endDate = Some(LocalDate.parse("2017-01-01")),
    companyBenefitsURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/company-benefits"),
    payAndTaxURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3/pay-and-tax"),
    employmentURI = Some("/2017/employments/01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
    employmentPaymentType = None,
    employmentStatus = EmploymentStatus.Live,
    worksNumber = "00191048716"
  )

  "Employment" should {

    "generate employmentId when none is supplied" in {
      val emp = Employment(payeReference = "paye-1",
        employerName = "employer-1",
        startDate = Some(LocalDate.parse("2016-01-21")),
        endDate = Some(LocalDate.parse("2017-01-01")),
        companyBenefitsURI = None,
        payAndTaxURI = None,
        employmentPaymentType = None,
        employmentStatus = EmploymentStatus.Live,
        worksNumber = "00191048716"
      )
      emp.employmentId.toString.nonEmpty shouldBe true
      emp.employmentId shouldNot be(employment1.employmentId)
    }
  }
}