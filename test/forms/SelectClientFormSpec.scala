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

package forms

import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import play.api.libs.json.Json
import form.SelectClientForm.selectClientForm
class SelectClientFormSpec extends PlaySpec {

  "SelectClientForm" must {

    "return no errors with valid data" in {
      val postData = Json.obj(
        "clientId" -> "WM123456C"
      )

      val validatedForm = selectClientForm.bind(postData)

      assert(validatedForm.errors.isEmpty)
    }
    "return an need value error with no nino entered" in {
      val postData = Json.obj(
        "clientId" -> ""
      )

      val validatedForm = selectClientForm.bind(postData)
      assert(validatedForm.errors.contains(FormError("clientId", List("mtdfi.select_client.form.need-value"))))
    }
    "return an longer then 9  error with no nino entered" in {
      val postData = Json.obj(
        "clientId" -> "1234567890"
      )

      val validatedForm = selectClientForm.bind(postData)
      assert(validatedForm.errors.contains(FormError("clientId", List("mtdfi.select_client.form.invalid-value-length"))))
    }
    "return an invalid format value error when an  invalid nino entered" in {
      val postData = Json.obj(
        "clientId" -> "WM1234XXXX"
      )

      val validatedForm = selectClientForm.bind(postData)
      assert(validatedForm.errors.contains(FormError("clientId", List("mtdfi.create_relationship.invalid-nino-format"))))
    }
  }
}
