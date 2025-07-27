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

package model.api

import play.api.libs.json._

sealed trait EmploymentStatus

object EmploymentStatus {

  case object Live extends EmploymentStatus
  case object PotentiallyCeased extends EmploymentStatus
  case object Ceased extends EmploymentStatus
  case object PermanentlyCeased extends EmploymentStatus

  private val LIVE: Int              = 1
  private val POTENTIALLYCEASED: Int = 2
  private val CEASED: Int            = 3
  private val PERMANENTLYCEASED      = 6

  implicit val jsonReads: Reads[EmploymentStatus] =
    (__ \ "employmentStatus").read[Int].flatMap[EmploymentStatus] {
      case LIVE              => Reads(_ => JsSuccess(Live))
      case POTENTIALLYCEASED => Reads(_ => JsSuccess(PotentiallyCeased))
      case CEASED            => Reads(_ => JsSuccess(Ceased))
      case PERMANENTLYCEASED => Reads(_ => JsSuccess(PermanentlyCeased))
      case _                 => Reads(_ => JsError(JsPath \ "employmentStatus", JsonValidationError("Invalid EmploymentStatus")))
    }

  implicit val jsonWrites: Writes[EmploymentStatus] = Writes[EmploymentStatus] {
    case Live              => Json.obj("employmentStatus" -> LIVE)
    case PotentiallyCeased => Json.obj("employmentStatus" -> POTENTIALLYCEASED)
    case Ceased            => Json.obj("employmentStatus" -> CEASED)
    case PermanentlyCeased => Json.obj("employmentStatus" -> PERMANENTLYCEASED)
  }
}
