/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.Strings.TextHelpers

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

trait Fixture extends Matchers {
  implicit val request: Request[AnyContentAsEmpty.type] = FakeRequest().withCSRFToken

  def view: HtmlFormat.Appendable

  lazy val html: String = view.body
  lazy val doc: Document = Jsoup.parse(html)
  lazy val form: Element = doc.getElementsByTag("form").first()
  lazy val heading: Element = doc.getElementsByTag("h1").first()
  lazy val subHeading: Element = doc.getElementsByClass("heading-secondary").first()
  lazy val errorSummary: Element = doc.getElementsByClass("amls-error-summary").first()

  def validateParagraphizedContent(messageKey: String)(implicit messages: Messages): Unit = {
    for (p <- Jsoup.parse(messages(messageKey).paragraphize).getElementsByTag("p").asScala) {
      doc.body().toString must include(p.text())
    }
  }

  def validateConditionalContent(id: String) = {
    Try {
      doc.getElementById(id).text
    } match {
      case Success(_) => fail()
      case Failure(_) => succeed
    }
  }

}
