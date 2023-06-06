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

import config.AppConfig
import form.{SelectClientForm, SelectTaxYearForm}
import model.api.EmploymentStatus.Live
import model.api.{Allowance, CompanyBenefit, Employment}
import models.taxhistory.{SelectClient, SelectTaxYear}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.scalatestaccessibilitylinter.views.AutomaticAccessibilitySpec
import views.html._
import views.html.errors._
import views.html.taxhistory._
import views.models.EmploymentViewDetail

class FrontendAccessibilitySpec extends AutomaticAccessibilitySpec {

  private val appConfig: AppConfig                      = app.injector.instanceOf[AppConfig]
  implicit val arbitraryAppConfig: Arbitrary[AppConfig] = fixed(appConfig)

  implicit val arbitrarySelectClientInput: Arbitrary[Form[SelectClient]] = fixed(SelectClientForm.selectClientForm)

  override def renderViewByClass: PartialFunction[Any, Html] = {
    case error_template: error_template                       => render(error_template)
    case deceased: deceased                                   => render(deceased)
    case mci_restricted: mci_restricted                       => render(mci_restricted)
    case no_agent_services_account: no_agent_services_account => render(no_agent_services_account)
    case no_data: no_data                                     => render(no_data)
    case not_authorised: not_authorised                       =>
      not_authorised.render(
        nino = Some(Nino("AM242413B")),
        appConfig = appConfig,
        request = fakeRequest,
        messages = messages
      )
    case technical_error: technical_error                     => render(technical_error)
    case signedOut: SignedOut                                 => render(signedOut)
    case confirm_details: confirm_details                     => render(confirm_details)
    case employment_detail: employment_detail                 =>
      employment_detail.render(
        taxYear = 2023,
        payAndTaxOpt = None,
        employment = Employment(
          payeReference = "paye",
          employerName = "employer",
          employmentPaymentType = None,
          startDate = None,
          employmentStatus = Live,
          worksNumber = "1"
        ),
        companyBenefits =
          List(CompanyBenefit(iabdType = "allowanceType", amount = BigDecimal(1), isForecastBenefit = true)),
        clientNameOrNino = "AM242413B",
        incomeSource = None,
        employmentViewDetail = EmploymentViewDetail("heading", "title"),
        request = fakeRequest,
        messages = messages,
        appConfig = appConfig
      )
    case employment_summary: employment_summary               =>
      employment_summary.render(
        nino = "AM242413B",
        taxYear = 2023,
        employments = List(
          Employment(
            payeReference = "paye",
            employerName = "employer",
            employmentPaymentType = None,
            startDate = None,
            employmentStatus = Live,
            worksNumber = "1"
          )
        ),
        allowances = List(Allowance(iabdType = "1", amount = 1)),
        person = None,
        taxAccount = None,
        statePension = None,
        incomeTotals = None,
        formattedNowDate = "25 January 2015",
        request = fakeRequest,
        messages = messages,
        appConfig = appConfig
      )
    case select_client: select_client                         => render(select_client)
    case select_tax_year: select_tax_year                     =>
      select_tax_year.render(
        form = SelectTaxYearForm.selectTaxYearForm,
        taxYears = List("selectTaxYear" -> "2016"),
        taxYearFromSession = SelectTaxYear(None),
        clientName = None,
        nino = "AM242413B",
        request = fakeRequest,
        messages = messages,
        appConfig = appConfig
      )

  }

  override def viewPackageName: String = "views.html"

  override def layoutClasses: Seq[Class[_]] = Seq(classOf[govuk_wrapper])

  runAccessibilityTests()

}
