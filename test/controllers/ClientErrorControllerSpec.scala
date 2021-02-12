/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import connectors.CitizenDetailsConnector
import models.taxhistory.Person
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.ControllerSpec
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.http.HttpResponse
import views.TestAppConfig

import scala.concurrent.Future

class ClientErrorControllerSpec extends ControllerSpec with TestAppConfig {

  trait HappyPathSetup extends MockitoSugar {

    implicit val actorSystem: ActorSystem = ActorSystem("test")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val person = Some(Person(Some("firstname"), Some("secondname"), deceased = Some(false)))

    lazy val controller: ClientErrorController = new ClientErrorController(mock[CitizenDetailsConnector], injected[MessagesControllerComponents],
      mock[AuthConnector],
      app.configuration, injected[Environment], appConfig)(stubControllerComponents().executionContext)

    when(controller.citizenDetailsConnector.getPersonDetails(any)(any)).
      thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(person)))))
    when(controller.authConnector.authorise(any, any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any, any)).thenReturn(
      Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))))
  }

  "ClientErrorController" should {
    "get Mci restricted page" in new HappyPathSetup {
      val result = controller.getMciRestricted().apply(FakeRequest())
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.mci.restricted.title"))
    }

    "get Not Authorised page with a NINO and relationship" in new HappyPathSetup {
      val result = controller.getNotAuthorised().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.not.authorised.title"))
    }

    "get Not Authorised page without NINO" in new HappyPathSetup {
      val result = controller.getNotAuthorised().apply(FakeRequest())
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SelectClientController.getSelectClientPage().url)
    }

    "get No Agent Services Account page" in new HappyPathSetup {
      val result = controller.getNoAgentServicesAccountPage().apply(FakeRequest())
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.no.agent.services.account.title"))
    }

    "get deceased page" in new HappyPathSetup {
      val result = controller.getDeceased().apply(FakeRequest())
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.deceased.title"))
    }

    "get No Data Available page" in new HappyPathSetup {
      val result = controller.getNoData(2017).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      val body = bodyOf(await(result))
      body should include(Messages("employmenthistory.no.data.title"))
      body should include(Messages("employmenthistory.no.data.header"))
    }

    "get Technical Error page" in new HappyPathSetup {
      val result = controller.getTechnicalError().apply(FakeRequest())
      status(result) shouldBe OK
      bodyOf(await(result)) should include(Messages("employmenthistory.technical.error.title"))
    }
  }
}