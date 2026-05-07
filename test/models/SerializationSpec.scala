/*
 * Copyright 2026 HM Revenue & Customs
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

package models

import model.api.{IncomeSource, TaAllowance, TaDeduction}
import models.taxhistory.{EarlierYearUpdate, SelectTaxYear}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class SerializationSpec extends AnyWordSpecLike with Matchers {

  val taDeduction: TaDeduction = TaDeduction(
    `type` = 5,
    npsDescription = "nps",
    amount = BigDecimal.decimal(4L),
    sourceAmount = Option(BigDecimal.decimal(87L))
  )
  val taAllowance: TaAllowance = TaAllowance(
    `type` = 5,
    npsDescription = "nps",
    amount = BigDecimal.decimal(4L),
    sourceAmount = Option(BigDecimal.decimal(87L))
  )

  "All the models" should {

    "EarlierYearUpdate" should {
      val update = EarlierYearUpdate(
        receivedDate = LocalDate.EPOCH
      )

      val json         = Json.toJson(update)
      val deserialized = json.as[EarlierYearUpdate]

      deserialized.shouldBe(update)

      "serialize and deserialize to/from JSON" in {
        val json = Json.toJson(update)
        json.validate[EarlierYearUpdate] shouldBe JsSuccess(update)
      }

      "fail to deserialize invalid JSON" in {
        val invalidJson = Json.obj("invalid" -> "data")
        invalidJson.validate[EarlierYearUpdate].isError shouldBe true
      }

      "have a working equals and hashCode" in {
        update          shouldEqual update
        update.hashCode shouldEqual update.hashCode
      }

      "have a working toString" in {
        update.toString should include("EarlierYearUpdate")
      }
    }

    "SelectTaxYear" should {
      val select = SelectTaxYear(
        taxYear = Some("2025/2026")
      )

      val json = Json.toJson(select)

      "serialize and deserialize to/from JSON" in {
        json.validate[SelectTaxYear] shouldBe JsSuccess(select)
      }

      "have a working equals and hashCode" in {
        select          shouldEqual select
        select.hashCode shouldEqual select.hashCode
      }

      "have a working toString" in {
        select.toString should include("SelectTaxYear")
      }
    }

    "TaDeduction" should {
      val json = Json.toJson(taDeduction)

      "serialize and deserialize to/from JSON" in {
        json.validate[TaDeduction] shouldBe JsSuccess(taDeduction)
      }

      "fail to deserialize invalid JSON" in {
        val invalidJson = Json.obj("invalid" -> "data")
        invalidJson.validate[TaDeduction].isError shouldBe true
      }

      "have a working equals and hashCode" in {
        taDeduction          shouldEqual taDeduction
        taDeduction.hashCode shouldEqual taDeduction.hashCode
      }

      "have a working toString" in {
        taDeduction.toString should include("TaDeduction")
      }
    }

    "TaAllowance" should {
      val json         = Json.toJson(taAllowance)
      val deserialized = json.as[TaAllowance]

      "serialize and deserialize to/from JSON" in {
        json.validate[TaAllowance] shouldBe JsSuccess(deserialized)
      }

      "fail to deserialize invalid JSON" in {
        val invalidJson = Json.obj("invalid" -> "data")
        invalidJson.validate[TaAllowance].isError shouldBe true
      }

      "have a working equals and hashCode" in {
        deserialized          shouldEqual deserialized
        deserialized.hashCode shouldEqual deserialized.hashCode
      }

      "have a working toString" in {
        deserialized.toString should include("TaAllowance")
      }
    }

    "IncomeSource" should {
      val incomeSource = IncomeSource(
        employmentId = 234,
        employmentType = 1,
        actualPUPCodedInCYPlusOneTaxYear = Option(BigDecimal.decimal(4234L)),
        deductions = List(taDeduction),
        allowances = List(taAllowance),
        taxCode = "321L",
        basisOperation = Option(2),
        employmentTaxDistrictNumber = 5,
        employmentPayeRef = "ref5"
      )

      "serialize and deserialize to/from JSON if all fields are present" in {
        val json = Json.toJson(incomeSource)
        json.validate[IncomeSource] shouldBe JsSuccess(incomeSource)
      }

      "fail to deserialize invalid JSON" in {
        val invalidJson = Json.obj("invalid" -> "data")
        invalidJson.validate[IncomeSource].isError shouldBe true
      }

      "have a working equals and hashCode" in {
        incomeSource          shouldEqual incomeSource
        incomeSource.hashCode shouldEqual incomeSource.hashCode
      }

      "have a working toString" in {
        incomeSource.toString should include("IncomeSource")
      }
    }
  }

}
