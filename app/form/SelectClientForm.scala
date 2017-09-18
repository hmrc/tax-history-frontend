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

package form

import java.util.Optional

import models.taxhistory.SelectClient
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import uk.gov.hmrc.domain.Nino.isValid

object SelectClientForm {
  val selectClientForm: Form[SelectClient] = {
    Form(mapping(
      "clientId" -> text
        .verifying("selectclient.error.empty", text => nonEmptyInput(text))
        .verifying("selectclient.error.invalid-length", text => nestedCheck(text, nonEmptyInput, lengthCheck))
        .verifying("selectclient.error.invalid-format", text => nestedCheck(text), )
    )(SelectClient.apply)(SelectClient.unapply))
  }


  def lengthCheck(nino: String): Boolean = {
    nino.length == 9
  }

  def nonEmptyInput (nino: String): Boolean = {
    nino.nonEmpty
  }

  def nestedCheck(nino: String, functionA :String => Boolean, functionB :String => Boolean): Boolean = {
    if (!functionA(nino)) true else functionB(nino)
  }

  def genericCheck[T:(String,T, T) => Boolean](nino: String, randomFunction: (String,T, T) => Boolean, otherFunction: (String, Option[T], Option[T])  => Boolean): Boolean = {
    if (!randomFunction(nino)) true else otherFunction(nino, None, None)
    }
  }
}

