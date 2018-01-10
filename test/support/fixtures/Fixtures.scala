/*
 * Copyright 2018 HM Revenue & Customs
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

package support.fixtures

import org.joda.time.DateTime
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import utils.TestUtil

trait Fixtures {
  def buildFakeAuthority(withPaye: Boolean = true, withSa: Boolean = false,
                         confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200, nino: Nino = Nino(TestUtil.randomNino.toString()),
                         userDetailsLink: Option[String] = Some("/userDetailsLink")) = Authority(
    uri = "/auth/oid/tuser",
    accounts = Accounts(
      paye = if(withPaye) Some(PayeAccount("/paye/"+nino.nino, nino)) else None

    ),
    loggedInAt = None,
    previouslyLoggedInAt = Some(new DateTime()),
    credentialStrength = CredentialStrength.Strong,
    confidenceLevel = confidenceLevel,
    userDetailsLink = userDetailsLink,
    enrolments = Some("/userEnrolmentsLink"),
    ids = None,
    legacyOid = ""
  )
}
