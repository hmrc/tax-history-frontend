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

package views.errors

import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import support.GuiceAppSpec
import uk.gov.hmrc.domain.Nino
import utils.TestUtil
import views.html.errors.not_authorised
import views.{BaseViewSpec, Fixture}

class NotAuthorisedSpec extends GuiceAppSpec with BaseViewSpec {

  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  trait ViewFixture extends Fixture with TestUtil {
    val nino: Nino                                 = randomNino
    val view: HtmlFormat.Appendable                = inject[not_authorised].apply(Some(nino))
    val viewHtmlViaRender: HtmlFormat.Appendable   =
      inject[not_authorised].render(Some(nino), request, messages, appConfig)
    val viewHtmlViaFunction: HtmlFormat.Appendable = inject[not_authorised].f(Some(nino))(request, messages, appConfig)
  }

  "NotAuthorisedView" when {

    "view.apply()" should {

      "have correct title, heading and GA page view event" in new ViewFixture {

        document(view).title mustBe expectedPageTitle(messages("employmenthistory.not.authorised.title"))
        document(view).getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
        document(view).getElementById("back-link").text mustBe Messages("lbl.back")
        document(view).select("h1").text() mustBe Messages("employmenthistory.not.authorised.header")
        document(view).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.text1", nino.value)
        ) should not be empty
        document(view).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.text2")
        ) should not be empty
        document(view)
          .getElementsMatchingOwnText(Messages("employmenthistory.not.authorised.select.client.link.text"))
          .attr("href") mustBe "/tax-history/select-client"
        document(view).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.invite.client.link.text")
        ) should not be empty
        document(view).getElementById("service").`val` mustBe "PERSONAL-INCOME-RECORD"
        document(view).getElementById("clientIdentifier").`val` mustBe s"$nino"
        document(view)
          .getElementsByTag("form")
          .attr(
            "action"
          ) mustBe s"${appConfig.agentInvitationFastTrack}?continue=%2Ftax-history%2Fselect-client&error=%2Ftax-history%2Fnot-authorised"
      }
    }

    "view.render()" should {

      "have correct title, heading and GA page view event" in new ViewFixture {

        document(viewHtmlViaRender).title mustBe expectedPageTitle(messages("employmenthistory.not.authorised.title"))
        document(viewHtmlViaRender).getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
        document(viewHtmlViaRender).getElementById("back-link").text mustBe Messages("lbl.back")
        document(viewHtmlViaRender).select("h1").text() mustBe Messages("employmenthistory.not.authorised.header")
        document(viewHtmlViaRender).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.text1", nino.value)
        ) should not be empty
        document(viewHtmlViaRender).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.text2")
        ) should not be empty
        document(viewHtmlViaRender)
          .getElementsMatchingOwnText(Messages("employmenthistory.not.authorised.select.client.link.text"))
          .attr("href") mustBe "/tax-history/select-client"
        document(viewHtmlViaRender).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.invite.client.link.text")
        ) should not be empty
        document(viewHtmlViaRender).getElementById("service").`val` mustBe "PERSONAL-INCOME-RECORD"
        document(viewHtmlViaRender).getElementById("clientIdentifier").`val` mustBe s"$nino"
        document(viewHtmlViaRender)
          .getElementsByTag("form")
          .attr(
            "action"
          ) mustBe s"${appConfig.agentInvitationFastTrack}?continue=%2Ftax-history%2Fselect-client&error=%2Ftax-history%2Fnot-authorised"
      }
    }

    "view.f()" should {

      "have correct title, heading and GA page view event" in new ViewFixture {

        document(viewHtmlViaFunction).title mustBe expectedPageTitle(messages("employmenthistory.not.authorised.title"))
        document(viewHtmlViaFunction).getElementById("back-link").attr("href") mustBe "/tax-history/select-client"
        document(viewHtmlViaFunction).getElementById("back-link").text mustBe Messages("lbl.back")
        document(viewHtmlViaFunction).select("h1").text() mustBe Messages("employmenthistory.not.authorised.header")
        document(viewHtmlViaFunction).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.text1", nino.value)
        ) should not be empty
        document(viewHtmlViaFunction).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.text2")
        ) should not be empty
        document(viewHtmlViaFunction)
          .getElementsMatchingOwnText(Messages("employmenthistory.not.authorised.select.client.link.text"))
          .attr("href") mustBe "/tax-history/select-client"
        document(viewHtmlViaFunction).getElementsMatchingOwnText(
          Messages("employmenthistory.not.authorised.invite.client.link.text")
        ) should not be empty
        document(viewHtmlViaFunction).getElementById("service").`val` mustBe "PERSONAL-INCOME-RECORD"
        document(viewHtmlViaFunction).getElementById("clientIdentifier").`val` mustBe s"$nino"
        document(viewHtmlViaFunction)
          .getElementsByTag("form")
          .attr(
            "action"
          ) mustBe s"${appConfig.agentInvitationFastTrack}?continue=%2Ftax-history%2Fselect-client&error=%2Ftax-history%2Fnot-authorised"
      }
    }
  }
}
