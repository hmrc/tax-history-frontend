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

package form

import models.taxhistory.SelectClient
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import uk.gov.hmrc.domain.Nino.isValid

object SelectClientForm {

  private val normaliseText: String => String = str => str.replaceAll("\\s", "")

  val selectClientForm: Form[SelectClient] =
    Form(
      mapping(
        "clientId" -> text
          .transform(normaliseText, identity[String])
          .transform(_.toUpperCase, identity[String])
          .verifying("employmenthistory.select.client.error.empty", _.nonEmpty)
          .verifying("employmenthistory.select.client.error.invalid-format", text => text.isEmpty || isValid(text))
      )(SelectClient.apply)(SelectClient.unapply)
    )

}
