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

import config.{ConfigDecorator, FrontendAuthConnector, LocalDelegationConnector}
import controllers.auth.{LocalActions, LocalRegime}
import play.api.mvc._
import services._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.DelegationAwareActions
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.time.TaxYearResolver

import scala.concurrent.Future


trait BaseController extends FrontendController with LocalActions with DelegationAwareActions

class MainController @Inject() (
  val taiService: TaiService,
  val configDecorator: ConfigDecorator,
  val localRegime: LocalRegime,
  val authConnector: FrontendAuthConnector,
  val delegationConnector: LocalDelegationConnector
) extends BaseController {

  val index = ProtectedAction { implicit localContext =>

    val cy1 = TaxYearResolver.currentTaxYear-1

    localContext.nino match {
      case Some(nino) =>
        taiService.taxSummary(nino, cy1) map {
          case TaxSummarySuccessResponse(taxSummary) =>
            Ok(taxSummary.taxSummaryDetails)
          case _ =>
            NotFound("Record not found")
        }
      case None =>
        Future.successful(NotFound("User had no nino"))
    }


  }
}
