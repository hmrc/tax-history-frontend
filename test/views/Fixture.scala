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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import views.Strings.TextHelpers

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

trait Fixture extends Matchers {

  def view: HtmlFormat.Appendable

  def document(html: Html): Document = Jsoup.parse(html.toString())

  def attributes(element: Element): Map[String, String] = element.attributes
    .iterator()
    .asScala
    .toList
    .map(attr => attr.getKey -> attr.getValue)
    .toMap

  lazy val form: Element    = document(view).getElementsByTag("form").first()
  lazy val heading: Element = document(view).getElementsByTag("h1").first()

  def validateParagraphizedContent(messageKey: String)(implicit messages: Messages): Unit =
    for (p <- Jsoup.parse(messages(messageKey).paragraphize).getElementsByTag("p").asScala)
      document(view).body().toString must include(p.text())

  def validateConditionalContent(id: String): Assertion =
    Try {
      document(view).getElementById(id).text
    } match {
      case Success(_) => fail()
      case Failure(_) => succeed
    }

}
