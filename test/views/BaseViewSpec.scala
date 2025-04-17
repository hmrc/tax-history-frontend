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

package views

import support.GuiceAppSpec

trait BaseViewSpec { this: GuiceAppSpec =>
  def expectedPageTitle(preTitle: String): String      = s"$preTitle - ${messages("lbl.service.title")} - GOV.UK"
  def expectedErrorPageTitle(preTitle: String): String =
    s"${messages("lbl.error")}: $preTitle - ${messages("lbl.service.title")} - GOV.UK"

}
