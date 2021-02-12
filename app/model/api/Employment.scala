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

package model.api

import java.util.UUID

import model.api.EmploymentPaymentType.{JobseekersAllowance, OccupationalPension}
import org.joda.time.LocalDate
import play.api.libs.functional.syntax.{unlift, _}
import play.api.libs.json.Reads._
import play.api.libs.json._
import utils.LocalDateFormat

case class Employment(employmentId: UUID = UUID.randomUUID(),
                      startDate: Option[LocalDate],
                      endDate: Option[LocalDate] = None,
                      payeReference: String,
                      employerName: String,
                      companyBenefitsURI: Option[String] = None,
                      payAndTaxURI: Option[String] = None,
                      employmentURI: Option[String] = None,
                      employmentPaymentType: Option[EmploymentPaymentType],
                      employmentStatus: EmploymentStatus,
                      worksNumber: String) {
  def isJobseekersAllowance = employmentPaymentType.contains(JobseekersAllowance)

  def isOccupationalPension = employmentPaymentType.contains(OccupationalPension)
}

object Employment extends LocalDateFormat {

  import EmploymentPaymentType.format

  implicit val jsonReads: Reads[Employment] = (
    (JsPath \ "employmentId").read[UUID] and
      (JsPath \ "startDate").readNullable[LocalDate] and
      (JsPath \ "endDate").readNullable[LocalDate] and
      (JsPath \ "payeReference").read[String] and
      (JsPath \ "employerName").read[String] and
      (JsPath \ "companyBenefitsURI").readNullable[String] and
      (JsPath \ "payAndTaxURI").readNullable[String] and
      (JsPath \ "employmentURI").readNullable[String] and
      (JsPath \ "employmentPaymentType").readNullable[EmploymentPaymentType] and
      JsPath.read[EmploymentStatus] and
      (JsPath \ "worksNumber").read[String]
    ) (Employment.apply _)


  implicit val locationWrites: Writes[Employment] = (
    (JsPath \ "employmentId").write[UUID] and
      (JsPath \ "startDate").writeNullable[LocalDate] and
      (JsPath \ "endDate").writeNullable[LocalDate] and
      (JsPath \ "payeReference").write[String] and
      (JsPath \ "employerName").write[String] and
      (JsPath \ "companyBenefitsURI").writeNullable[String] and
      (JsPath \ "payAndTaxURI").writeNullable[String] and
      (JsPath \ "employmentURI").writeNullable[String] and
      (JsPath \ "employmentPaymentType").writeNullable[EmploymentPaymentType] and
      JsPath.write[EmploymentStatus] and
      (JsPath \ "worksNumber").write[String]
    ) (unlift(Employment.unapply))
}

