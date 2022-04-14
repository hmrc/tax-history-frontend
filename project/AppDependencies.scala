import play.core.PlayVersion
import sbt._

object AppDependencies {

  val silencerVersion = "1.7.1"

  private val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"            %% "domain"                           % "8.0.0-play-28",
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-28"       % "5.22.0",
    "uk.gov.hmrc"            %% "play-frontend-hmrc"               % "3.13.0-play-28",
    "uk.gov.hmrc"            %% "play-partials"                    % "8.3.0-play-28",
    "uk.gov.hmrc"            %% "url-builder"                      % "3.6.0-play-28",
    "uk.gov.hmrc"            %% "agent-mtd-identifiers"            % "0.35.0-play-28",
    "uk.gov.hmrc"            %% "tax-year"                         % "1.7.0",
    "com.typesafe.play"      %% "play-json-joda"                   % "2.9.2"

  )

  private val test: Seq[ModuleID] = Seq(
    "org.jsoup"              %  "jsoup"                   % "1.14.3",
    "com.typesafe.play"      %% "play-test"               % PlayVersion.current,
    "org.mockito"            %% "mockito-scala"           % "1.17.5",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.scalatestplus"      %% "mockito-3-4"             % "3.2.10.0",
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.35.10",
    "org.pegdown"            % "pegdown"                  % "1.6.0"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
   "com.github.ghik"        % "silencer-lib"             % silencerVersion % Provided cross CrossVersion.full,
    compilerPlugin("com.github.ghik" % "silencer-plugin"     % silencerVersion cross CrossVersion.full)
  )

  def apply(): Seq[ModuleID] = compile ++ test ++ silencerDependencies
}
