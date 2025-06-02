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

package utils

import model.api.{Employment, EmploymentStatus, PayAndTax, StatePension}
import play.api.i18n.Messages
import uk.gov.hmrc.play.language.LanguageUtils

import java.time.{Instant, LocalDate, ZoneOffset}
import javax.inject.Inject

class DateUtils @Inject() (languageUtils: LanguageUtils) {

  def formatEmploymentDatesAbbrMonth(employment: Employment)(implicit messages: Messages): Employment =
    employment.copy(
      startDateFormatted = Some(formatStartDate(employment, dateToFormattedAbbrMonthString)),
      endDateFormatted = Some(formatEndDate(employment, dateToFormattedAbbrMonthString))
    )

  def formatEmploymentDates(employment: Employment)(implicit messages: Messages): Employment =
    employment.copy(
      startDateFormatted = Some(formatStartDate(employment, dateToFormattedString)),
      endDateFormatted = Some(formatEndDate(employment, dateToFormattedString))
    )

  def formatEarlierYearUpdateReceivedDate(payAndTax: PayAndTax)(implicit messages: Messages): PayAndTax =
    payAndTax.copy(earlierYearUpdates =
      payAndTax.earlierYearUpdates
        .map(eyu => eyu.copy(receivedDateFormatted = Some(dateToFormattedString(eyu.receivedDate))))
    )

  def formatStatePensionStartDate(statePension: StatePension)(implicit messages: Messages): StatePension =
    statePension.startDate.fold(statePension)(date =>
      statePension.copy(startDateFormatted = Some(dateToFormattedString(date)))
    )

  def nowDateFormatted(implicit messages: Messages): String = dateToFormattedString(
    Instant.now().atOffset(ZoneOffset.UTC).toLocalDate
  )

  def dateToFormattedString(date: LocalDate)(implicit messages: Messages): String = languageUtils.Dates.formatDate(date)

  private def dateToFormattedAbbrMonthString(date: LocalDate)(implicit messages: Messages): String =
    languageUtils.Dates.formatDateAbbrMonth(date)

  private def noRecord(implicit messages: Messages): String = messages("lbl.date.no-record")

  def formatStartDate(employment: Employment, dateFormat: LocalDate => String)(implicit messages: Messages): String =
    employment.startDate.fold(noRecord)(date => dateFormat(date))

  def formatEndDate(employment: Employment, dateFormat: LocalDate => String)(implicit messages: Messages): String = {
    val ongoing = messages("lbl.end-date.ongoing")
    employment.employmentStatus match {
      case EmploymentStatus.PotentiallyCeased => noRecord
      //TODO: Unknown needs to be removed
      case EmploymentStatus.Unknown           => employment.endDate.fold(noRecord)(date => dateFormat(date))
      case _                                  => employment.endDate.fold(ongoing)(date => dateFormat(date))
    }
  }

}
