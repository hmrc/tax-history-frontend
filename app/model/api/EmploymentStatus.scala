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
  case object Unknown extends EmploymentStatus
  case object PermanentlyCeased extends EmploymentStatus

  private val LIVE: Int              = 1
  private val POTENTIALLYCEASED: Int = 2
  private val CEASED: Int            = 3
  private val UNKNOWN: Int           =
    99 // Code 99, Unknown, is internal to tax-history, and is not an wider HMRC employment status
  private val PERMANENTLYCEASED = 6
//Unknown is not a possible value in the new hip api

  implicit val jsonReads: Reads[EmploymentStatus] =
    (__ \ "employmentStatus").read[Int].flatMap[EmploymentStatus] {
      case LIVE              => Reads(_ => JsSuccess(Live))
      case POTENTIALLYCEASED => Reads(_ => JsSuccess(PotentiallyCeased))
      case CEASED            => Reads(_ => JsSuccess(Ceased))
      case UNKNOWN           => Reads(_ => JsSuccess(Unknown))
      case PERMANENTLYCEASED           => Reads(_ => JsSuccess(PermanentlyCeased))
      case _                 => Reads(_ => JsError(JsPath \ "employmentStatus", JsonValidationError("Invalid EmploymentStatus")))
    }

  implicit val jsonWrites: Writes[EmploymentStatus] = Writes[EmploymentStatus] {
    case Live              => Json.obj("employmentStatus" -> LIVE)
    case PotentiallyCeased => Json.obj("employmentStatus" -> POTENTIALLYCEASED)
    case Ceased            => Json.obj("employmentStatus" -> CEASED)
    case Unknown           => Json.obj("employmentStatus" -> UNKNOWN)
    case PermanentlyCeased              => Json.obj("employmentStatus" -> PERMANENTLYCEASED)
  }
}
