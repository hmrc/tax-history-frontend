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

import model.api.{Employment, EmploymentStatus, PayAndTax}
import play.api.i18n.Messages
import uk.gov.hmrc.play.language.LanguageUtils

import java.time.{Instant, LocalDate, ZoneOffset}
import javax.inject.Inject

class DateUtils @Inject()(languageUtils: LanguageUtils) {

  def formatEmploymentDates(employment: Employment)(implicit messages: Messages): Employment = {
    val startDateFormatted = formatStartDate(employment)
    val endDateFormatted = formatEndDate(employment)
    employment.copy(startDateFormatted = Some(startDateFormatted), endDateFormatted = Some(endDateFormatted))
  }

  def formatEarlierYearUpdateReceivedDate(payAndTax: PayAndTax)(implicit messages: Messages): PayAndTax = {
    payAndTax.copy(earlierYearUpdates = payAndTax.earlierYearUpdates
      .map(eyu => eyu.copy(receivedDateFormatted = Some(dateToFormattedString(eyu.receivedDate)))))
  }

  def nowDateFormatted(implicit messages: Messages): String = dateToFormattedString(Instant.now().atOffset(ZoneOffset.UTC).toLocalDate)

  def dateToFormattedString(date: LocalDate)(implicit messages: Messages): String = languageUtils.Dates.formatDate(date)

  def noRecord(implicit messages: Messages): String = messages("lbl.date.no-record")

  def formatStartDate(employment: Employment)(implicit messages: Messages): String = {
    employment.startDate.fold(noRecord){date => dateToFormattedString(date)}
  }

  def formatEndDate(employment: Employment)(implicit messages: Messages): String = {
    val ongoing = messages("lbl.end-date.ongoing")
    employment.employmentStatus match {
      case EmploymentStatus.PotentiallyCeased => noRecord
      case EmploymentStatus.Unknown           => employment.endDate.fold(noRecord){date => dateToFormattedString(date)}
      case _                                  => employment.endDate.fold(ongoing){date => dateToFormattedString(date)}
    }
  }

}
