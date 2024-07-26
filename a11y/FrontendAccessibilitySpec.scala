/*
 * Copyright 2024 HM Revenue & Customs
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
import model.api._
import models.taxhistory.{SelectClient, SelectTaxYear}
import org.scalacheck.Arbitrary
import play.api.data.Form
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.scalatestaccessibilitylinter.views.AutomaticAccessibilitySpec
import views.html._
import views.html.errors._
import views.html.taxhistory._
import views.models.EmploymentViewDetail
import views.taxhistory.Constants

class FrontendAccessibilitySpec extends AutomaticAccessibilitySpec with Constants {

  private val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  private val selectTaxYear: SelectTaxYear = SelectTaxYear(Some("2016"))

  implicit val arbAppConfig: Arbitrary[AppConfig]         = fixed(appConfig)
  override implicit val arbAsciiString: Arbitrary[String] = fixed("/")

  implicit val arbRequestHeader: Arbitrary[RequestHeader]                            = fixed(fakeRequest)
  implicit val arbEmployment: Arbitrary[Employment]                     = fixed(employment)
  implicit val arbStatePension: Arbitrary[StatePension]                 = fixed(statePension)
  implicit val arbSelectTaxYear: Arbitrary[SelectTaxYear]               = fixed(selectTaxYear)
  implicit val arbNino: Arbitrary[Nino]                                 = fixed(Nino("AM242413B"))
  implicit val arbPayAndTax: Arbitrary[PayAndTax]                       = fixed(payAndTax)
  implicit val arbEmploymentViewDetail: Arbitrary[EmploymentViewDetail] = fixed(
    EmploymentViewDetail.apply(isJobseekersAllowance = true, isOccupationalPension = true, "income")(messages)
  )

  implicit val arbListOfEmployments: Arbitrary[List[Employment]]         = fixed(List(employment))
  implicit val arbListOfAllowances: Arbitrary[List[Allowance]]           = fixed(List(allowance1, allowance2))
  implicit val arbListOfCompanyBenefits: Arbitrary[List[CompanyBenefit]] = fixed(
    List(companyBenefits)
  )
  implicit val arbListOfTaxYears: Arbitrary[List[(String, String)]]      = fixed(List("selectTaxYear" -> "2016"))

  implicit val arbSelectClientForm: Arbitrary[Form[SelectClient]] = fixed(SelectClientForm.selectClientForm)
  implicit val arbTaxYearForm: Arbitrary[Form[SelectTaxYear]]     = fixed(
    SelectTaxYearForm.selectTaxYearForm.fill(selectTaxYear)
  )

  override def renderViewByClass: PartialFunction[Any, Html] = {
    case error_template: error_template                       => render(error_template)
    case deceased: deceased                                   => render(deceased)
    case mci_restricted: mci_restricted                       => render(mci_restricted)
    case no_agent_services_account: no_agent_services_account => render(no_agent_services_account)
    case no_data: no_data                                     => render(no_data)
    case not_authorised: not_authorised                       => render(not_authorised)
    case technical_error: technical_error                     => render(technical_error)
    case signedOut: SignedOut                                 => render(signedOut)
    case confirm_details: confirm_details                     => render(confirm_details)
    case employment_detail: employment_detail                 => render(employment_detail)
    case employment_summary: employment_summary               => render(employment_summary)
    case select_client: select_client                         => render(select_client)
    case select_tax_year: select_tax_year                     => render(select_tax_year)
  }

  override def viewPackageName: String = "views.html"

  override def layoutClasses: Seq[Class[govuk_wrapper]] = Seq(classOf[govuk_wrapper])

  runAccessibilityTests()
}
