/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.time.TaxYear

import java.time.LocalDate

object ControllerUtils {

  def getEmploymentStatus(employment: Employment)(implicit messages: Messages): String = {
    val current = Messages("lbl.employment.status.current")
    val ceased  = Messages("lbl.employment.status.ceased")
    val unknown = Messages("lbl.employment.status.unknown")

    employment.employmentStatus match {
      case EmploymentStatus.Live    => current
      case EmploymentStatus.Unknown => unknown
      case _                        => ceased
    }
  }

  def hasEmploymentDetails(employment: Employment): Boolean =
    employment.employmentStatus match {
      case EmploymentStatus.Unknown => false
      case _                        => true
    }

  def displaySource(sourceAmount: Option[BigDecimal], amount: BigDecimal): Option[BigDecimal] =
    if (sourceAmount.contains(amount)) None else sourceAmount

  def displayTaxCodeHeading(taxYear: Int, employmentStatus: EmploymentStatus, employmentEndDate: Option[LocalDate])(
    implicit messages: Messages
  ): String =
    if (TaxYear.current.startYear == taxYear && employmentStatus != EmploymentStatus.Ceased) {
      employmentEndDate match {
        case Some(date) if date.isAfter(LocalDate.now())  => messages("tax.code.subheading.ongoing.employment")
        case Some(date) if date.isBefore(LocalDate.now()) =>
          messages("tax.code.subheading.previous.or.terminated.employment")
        case _                                            => messages("tax.code.subheading.ongoing.employment")
      }
    } else {
      messages("tax.code.subheading.previous.or.terminated.employment")
    }

  def displayTaxCode(basisOperation: Option[Int]): Option[String] =
    basisOperation match {
      case Some(1) | Some(3) => Some("X")
      case _                 => None
    }

  def sentenceCase(input: String): String =
    input.toLowerCase.capitalize

  def isJobSeekerAllowance(employment: Employment)(implicit messages: Messages): String =
    if (employment.isJobseekersAllowance) {
      Messages("employmenthistory.job.seekers")
    } else {
      employment.employerName
    }
}
