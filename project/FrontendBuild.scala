import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "tax-history-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "domain" % "5.3.0",
    "uk.gov.hmrc" %% "bootstrap-play-25" % "4.9.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.22.0",
    "uk.gov.hmrc" %% "play-ui" % "7.32.0-play-25",
    "uk.gov.hmrc" %% "play-partials" % "6.5.0",
    "uk.gov.hmrc" %% "url-builder" % "3.1.0",
    "uk.gov.hmrc" %% "auth-client" % "2.19.0-play-25",
    "uk.gov.hmrc" %% "agent-mtd-identifiers" % "0.13.0",
    "uk.gov.hmrc" %% "tax-year" % "0.4.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "3.4.0-play-25" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.10.2" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.mockito" % "mockito-all" % "2.0.2-beta" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope
  )

}
