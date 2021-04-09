import play.core.PlayVersion
import sbt._

object AppDependencies {

  val silencerVersion = "1.7.1"

  private val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"            %% "domain"                           % "5.11.0-play-27",
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-27"       % "4.2.0",
    "uk.gov.hmrc"            %% "play-frontend-hmrc"               % "0.57.0-play-27",
    "uk.gov.hmrc"            %% "play-partials"                    % "8.0.0-play-27",
    "uk.gov.hmrc"            %% "url-builder"                      % "3.5.0-play-27",
    "uk.gov.hmrc"            %% "auth-client"                      % "5.2.0-play-26",
    "uk.gov.hmrc"            %% "agent-mtd-identifiers"            % "0.23.0-play-27",
    "uk.gov.hmrc"            %% "tax-year"                         % "1.3.0",
    "com.typesafe.play"      %% "play-json-joda"                   % "2.9.2"

  )

  private val test: Seq[ModuleID] = Seq(
    "org.jsoup"              %  "jsoup"                   % "1.13.1",
    "com.typesafe.play"      %% "play-test"               % PlayVersion.current,
    "org.mockito"            %% "mockito-scala"           % "1.16.37",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "4.0.3",
    "org.pegdown"            % "pegdown"                  % "1.6.0"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
   "com.github.ghik"        % "silencer-lib"             % silencerVersion % Provided cross CrossVersion.full,
    compilerPlugin("com.github.ghik" % "silencer-plugin"     % silencerVersion cross CrossVersion.full)
  )

  def apply(): Seq[ModuleID] = compile ++ test ++ silencerDependencies
}
