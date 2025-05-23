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

package controllers

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import connectors.CitizenDetailsConnector
import models.taxhistory.Person
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito.{mock, when}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{MessagesControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import views.html.errors._

import scala.concurrent.Future

class ClientErrorControllerSpec extends ControllerSpec with BaseSpec {

  trait LocalSetup {

    implicit val actorSystem: ActorSystem   = ActorSystem("test")
    implicit val materializer: Materializer = Materializer(actorSystem)
    val person: Option[Person]              = Some(Person(Some("firstname"), Some("secondname"), deceased = Some(false)))

    lazy val controller: ClientErrorController = new ClientErrorController(
      mock(classOf[CitizenDetailsConnector]),
      injected[MessagesControllerComponents],
      mock(classOf[AuthConnector]),
      app.configuration,
      environment,
      appConfig,
      inject[not_authorised],
      inject[mci_restricted],
      inject[deceased],
      inject[no_data],
      inject[technical_error],
      inject[no_agent_services_account]
    )(stubControllerComponents().executionContext)

    when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
      .thenReturn(Future.successful(HttpResponse(OK, json = Json.toJson(person), Map.empty)))

    when(controller.authConnector.authorise(any(), any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(any(), any()))
      .thenReturn(
        Future.successful(
          new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))
        )
      )
  }

  "ClientErrorController" should {
    "get Mci restricted page" in new LocalSetup {
      implicit val request: Request[_] = FakeRequest()
      val result: Future[Result]       = controller.getMciRestricted().apply(FakeRequest())
      val expectedView: mci_restricted = app.injector.instanceOf[mci_restricted]
      status(result) shouldBe OK

      result rendersTheSameViewAs expectedView()
    }

    "get Not Authorised page with a NINO and relationship" in new LocalSetup {
      val result: Future[Result] = controller.getNotAuthorised().apply(FakeRequest().withSession("USER_NINO" -> nino))
      status(result)        shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.not.authorised.title"))
    }

    "get Not Authorised page without NINO" in new LocalSetup {
      val result: Future[Result] = controller.getNotAuthorised().apply(FakeRequest())
      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SelectClientController.getSelectClientPage().url)
    }

    "get No Agent Services Account page" in new LocalSetup {
      val result: Future[Result] = controller.getNoAgentServicesAccountPage().apply(FakeRequest())
      status(result)        shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.no.agent.services.account.title"))
    }

    "get deceased page" in new LocalSetup {
      implicit val request: Request[_] = FakeRequest()
      val result: Future[Result]       = controller.getDeceased().apply(FakeRequest())
      val expectedView: deceased       = app.injector.instanceOf[deceased]
      status(result) shouldBe OK

      result rendersTheSameViewAs expectedView()
    }

    "get No Data Available page" in new LocalSetup {
      val taxYear                = 2017
      val result: Future[Result] = controller.getNoData(taxYear).apply(fakeRequestWithNino)
      status(result) shouldBe OK
      val body: String = contentAsString(result)
      body should include(Messages("employmenthistory.no.data.title"))
      body should include(Messages("employmenthistory.no.data.header"))
    }

    "get Technical Error page" in new LocalSetup {
      val result: Future[Result] = controller.getTechnicalError().apply(FakeRequest())
      status(result)        shouldBe OK
      contentAsString(result) should include(Messages("employmenthistory.technical.error.title"))
    }

    "redirect to select client page" when {
      "there is no nino in session" in new LocalSetup {
        val taxYear: Int           = 2017
        val result: Future[Result] = controller.getNoData(taxYear).apply(FakeRequest())

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.routes.SelectClientController.getSelectClientPage().url)
      }
    }

    "redirect to technical error page" when {
      "500 is returned from citizenDetailsConnector" in new LocalSetup {
        val taxYear: Int = 2017
        when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
          .thenReturn(
            Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, ""))
          )

        val result: Future[Result] = controller.getNoData(taxYear).apply(fakeRequestWithNino)

        status(result)           shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.routes.ClientErrorController.getTechnicalError().url)
      }
    }

    "return an exception when a query string 'issue' is present in the request" in new LocalSetup {
      val result: Exception = intercept[Exception] {
        controller
          .getNotAuthorised()
          .apply(
            FakeRequest(
              method = "GET",
              path = "/path?issue=test"
            ).withSession("USER_NINO" -> nino)
          )
      }

      result.getMessage shouldBe "Invalid Form Data was submitted to Fast-Track Authorisations"
    }
  }
}
