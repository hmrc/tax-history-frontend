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

package views.models

import play.api.i18n.Messages

case class EmploymentViewDetail(heading: String, title: String)

object EmploymentViewDetail {
  def apply(isJobseekersAllowance: Boolean, isOccupationalPension: Boolean, incomeName: String)(implicit messages: Messages): EmploymentViewDetail = {
    (isJobseekersAllowance, isOccupationalPension) match {
      case(true, _) => EmploymentViewDetail(messages("employmenthistory.job.seekers"), messages("employmenthistory.employment.details.job.seekers.title"))
      case(_, true) => EmploymentViewDetail(messages("employmenthistory.jobtype.pension", incomeName), messages("employmenthistory.employment.details.pension.title"))
      case(_, false) =>  EmploymentViewDetail(messages("employmenthistory.jobtype.employment", incomeName), messages("employmenthistory.employment.details.employment.title"))
    }
  }
}
