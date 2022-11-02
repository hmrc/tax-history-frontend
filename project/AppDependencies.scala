import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "domain"                     % "8.1.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.8.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "3.32.0-play-28",
    "uk.gov.hmrc" %% "play-partials"              % "8.3.0-play-28",
    "uk.gov.hmrc" %% "url-builder"                % "3.6.0-play-28",
    "uk.gov.hmrc" %% "agent-mtd-identifiers"      % "0.47.0-play-28",
    "uk.gov.hmrc" %% "tax-year"                   % "3.0.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"          % "3.2.14",
    "org.jsoup"               % "jsoup"              % "1.15.3",
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current,
    "org.mockito"            %% "mockito-scala"      % "1.17.12",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0",
    "org.scalatestplus"      %% "mockito-3-4"        % "3.2.10.0",
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID]      = compile ++ test
}
