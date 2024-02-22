/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.CitizenDetailsConnector
import routes._
import models.taxhistory.Person
import org.mockito.ArgumentMatchers.{any, eq => argEq}
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.twirl.api.Html
import support.{BaseSpec, ControllerSpec}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, Enrolments}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import views.html.taxhistory.confirm_details

import scala.concurrent.{ExecutionContext, Future}

class ConfirmDetailsControllerSpec extends ControllerSpec with BaseSpec {

  trait Setup {
    val name: String   = "Hazel Young"
    val person: Person = Person(Some("Hazel"), Some("Young"), Some(false))

    lazy val controller: ConfirmDetailsController = new ConfirmDetailsController(
      authConnector = mock[AuthConnector],
      citizenDetailsConnector = mock[CitizenDetailsConnector],
      config = app.configuration,
      env = environment,
      cc = messagesControllerComponents,
      appConfig = appConfig,
      confirmDetails = injected[confirm_details]
    )(stubControllerComponents().executionContext)

    when(
      controller.authConnector.authorise(any[Predicate], any[Retrieval[~[Option[AffinityGroup], Enrolments]]])(
        any[HeaderCarrier],
        any[ExecutionContext]
      )
    ).thenReturn(
      Future.successful(
        new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent), Enrolments(newEnrolments))
      )
    )
  }

  "ConfirmDetailsController" must {

    "successfully load confirm details page with name when citizen details returns 200 OK" in new Setup {
      implicit val request: Request[_] = fakeRequest

      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(status = OK, json = Json.toJson(person), headers = Map.empty)))

      val result: Future[Result] = controller.getConfirmDetailsPage()(fakeRequestWithNino)
      val expectedView: Html     = inject[confirm_details].apply(name, nino)

      status(result) shouldBe OK
      result.rendersTheSameViewAs(expectedView)
    }

    "successfully load confirm details page with no name when citizen details returns 200 OK" in new Setup {
      implicit val request: Request[_] = fakeRequest

      when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(
            HttpResponse(status = OK, json = Json.toJson(Person(None, None, Some(false))), headers = Map.empty)
          )
        )

      val result: Future[Result] = controller.getConfirmDetailsPage()(fakeRequestWithNino)
      val expectedView: Html     = inject[confirm_details].apply(nino, nino)

      status(result) shouldBe OK
      result.rendersTheSameViewAs(expectedView)
    }

    "successfully redirect to next page (Select Tax Year Page)" in new Setup {
      val result: Future[Result] = controller.submitConfirmDetailsPage()(fakeRequestWithNino)

      status(result)           shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(SelectTaxYearController.getSelectTaxYearPage().url)
    }

    "successfully redirect to client error page" when {
      def test(errorStatus: Int, url: String): Unit =
        s"citizen details returns $errorStatus" in new Setup {
          when(controller.citizenDetailsConnector.getPersonDetails(argEq(Nino(nino)))(any[HeaderCarrier]))
            .thenReturn(Future.successful(HttpResponse(status = errorStatus, body = "")))

          val result: Future[Result] = controller.getConfirmDetailsPage()(fakeRequestWithNino)

          status(result)           shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(url)
        }

      val input = Seq(
        (LOCKED, ClientErrorController.getMciRestricted().url),
        (GONE, ClientErrorController.getDeceased().url),
        (INTERNAL_SERVER_ERROR, ClientErrorController.getTechnicalError().url)
      )

      input.foreach(args => (test _).tupled(args))
    }
  }
}
