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

package utils

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.i18n.{Lang, Messages}
import support.GuiceAppSpec

import scala.util.matching.Regex

class MessagesSpec extends GuiceAppSpec {

  override implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en"), Lang("cy")))

  val MatchSingleQuoteOnly: Regex = """\w+'{1}\w+""".r
  val MatchBacktickQuoteOnly: Regex = """`+""".r

  private val commonProvidedKeys = Set(
    "error.address.invalid.character"
  )

  private val frameworkProvidedKeys = Set(
    "global.error.badRequest400.heading",
    "global.error.badRequest400.message",
    "global.error.badRequest400.title",
    "global.error.pageNotFound404.heading",
    "global.error.pageNotFound404.message",
    "global.error.pageNotFound404.title"
  )

  private val providedKeys = commonProvidedKeys ++ frameworkProvidedKeys

  "Application" should {
    "have the correct message configs" in {
      messagesApi.messages.size mustBe 4
      messagesApi.messages.keys must contain theSameElementsAs Vector("en", "cy", "default", "default.play")
    }

    "have messages for default and cy only" in {
      messagesApi.messages("en").size mustBe 0
      val englishMessageCount = messagesApi.messages("default").size - frameworkProvidedKeys.size

      messagesApi.messages("cy").size mustBe englishMessageCount
    }
  }

  "All message files" should {
    "have the same set of keys" in {
      withClue(mismatchingKeys(defaultMessages.keySet, welshMessages.keySet)) {
        assert(welshMessages.keySet equals defaultMessages.keySet)
      }
    }
    "not have the same messages" in {
      val same = defaultMessages.keys.collect({
        case key if defaultMessages.get(key) == welshMessages.get(key) =>
          (key, defaultMessages.get(key))
      })

      // 94% of app needs to be translated into Welsh. 94% allows for:
      //   - Messages which just can't be different from English
      //     E.g. addresses, acronyms, numbers, etc.
      //   - Content which is pending translation to Welsh
      same.size.toDouble / defaultMessages.size.toDouble < 0.06 mustBe true
    }
    "have a non-empty message for each key" in {
      assertNonEmptyValuesForDefaultMessages()
      assertNonEmptyValuesForWelshMessages()
    }
    "have no unescaped single quotes in value" in {
      assertCorrectUseOfQuotesForDefaultMessages()
      assertCorrectUseOfQuotesForWelshMessages()
    }
    "have a resolvable message for keys which take args" in {
      val englishWithArgsMsgKeys = defaultMessages collect { case (key, value) if countArgs(value) > 0 => key }
      val welshWithArgsMsgKeys = welshMessages collect { case (key, value) if countArgs(value) > 0     => key }
      val missingFromEnglish = englishWithArgsMsgKeys.toList diff welshWithArgsMsgKeys.toList
      val missingFromWelsh = welshWithArgsMsgKeys.toList diff englishWithArgsMsgKeys.toList
      missingFromEnglish foreach { key =>
        println(s"Key which has arguments in English but not in Welsh: $key")
      }
      missingFromWelsh foreach { key =>
        println(s"Key which has arguments in Welsh but not in English: $key")
      }
      englishWithArgsMsgKeys.size mustBe welshWithArgsMsgKeys.size
    }
    "have the same args in the same order for all keys which take args" in {
      val englishWithArgsMsgKeysAndArgList = defaultMessages collect {
        case (key, value) if countArgs(value) > 0 => (key, listArgs(value))
      }
      val welshWithArgsMsgKeysAndArgList = welshMessages collect {
        case (key, value) if countArgs(value) > 0 => (key, listArgs(value))
      }
      val mismatchedArgSequences = englishWithArgsMsgKeysAndArgList collect {
        case (key, engArgSeq) if engArgSeq != welshWithArgsMsgKeysAndArgList(key) =>
          (key, engArgSeq, welshWithArgsMsgKeysAndArgList(key))
      }
      mismatchedArgSequences foreach {
        case (key, engArgSeq, welshArgSeq) =>
          println(
            s"key which has different arguments or order of arguments between English and Welsh: $key -- English arg seq=$engArgSeq and Welsh arg seq=$welshArgSeq")
      }
      mismatchedArgSequences.size mustBe 0
    }
  }

  private def isInteger(s: String): Boolean = s forall Character.isDigit

  private def toArgArray(msg: String) = msg.split("\\{|\\}").map(_.trim()).filter(isInteger(_))

  private def countArgs(msg: String) = toArgArray(msg).size

  private def listArgs(msg: String) = toArgArray(msg).mkString

  private def assertNonEmptyValuesForDefaultMessages() = assertNonEmptyNonTemporaryValues("Default", defaultMessages)

  private def assertNonEmptyValuesForWelshMessages() = assertNonEmptyNonTemporaryValues("Welsh", welshMessages)

  private def assertCorrectUseOfQuotesForDefaultMessages() = assertCorrectUseOfQuotes("Default", defaultMessages)

  private def assertCorrectUseOfQuotesForWelshMessages() = assertCorrectUseOfQuotes("Welsh", welshMessages)

  private def assertNonEmptyNonTemporaryValues(label: String, messages: Map[String, String]) = messages.foreach {
    case (key: String, value: String) =>
      withClue(s"In $label, there is an empty value for the key:[$key][$value]") {
        value.trim.isEmpty mustBe false
      }
  }

  private def assertCorrectUseOfQuotes(label: String, messages: Map[String, String]) = messages.foreach {
    case (key: String, value: String) =>
      withClue(s"In $label, there is an unescaped or invalid quote:[$key][$value]") {
        MatchSingleQuoteOnly.findFirstIn(value).isDefined mustBe false
        MatchBacktickQuoteOnly.findFirstIn(value).isDefined mustBe false
      }
  }

  private def listMissingMessageKeys(header: String, missingKeys: Set[String]) =
    missingKeys.toList.sorted.mkString(header + displayLine, "\n", displayLine)

  private lazy val displayLine = "\n" + ("@" * 42) + "\n"

  private lazy val defaultMessages: Map[String, String] = getExpectedMessages("default") -- providedKeys

  private lazy val welshMessages: Map[String, String] = getExpectedMessages("cy") -- commonProvidedKeys

  private def getExpectedMessages(languageCode: String) =
    messagesApi.messages.getOrElse(languageCode, throw new Exception(s"Missing messages for $languageCode"))

  private def mismatchingKeys(defaultKeySet: Set[String], welshKeySet: Set[String]) = {
    val test1 =
      listMissingMessageKeys("The following message keys are missing from Welsh Set:", defaultKeySet.diff(welshKeySet))
    val test2 = listMissingMessageKeys(
      "The following message keys are missing from English Set:",
      welshKeySet.diff(defaultKeySet))

    test1 ++ test2
  }
}
