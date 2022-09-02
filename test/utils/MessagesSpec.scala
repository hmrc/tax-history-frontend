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

  val MatchSingleQuoteOnly: Regex   = """\w+'{1}\w+""".r
  val MatchBacktickQuoteOnly: Regex = """`+""".r

  "Application" should {
    "have the correct message configs" in {
      messagesApi.messages.size mustBe 4
      messagesApi.messages.keys must contain theSameElementsAs Vector("en", "cy", "default", "default.play")
    }

    "have the correct number of default messages" in {
      defaultMessageKeys.size mustBe 41
    }
  }

  "All message files" should {
    "have the same set of keys" in {
      withClue(mismatchingKeys(englishMessageKeys.keySet, welshMessagesKeys.keySet)) {
        assert(welshMessagesKeys.keySet equals englishMessageKeys.keySet)
      }
    }
    "not have the same messages" in {
      val same = englishMessageKeys.keys.collect({
        case key if englishMessageKeys.get(key) == welshMessagesKeys.get(key) =>
          (key, englishMessageKeys.get(key))
      })

      // 94% of app needs to be translated into Welsh. 94% allows for:
      //   - Messages which just can't be different from English
      //     E.g. addresses, acronyms, numbers, etc.
      //   - Content which is pending translation to Welsh
      same.size.toDouble / englishMessageKeys.size.toDouble < 0.06 mustBe true
    }
    "have a non-empty message for each key" in {
      assertNonEmptyValuesForDefaultMessages()
      assertNonEmptyValuesForwelshMessagesKeys()
    }
    "have no unescaped single quotes in value" in {
      assertCorrectUseOfQuotesForDefaultMessages()
      assertCorrectUseOfQuotesForwelshMessagesKeys()
    }
    "have a resolvable message for keys which take args" in {
      val englishWithArgsMsgKeys = englishMessageKeys collect { case (key, value) if countArgs(value) > 0 => key }
      val welshWithArgsMsgKeys   = welshMessagesKeys collect { case (key, value) if countArgs(value) > 0 => key }
      val missingFromEnglish     = englishWithArgsMsgKeys.toList diff welshWithArgsMsgKeys.toList
      val missingFromWelsh       = welshWithArgsMsgKeys.toList diff englishWithArgsMsgKeys.toList
      missingFromEnglish foreach { key =>
        println(s"Key which has arguments in English but not in Welsh: $key")
      }
      missingFromWelsh foreach { key =>
        println(s"Key which has arguments in Welsh but not in English: $key")
      }
      englishWithArgsMsgKeys.size mustBe welshWithArgsMsgKeys.size
    }
    "have the same args in the same order for all keys which take args" in {
      val englishWithArgsMsgKeysAndArgList = englishMessageKeys collect {
        case (key, value) if countArgs(value) > 0 => (key, listArgs(value))
      }
      val welshWithArgsMsgKeysAndArgList   = welshMessagesKeys collect {
        case (key, value) if countArgs(value) > 0 => (key, listArgs(value))
      }
      val mismatchedArgSequences           = englishWithArgsMsgKeysAndArgList collect {
        case (key, engArgSeq) if engArgSeq != welshWithArgsMsgKeysAndArgList(key) =>
          (key, engArgSeq, welshWithArgsMsgKeysAndArgList(key))
      }
      mismatchedArgSequences foreach { case (key, engArgSeq, welshArgSeq) =>
        println(
          s"key which has different arguments or order of arguments between English and Welsh: " +
            s"$key -- English arg seq=$engArgSeq and Welsh arg seq=$welshArgSeq"
        )
      }
      mismatchedArgSequences.size mustBe 0
    }
  }

  private def isInteger(s: String): Boolean = s forall Character.isDigit

  private def toArgArray(msg: String) = msg.split("\\{|\\}").map(_.trim()).filter(isInteger)

  private def countArgs(msg: String) = toArgArray(msg).size

  private def listArgs(msg: String) = toArgArray(msg).mkString

  private def assertNonEmptyValuesForDefaultMessages() = assertNonEmptyNonTemporaryValues("Default", englishMessageKeys)

  private def assertNonEmptyValuesForwelshMessagesKeys() = assertNonEmptyNonTemporaryValues("Welsh", welshMessagesKeys)

  private def assertCorrectUseOfQuotesForDefaultMessages() = assertCorrectUseOfQuotes("Default", englishMessageKeys)

  private def assertCorrectUseOfQuotesForwelshMessagesKeys() = assertCorrectUseOfQuotes("Welsh", welshMessagesKeys)

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

  private lazy val englishMessageKeys: Map[String, String] = getExpectedMessages("en")

  private lazy val defaultMessageKeys: Seq[String] =
    getExpectedMessages("default").toSeq.diff(englishMessageKeys.toSeq).map(_._1)

  private lazy val welshMessagesKeys: Map[String, String] = getExpectedMessages("cy") -- defaultMessageKeys

  private def getExpectedMessages(languageCode: String) =
    messagesApi.messages.getOrElse(languageCode, throw new Exception(s"Missing messages for $languageCode"))

  private def mismatchingKeys(defaultKeySet: Set[String], welshKeySet: Set[String]) = {
    val test1 =
      listMissingMessageKeys("The following message keys are missing from Welsh Set:", defaultKeySet.diff(welshKeySet))
    val test2 = listMissingMessageKeys(
      "The following message keys are missing from English Set:",
      welshKeySet.diff(defaultKeySet)
    )

    test1 ++ test2
  }
}
