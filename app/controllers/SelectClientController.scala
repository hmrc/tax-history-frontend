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

import config.{FrontendAppConfig, FrontendAuthConnector}
import form.SelectClientForm.selectClientForm
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.urls.Link
import views.html.taxhistory.select_client

import scala.concurrent.Future

class SelectClientController @Inject()(
                                        override val authConnector: FrontendAuthConnector,
                                        override val config: Configuration,
                                        override val env: Environment,
                                        implicit val messagesApi: MessagesApi
                                      ) extends BaseController {

  //TODO Remove this as it is only included to support legacy url
  @Deprecated
  def getLegacySelectClientPage() = Action.async { implicit request => {
      Future.successful(Redirect(controllers.routes.SelectClientController.getSelectClientPage()))
    }
  }

  def getSelectClientPage: Action[AnyContent] = Action.async { implicit request =>
    authorisedForAgent{
      val sidebarLink = Link.toInternalPage(
        url=FrontendAppConfig.AfiHomePage,
        value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
      Future.successful(Ok(select_client(selectClientForm,
        Some(sidebarLink)
      )))
    }
  }

  def submitSelectClientPage(): Action[AnyContent] = Action.async { implicit request =>
    selectClientForm.bindFromRequest().fold(
      formWithErrors ⇒ {
        val sidebarLink = Link.toInternalPage(
          url=FrontendAppConfig.AfiHomePage,
          value = Some(messagesApi("employmenthistory.afihomepage.linktext"))).toHtml
        Future.successful(BadRequest(select_client(formWithErrors,
          Some(sidebarLink)
          )))
      },
      validFormData => {
        authorised(AuthProviderAgents).retrieve(affinityGroupAllEnrolls) {
          case Some(affinityG) ~ allEnrols ⇒
            (isAgent(affinityG), extractArn(allEnrols.enrolments)) match {
              case (`isAnAgent`, Some(_)) => Future successful Redirect(routes.EmploymentSummaryController.getTaxHistory())
                .addingToSession("USER_NINO" -> s"${validFormData.clientId.toUpperCase}")
              case (`isAnAgent`, None) => redirectToSubPage
              case _ => redirectToExitPage
            }
          case _ =>
            redirectToExitPage
        } recover {
          case e ⇒
            handleFailure(e)
        }
      }
    )
  }
}
