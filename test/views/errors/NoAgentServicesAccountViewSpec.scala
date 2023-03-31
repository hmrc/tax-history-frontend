/*
 * Copyright 2023 HM Revenue & Customs
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

import play.twirl.api.HtmlFormat
import support.{BaseSpec, GuiceAppSpec}
import utils.TestUtil
import views.html.errors.no_agent_services_account
import views.{BaseViewSpec, Fixture}

class NoAgentServicesAccountViewSpec extends GuiceAppSpec with BaseViewSpec with BaseSpec with TestUtil {

  trait ViewFixture extends Fixture {
    val view: HtmlFormat.Appendable                = inject[no_agent_services_account].apply()(fakeRequest, messages, appConfig)
    val viewHtmlViaRender: HtmlFormat.Appendable   =
      inject[no_agent_services_account].render(fakeRequest, messages, appConfig)
    val viewHtmlViaFunction: HtmlFormat.Appendable =
      inject[no_agent_services_account].f()(fakeRequest, messages, appConfig)
  }

  val titleAndHeadingContent = "Your agent business does not have an Agent Services account"
  val p1Content              = "If you are authorised to act on their behalf, you can create an account now."

  "NoAgentServicesAccountView" should {

    "have the correct title when no form errors have occurred" in new ViewFixture {
      document(view).title shouldBe expectedPageTitle(titleAndHeadingContent)
    }

    "have the correct heading" in new ViewFixture {
      document(view).select("h1").text() shouldBe titleAndHeadingContent
    }

    "have the correct p element content" in new ViewFixture {
      document(view).select("#main-content > div > div > div > div > p").text() shouldBe p1Content
    }

    "view .render()" should {
      "render correctly" in new ViewFixture {
        document(viewHtmlViaRender).title                                                      shouldBe expectedPageTitle(titleAndHeadingContent)
        document(viewHtmlViaRender).select("h1").text()                                        shouldBe titleAndHeadingContent
        document(viewHtmlViaRender).select("#main-content > div > div > div > div > p").text() shouldBe p1Content
      }
    }

    "view .f()" when {
      "supplied the correct parameters create the view" in new ViewFixture {
        document(viewHtmlViaFunction).title                                                      shouldBe expectedPageTitle(titleAndHeadingContent)
        document(viewHtmlViaFunction).select("h1").text()                                        shouldBe titleAndHeadingContent
        document(viewHtmlViaFunction).select("#main-content > div > div > div > div > p").text() shouldBe p1Content
      }
    }
  }
}
