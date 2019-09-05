import play.core.PlayVersion
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "tax-history-frontend"

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;modgiels/.data/..*;controllers.auth.*;filters.*;forms.*;config.*;" +
      ".*BuildInfo.*;prod.Routes;app.Routes;testOnlyDoNotUseInAppConf.Routes;controllers.ExampleController;controllers.testonly.TestOnlyController",
    ScoverageKeys.coverageMinimum := 70.00,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

val compile = Seq(
  ws,
  "uk.gov.hmrc" %% "domain"                % "5.6.0-play-25",
  "uk.gov.hmrc" %% "bootstrap-play-25"     % "4.16.0",
  "uk.gov.hmrc" %% "govuk-template"        % "5.35.0-play-25",
  "uk.gov.hmrc" %% "play-ui"               % "8.0.0-play-25",
  "uk.gov.hmrc" %% "play-partials"         % "6.9.0-play-25",
  "uk.gov.hmrc" %% "url-builder"           % "3.1.0",
  "uk.gov.hmrc" %% "auth-client"           % "2.28.0-play-25",
  "uk.gov.hmrc" %% "agent-mtd-identifiers" % "0.15.0-play-25",
  "uk.gov.hmrc" %% "tax-year"              % "0.6.0"
)

def test(scope: String = "test") = Seq(
  "uk.gov.hmrc"            %% "hmrctest"           % "3.9.0-play-25"     % scope,
  "org.jsoup"              % "jsoup"               % "1.11.2"            % scope,
  "com.typesafe.play"      %% "play-test"          % PlayVersion.current % scope,
  "org.mockito"            % "mockito-all"         % "2.0.2-beta"        % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1"             % scope
)

lazy val appDependencies: Seq[ModuleID] = compile ++ test()

lazy val microservice =
  Project(appName, file("."))
    .enablePlugins(Seq(
      play.sbt.PlayScala,
      SbtAutoBuildPlugin,
      SbtGitVersioning,
      SbtDistributablesPlugin,
      SbtArtifactory): _*)
    .settings(PlayKeys.playDefaultPort := 9996)
    .settings(scoverageSettings: _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      scalaVersion := "2.11.11",
      libraryDependencies ++= appDependencies,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      routesGenerator := StaticRoutesGenerator
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      majorVersion := 3,
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false
    )
    .settings(resolvers ++= Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.jcenterRepo
    ))

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
  tests map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
  }
