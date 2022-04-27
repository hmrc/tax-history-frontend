package model.api

import support.BaseSpec
import utils.TestUtil

import java.util.UUID

class TotalIncomeSpec extends TestUtil with BaseSpec {

  private val employmentId = UUID.randomUUID()
  private val employment = Employment(employmentId, startDate = None,
    payeReference = "paye-1",
    employerName = "employer-1",
    employmentStatus = EmploymentStatus.Unknown,
    worksNumber = "1",
    employmentPaymentType = None
  )
  val employmentAndIncomeTax1 = EmploymentIncomeAndTax(employmentId.toString, BigDecimal(10), BigDecimal(5))
  private val totalIncome = TotalIncome(
      employmentIncomeAndTax = List(employmentAndIncomeTax1),
      employmentTaxablePayTotalIncludingEYU = BigDecimal(10),
      pensionTaxablePayTotalIncludingEYU = BigDecimal(10),
      employmentTaxTotalIncludingEYU = BigDecimal(10),
      pensionTaxTotalIncludingEYU = BigDecimal(10)
  )

  "TotalIncome" should {

    "Get the employmentIncomeAndTax when it exists for an Employment" in {
      totalIncome.getIncomeAndTax(employment) shouldBe Some(employmentAndIncomeTax1)
    }

    "Not get the employmentIncomeAndTax when it does not exists for an Employment" in {
      totalIncome.getIncomeAndTax(employment.copy(employmentId = UUID.randomUUID())) shouldBe None
    }
  }
}
