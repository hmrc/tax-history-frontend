/*
 * Copyright 2022 HM Revenue & Customs
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

package models.taxhistory

import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

case class SelectTaxYear(taxYear: Option[String])

object SelectTaxYear {

  implicit val format = Json.format[SelectTaxYear]

  def options(taxYears: List[(String, String)]): Seq[RadioItem] = {

    taxYears.map {
      case (value, label) =>
        RadioItem(
          value = Some(value),
          content = Text(label),
          checked = false,
          id = Some(s"selectTaxYear-$value")
        )
    }
  }
}
