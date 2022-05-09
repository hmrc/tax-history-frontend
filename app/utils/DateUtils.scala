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

package utils

import model.api.{Employment, EmploymentStatus}
import play.api.i18n.Messages
import uk.gov.hmrc.play.language.LanguageUtils

import java.time.LocalDate
import javax.inject.Inject

class DateUtils @Inject()(languageUtils: LanguageUtils) {

  private def noRecord(implicit messages: Messages): String = messages("lbl.date.no-record")

  def format(date: LocalDate)(implicit messages: Messages): String = languageUtils.Dates.formatDate(date)
  def format(date: Option[LocalDate])(implicit messages: Messages): String = {
    date.fold("")(languageUtils.Dates.formatDate)
  }

  def getStartDate(employment: Employment)(implicit messages: Messages): String = {
    employment.startDate.fold(noRecord){date => format(date)}
  }

  def getEndDate(employment: Employment)(implicit messages: Messages): String = {
    val ongoing = messages("lbl.end-date.ongoing")
    employment.employmentStatus match {
      case EmploymentStatus.PotentiallyCeased => noRecord
      case EmploymentStatus.Unknown           => employment.endDate.fold(noRecord){date => format(date)}
      case _                                  => employment.endDate.fold(ongoing){date => format(date)}
    }
  }

}
