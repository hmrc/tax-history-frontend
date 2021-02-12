/*
 * Copyright 2021 HM Revenue & Customs
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

object ControllerUtils {

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

  private def noRecord(implicit messages: Messages) = Messages("lbl.date.no-record")

  def getStartDate(employment: Employment)(implicit messages: Messages): String = {
    employment.startDate.map(startDate => DateHelper.formatDate(startDate)).getOrElse(noRecord)
  }

  def getEndDate(employment: Employment)(implicit messages: Messages): String = {
    val ongoing = Messages("lbl.end-date.ongoing")
    employment.employmentStatus match {
      case EmploymentStatus.PotentiallyCeased => noRecord
      case EmploymentStatus.Unknown           => employment.endDate.map(DateHelper.formatDate).getOrElse(noRecord)
      case _                                  => employment.endDate.map(DateHelper.formatDate).getOrElse(ongoing)
    }
  }

  def hasEmploymentDetails(employment: Employment):Boolean = {
    employment.employmentStatus match{
      case EmploymentStatus.Unknown => false
      case _                        => true
    }
  }

  def displaySource(sourceAmount:Option[BigDecimal], amount: BigDecimal): Option[BigDecimal] =
    if (sourceAmount.contains(amount)) None else sourceAmount

  def displayTaxCode(basisOperation: Option[Int]):Option[String] =
    basisOperation match{
      case (Some(1) | Some(3)) => Some("X")
      case _ => None
    }

  def sentenceCase(input :String): String = {
    input.toLowerCase.capitalize
  }

  def isJobSeekerAllowance(employment: Employment)(implicit messages: Messages): String = {
    if (employment.isJobseekersAllowance) Messages("employmenthistory.job.seekers")
    else employment.employerName
  }
}