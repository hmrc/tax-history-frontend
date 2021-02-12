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

import play.api.libs.json._

sealed trait EmploymentPaymentType extends Product with Serializable {
  val name: String
}

object EmploymentPaymentType {
  case object OccupationalPension extends EmploymentPaymentType { val name = "OccupationalPension" }
  case object JobseekersAllowance extends EmploymentPaymentType { val name = "JobseekersAllowance" }
  case object IncapacityBenefit extends EmploymentPaymentType { val name = "IncapacityBenefit" }
  case object EmploymentAndSupportAllowance extends EmploymentPaymentType { val name = "EmploymentAndSupportAllowance" }
  case object StatePensionLumpSum extends EmploymentPaymentType { val name = "StatePensionLumpSum" }
  case object Unknown extends EmploymentPaymentType { val name = "Unknown" }

  def apply(name: String): EmploymentPaymentType = name.trim match {
    case OccupationalPension.name => OccupationalPension
    case JobseekersAllowance.name => JobseekersAllowance
    case IncapacityBenefit.name => IncapacityBenefit
    case EmploymentAndSupportAllowance.name => EmploymentAndSupportAllowance
    case StatePensionLumpSum.name => StatePensionLumpSum
    case _ => Unknown
  }

  def unapply(paymentType: EmploymentPaymentType): Option[String] = Some(paymentType.name)

  private implicit val reads: Reads[EmploymentPaymentType] = new Reads[EmploymentPaymentType] {
    override def reads(json: JsValue): JsResult[EmploymentPaymentType] = json match {
      case JsString(value)  => JsSuccess(EmploymentPaymentType(value))
      case invalid => JsError(s"Invalid EmploymentPaymentType $invalid")
    }
  }

  private implicit val writes: Writes[EmploymentPaymentType] = new Writes[EmploymentPaymentType] {
    override def writes(o: EmploymentPaymentType): JsValue = JsString(o.name)
  }

  implicit val format: Format[EmploymentPaymentType] = Format[EmploymentPaymentType](reads, writes)
}