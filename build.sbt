import sbt.Keys.{evictionWarningOptions, resolvers}
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "tax-history-frontend"
val silencerVersion = "1.7.0"

lazy val microservice =  Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning,  SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    majorVersion := 3,
    scalaSettings,
    scalaVersion := "2.12.12",
    libraryDependencies ++= AppDependencies(),
    name := appName,
    PlayKeys.playDefaultPort := 9996,
    publishingSettings,
    defaultSettings(),
    retrieveManaged := true,
    resolvers += Resolver.jcenterRepo,
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;modgiels/.data/..*;controllers.auth.*;filters.*;forms.*;config.*;" +
      ".*BuildInfo.*;.*helpers.*;.*Routes.*;controllers.ExampleController;controllers.testonly.TestOnlyController",
    ScoverageKeys.coverageMinimum := 90.00,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
  .settings(TwirlKeys.templateImports ++= Seq(
    "play.twirl.api.HtmlFormat",
    "uk.gov.hmrc.govukfrontend.views.html.components._",
    "uk.gov.hmrc.govukfrontend.views.html.helpers._",
    "uk.gov.hmrc.hmrcfrontend.views.html.components._",
    "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
    ),
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=target/.*",
      s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
    ),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )






