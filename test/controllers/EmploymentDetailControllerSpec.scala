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

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import model.api.{CompanyBenefit, Employment, EmploymentStatus, PayAndTax}
import models.taxhistory.Person
import org.joda.time.LocalDate
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class EmploymentDetailControllerSpec extends BaseControllerSpec {

  trait HappyPathSetup {

    implicit val actorSystem = ActorSystem("test")
    implicit val materializer = ActorMaterializer()
    lazy val controller = {

      val c = injected[EmploymentDetailController]
      val cbUUID = UUID.randomUUID()
      val companyBenefits = List(CompanyBenefit(cbUUID, "EmployerProvidedServices", 1000.00, Some(1)),
        CompanyBenefit(cbUUID, "CarFuelBenefit", 1000))

      val payAndTax = PayAndTax(
        taxablePayTotal = Some(4896.80),
        taxTotal = Some(979.36),
        earlierYearUpdates = List.empty
      )

      val employment =  Employment(
        employmentId = UUID.fromString("01318d7c-bcd9-47e2-8c38-551e7ccdfae3"),
        payeReference = "paye-1",
        employerName = "employer-1",
        startDate = LocalDate.parse("2016-01-21"),
        endDate = Some(LocalDate.parse("2017-01-01")),
        companyBenefitsURI = None,
        payAndTaxURI = None,
        employmentStatus = EmploymentStatus.Live
      )
      val person = Some(Person(Some("first name"),Some("second name"), false))

      when(c.authConnector.authorise(any(), Matchers.any[Retrieval[~[Option[AffinityGroup], Enrolments]]]())(any(), any())).thenReturn(
        Future.successful(new ~[Option[AffinityGroup], Enrolments](Some(AffinityGroup.Agent) , Enrolments(newEnrolments))))
      when(c.taxHistoryConnector.getPayAndTaxDetails(any(), any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(payAndTax)))))
      when(c.taxHistoryConnector.getEmployment(any(), any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(employment)))))
      when(c.taxHistoryConnector.getCompanyBenefits(any(), any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(companyBenefits)))))
      when(c.citizenDetailsConnector.getPersonDetails(any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.OK,Some(Json.toJson(person)))))
      c
    }
  }

  "EmploymentDetailController" must {
    "successfully load Employment details page" in new HappyPathSetup {
     val result = controller.getEmploymentDetails(UUID.randomUUID().toString,2014)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include ("first name second name")

    }

    "load select client page when there is no nino in session" in new HappyPathSetup {
      val result = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SelectClientController.getSelectClientPage().url)
    }

    "show technical error page when status is other than 200, 401, 404" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getEmployment(any(), any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,
          None)))
      val result = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.EmploymentSummaryController.getTaxHistory(2014).url)
    }

    "show technical error page when getEmploymentDetails returns status other than 200, 401, 404" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getPayAndTaxDetails(any(), any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,
          None)))
      val result = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession("USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include (Messages("employmenthistory.employment.details.title"))
    }

    "show technical error page when getCompanyBenefits returns status other than 200, 401, 404" in new HappyPathSetup {
      when(controller.taxHistoryConnector.getCompanyBenefits(any(), any(), any())(any())).
        thenReturn(Future.successful(HttpResponse(Status.NOT_FOUND,
          None)))
      val result = controller.getEmploymentDetails(UUID.randomUUID().toString, 2014)(fakeRequest.withSession(
        "USER_NINO" -> nino))
      status(result) shouldBe Status.OK
      contentAsString(result) should include (Messages("employmenthistory.employment.details.title"))
    }
  }
}
