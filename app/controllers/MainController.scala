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

package controllers

import javax.inject.Inject

import config.{ConfigDecorator, FrontendAuthConnector}
import connectors.TaxHistoryConnector
import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{Enrolment, _}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.{Actions, DelegationAwareActions}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.time.TaxYearResolver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait BaseController extends FrontendController with Actions

class MainController @Inject()(
                                val configDecorator: ConfigDecorator,
                                val taxHistoryConnector: TaxHistoryConnector,
                                override val authConnector: FrontendAuthConnector
                              ) extends BaseController with AuthorisedFunctions{


  def get() = Action.async {

    implicit request => {
      var nino:Option[Nino] = Some(Nino("AA000003A"))
      authorised(Enrolment("HMRC-AS-AGENT") and AuthProviders(GovernmentGateway)) {

        val cy1 = TaxYearResolver.currentTaxYear - 1
        nino match {
          case Some(nino) =>
            taxHistoryConnector.getTaxHistory(nino, cy1) map {
              taxHistory =>
                Ok(Json.toJson(taxHistory))
            }
          case None =>
            Future.successful(NotFound("User had no nino"))
        }
      }

    }
  }
}
