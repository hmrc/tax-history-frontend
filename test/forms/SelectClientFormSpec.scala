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

import form.SelectClientForm.selectClientForm
import models.taxhistory.SelectClient
import play.api.data.FormError
import play.api.libs.json.Json
import support.BaseSpec
import utils.TestUtil
class SelectClientFormSpec extends BaseSpec with TestUtil {

  private val maxChar: Int = 100

  "SelectClientForm" must {

    "return no errors with valid data" in {
      val postData = Json.obj(
        "clientId" -> nino
      )

      val validatedForm = selectClientForm.bind(postData, maxChar)
      val errors        = validatedForm.errors
      errors shouldBe empty
    }

    "return no errors with valid data(white spaces)" in {
      val postData = Json.obj(
        "clientId" -> s"   $nino   "
      )

      val validatedForm = selectClientForm.bind(postData, maxChar)
      val errors        = validatedForm.errors
      errors shouldBe empty
    }

    "return the correct result when filled" in {
      val postData      = Json.obj(
        "clientId" -> nino
      )
      val validatedForm = selectClientForm.bind(postData, maxChar)

      selectClientForm.fill(SelectClient(nino)) shouldBe validatedForm
    }

    "return an invalid length error when no nino is entered" in {
      val postData = Json.obj(
        "clientId" -> ""
      )

      val validatedForm = selectClientForm.bind(postData, maxChar)
      validatedForm.errors shouldBe List(FormError("clientId", List("employmenthistory.select.client.error.empty")))
    }

    "return an invalid length error when nino has only white spaces" in {
      val postData = Json.obj(
        "clientId" -> "   "
      )

      val validatedForm = selectClientForm.bind(postData, maxChar)
      validatedForm.errors shouldBe List(FormError("clientId", List("employmenthistory.select.client.error.empty")))
    }

    "return an invalid format error when nino is shorter than nine characters" in {
      val postData = Json.obj(
        "clientId" -> "123456"
      )

      val validatedForm = selectClientForm.bind(postData, maxChar)
      val errors        = validatedForm.errors
      errors shouldBe List(FormError("clientId", List("employmenthistory.select.client.error.invalid-format")))
    }

    "return an invalid format error when nino is longer than nine characters" in {
      val postData = Json.obj(
        "clientId" -> "1234567890"
      )

      val validatedForm = selectClientForm.bind(postData, maxChar)
      val errors        = validatedForm.errors
      errors shouldBe List(FormError("clientId", List("employmenthistory.select.client.error.invalid-format")))
    }

    "return an invalid format value error when an invalid nino entered" in {
      val postData = Json.obj(
        "clientId" -> "1234XXXXA"
      )

      val validatedForm = selectClientForm.bind(postData, maxChar)
      validatedForm.errors shouldBe List(
        FormError("clientId", List("employmenthistory.select.client.error.invalid-format"))
      )
    }
  }
}
