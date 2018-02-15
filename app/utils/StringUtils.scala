/*
 * Copyright 2018 HM Revenue & Customs
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

package utils

import model.api.{Employment, EmploymentStatus}
import play.api.i18n.Messages

object StringUtils {
  def uppercaseToTitleCase(s: String): String = {
    if (!s.exists(_.isLower)) s.toLowerCase.capitalize else s
  }

  def getEmploymentStatus(employment: Employment)(implicit messages: Messages): String = {
    val current = Messages("lbl.employment.status.current")
    val ceased = Messages("lbl.employment.status.ceased")
    val unknown = Messages("lbl.employment.status.unknown")

    employment.employmentStatus match {
      case EmploymentStatus.Live    => current
      case EmploymentStatus.Unknown => unknown
      case _                        => ceased
    }
  }

  def getEndDate(employment: Employment)(implicit messages: Messages): String = {
    val unknown = Messages("lbl.end-date.unknown")
    val ongoing = Messages("lbl.end-date.ongoing")
    employment.employmentStatus match {
      case EmploymentStatus.PotentiallyCeased => unknown
      case EmploymentStatus.Unknown           => employment.endDate.map(DateHelper.formatDate).getOrElse(unknown)
      case _                                  => employment.endDate.map(DateHelper.formatDate).getOrElse(ongoing)
    }
  }

}