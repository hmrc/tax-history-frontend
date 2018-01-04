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

object StringUtils {
  def uppcaseToTitleCase(s:String): String ={
    if (!s.exists(_.isLower)) s.toLowerCase.capitalize else s
  }

  private def getDefaultEndDateText(employmentStatus: EmploymentStatus, current: String, noDataAvailable: String) : String  = {
    employmentStatus match {
      case EmploymentStatus.Live => current
      case _ => noDataAvailable
    }
  }

  def getEndDate(employment:Employment, current: String, noDataAvailable: String): String = {
    val defaultText = getDefaultEndDateText(employment.employmentStatus, current, noDataAvailable)
    employment.endDate.fold(defaultText)(date => DateHelper.formatDate(date))

  }

}
