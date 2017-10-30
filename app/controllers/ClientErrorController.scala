package controllers

import javax.inject.Inject

import play.api.i18n.MessagesApi
import play.api.mvc.Action
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

class ClientErrorController @Inject()(implicit val messagesApi: MessagesApi) extends FrontendController {

  def getNotAuthorised() = Action.async {
    implicit request => {
      request.session.get("USER_NINO").map(Nino(_)) match {
        case Some(nino) => Future.successful(Ok(views.html.errors.not_authorised(nino.toString())))
        case None => Future.successful(Redirect(controllers.routes.SelectClientController.getSelectClientPage()))
      }
    }
  }
}
