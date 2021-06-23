import sbt.Keys.{evictionWarningOptions, resolvers}
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "tax-history-frontend"
val silencerVersion = "1.7.1"

lazy val microservice =  Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
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

mainClass in assembly := Some("play.core.server.ProdServerStart")
fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

assemblyMergeStrategy in assembly := {
  case manifest if manifest.contains("MANIFEST.MF") =>
    // We don't need manifest files since sbt-assembly will create
    // one with the given settings
    MergeStrategy.discard
  case referenceOverrides if referenceOverrides.contains("reference-overrides.conf") =>
    // Keep the content for all reference-overrides.conf files
    MergeStrategy.concat
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard

  case x => MergeStrategy.first
}



