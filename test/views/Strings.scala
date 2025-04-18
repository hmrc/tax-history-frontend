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

import play.twirl.api.Html

object Strings {

  implicit class ConsoleHelpers(s: String) {
    def in(colour: String): String = s"$colour$s${Console.RESET}"
  }

  trait LineBreakConverter {
    def convert(input: String): String
  }

  implicit val defaultLineBreakConverter: LineBreakConverter = (input: String) =>
    input.replaceAll("""\s*\n\s*""", "</p><p>")

  implicit class TextHelpers(s: String) {
    def convertLineBreaks(implicit converter: LineBreakConverter): String = converter.convert(s)
    def convertLineBreaksH(implicit converter: LineBreakConverter): Html  = Html(converter.convert(s))
    def paragraphize(implicit converter: LineBreakConverter): String      = s"<p>${converter.convert(s)}</p>"
    def paragraphizeH(implicit converter: LineBreakConverter): Html       = Html(s"<p>${converter.convert(s)}</p>")
  }

}
