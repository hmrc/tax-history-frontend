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

import config.FrontendAuthConnector
import connectors.CitizenDetailsConnector
import models.taxhistory.Person
import play.api.{Configuration, Environment}
import play.api.i18n.MessagesApi
import play.api.mvc.Action
import uk.gov.hmrc.domain.Nino

import scala.concurrent.Future

class ClientErrorController @Inject()(val citizenDetailsConnector: CitizenDetailsConnector,
                                      override val authConnector: FrontendAuthConnector,
                                      override val config: Configuration,
                                      override val env: Environment,
                                      implicit val messagesApi: MessagesApi) extends BaseController {

  def getNotAuthorised() = Action.async {
    implicit request => {
      request.session.get("USER_NINO").map(Nino(_)) match {
        case Some(nino) => Future.successful(Ok(views.html.errors.not_authorised(nino.toString())))
        case None => Future.successful(Redirect(controllers.routes.SelectClientController.getSelectClientPage()))
      }
    }
  }

  def getMciRestricted() = Action.async {
    implicit request => {
        Future.successful(Ok(views.html.errors.mci_restricted()))
      }
    }

  def getDeceased() = Action.async {
    implicit request => {
      Future.successful(Ok(views.html.errors.deceased()))
    }
  }

  def getNoData(maybePerson:Option[Person]) = Action.async {
    implicit request => {
      request.session.get("USER_NINO").map(Nino(_)).map(
        nino =>
          retrieveCitizenDetails(maybeNino,citizenDetailsConnector).map(
            result => mat

        )
      )
      (maybePerson,) match {
        case (Some(person),_)   if person.getName.isDefined => Future.successful(Ok(views.html.errors.no_data(person.getName.getOrElse(""))))
        case (_,Some(nino)) => Future.successful(Ok(views.html.errors.no_data(nino.toString())))
        case _ => Future.successful(Redirect(controllers.routes.SelectClientController.getSelectClientPage()))
      }
    }
  }

  def getTechnicalError() = Action.async {
    implicit request => {
      Future.successful(Ok(views.html.errors.technical_error()))
    }
  }

}
