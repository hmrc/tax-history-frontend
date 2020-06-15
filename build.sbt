import play.core.PlayVersion
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "tax-history-frontend"
val silencerVersion = "1.7.0"

lazy val microservice =
  Project(appName, file("."))
    .configs(IntegrationTest)
    .disablePlugins(JUnitXmlReportPlugin)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)

defaultSettings()
scalaSettings
majorVersion := 3
scalaVersion := "2.12.11"
PlayKeys.playDefaultPort := 9996

TwirlKeys.templateImports += "play.twirl.api.HtmlFormat"

libraryDependencies ++= Seq(
  ws,
  "uk.gov.hmrc"            %% "domain"                % "5.9.0-play-26",
  "uk.gov.hmrc"            %% "bootstrap-play-26"     % "1.8.0",
  "uk.gov.hmrc"            %% "govuk-template"        % "5.55.0-play-26",
  "uk.gov.hmrc"            %% "play-ui"               % "8.10.0-play-26",
  "uk.gov.hmrc"            %% "play-partials"         % "6.11.0-play-26",
  "uk.gov.hmrc"            %% "url-builder"           % "3.4.0-play-26",
  "uk.gov.hmrc"            %% "auth-client"           % "3.0.0-play-26",
  "uk.gov.hmrc"            %% "agent-mtd-identifiers" % "0.17.0-play-26",
  "uk.gov.hmrc"            %% "tax-year"              % "1.1.0",
  "com.typesafe.play"      %% "play-json-joda"        % "2.9.0",
  compilerPlugin("com.github.ghik" % "silencer-plugin"     % silencerVersion cross CrossVersion.full),
  "com.github.ghik"        % "silencer-lib"           % silencerVersion % Provided cross CrossVersion.full
)

val testScope = "test, it"
libraryDependencies ++= Seq(
  "uk.gov.hmrc"            %% "bootstrap-play-26"     % "1.6.0"             % Test classifier "tests",
  "uk.gov.hmrc"            %% "hmrctest"              % "3.9.0-play-26"     % testScope,
  "org.jsoup"              % "jsoup"                  % "1.13.1"            % testScope,
  "com.typesafe.play"      %% "play-test"             % PlayVersion.current % testScope,
  "org.mockito"            %% "mockito-scala"         % "1.14.4"            % testScope,
  "org.scalatestplus.play" %% "scalatestplus-play"    % "3.1.3"             % testScope
)

enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning,  SbtDistributablesPlugin,  SbtArtifactory)

publishingSettings
integrationTestSettings
retrieveManaged := true
resolvers ++= Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.jcenterRepo
)

evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)

ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;modgiels/.data/..*;controllers.auth.*;filters.*;forms.*;config.*;" +
  ".*BuildInfo.*;prod.Routes;app.Routes;testOnlyDoNotUseInAppConf.Routes;controllers.ExampleController;controllers.testonly.TestOnlyController"
ScoverageKeys.coverageMinimum := 70.00
ScoverageKeys.coverageFailOnMinimum := true
ScoverageKeys.coverageHighlighting := true

scalacOptions ++= Seq(
  "-P:silencer:pathFilters=target/.*",
  s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
)
