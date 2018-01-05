/*
 * Copyright 2018 HM Revenue & Customs
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

/*
 * Copyright 2017 HM Revenue & Customs
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

import models.taxhistory.SelectTaxYear
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class SelectTaxYearSpec extends UnitSpec {

  "SelectTaxYear" should {
    "transform into Json from object correctly " in {
      Json.toJson(SelectTaxYear(Some("2014"))) shouldBe Json.obj("taxYear" ->"2014")
    }
    "transform into object from json correctly " in {
      Json.obj("taxYear" ->"2014").as[SelectTaxYear] shouldBe SelectTaxYear(Some("2014"))
    }

  }
}