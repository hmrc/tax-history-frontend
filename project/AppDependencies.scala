import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val bootstrapPlayVersion = "7.15.0"

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.3.0-play-28",
    "uk.gov.hmrc" %% "play-partials"              % "8.4.0-play-28",
    "uk.gov.hmrc" %% "domain"                     % "8.2.0-play-28",
    "uk.gov.hmrc" %% "tax-year"                   % "3.1.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatest"       %% "scalatest"               % "3.2.15",
    "org.jsoup"            % "jsoup"                   % "1.15.4",
    "com.typesafe.play"   %% "play-test"               % PlayVersion.current,
    "org.mockito"         %% "mockito-scala-scalatest" % "1.17.14",
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.62.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID]      = compile ++ test
}
