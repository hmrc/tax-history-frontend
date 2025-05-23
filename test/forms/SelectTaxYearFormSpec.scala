/*
 * Copyright 2025 HM Revenue & Customs
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

package forms

import form.SelectTaxYearForm.selectTaxYearForm
import models.taxhistory.SelectTaxYear
import play.api.data.FormError
import play.api.libs.json.Json
import support.BaseSpec
import utils.TestUtil

class SelectTaxYearFormSpec extends BaseSpec with TestUtil {

  private val maxChar: Int = 100

  "SelectTaxYear" must {

    "return no errors with valid data" in {
      val postData = Json.obj(
        "selectTaxYear" -> "2016"
      )

      val validatedForm = selectTaxYearForm.bind(postData, maxChar)
      val errors        = validatedForm.errors
      errors shouldBe empty
    }

    "return the correct result when filled" in {
      val postData      = Json.obj(
        "selectTaxYear" -> "2016"
      )
      val validatedForm = selectTaxYearForm.bind(postData, maxChar)

      selectTaxYearForm.fill(SelectTaxYear(Some("2016"))) shouldBe validatedForm
    }

    "return an invalid length error when no tax year is selected" in {
      val postData = Json.obj(
        "selectTaxYear" -> ""
      )

      val validatedForm = selectTaxYearForm.bind(postData, maxChar)
      validatedForm.errors shouldBe List(
        FormError("selectTaxYear", List("employmenthistory.select.tax.year.error.linktext"))
      )
    }
  }
}
