import play.core.PlayVersion
import sbt._

object AppDependencies {

  val silencerVersion = "1.7.0"

  private val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"            %% "domain"                           % "5.10.0-play-27",
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-27"       % "3.0.0",
    "uk.gov.hmrc"            %% "play-frontend-govuk"              % "0.65.0-play-27",
    "uk.gov.hmrc"            %% "play-frontend-hmrc"               % "0.51.0-play-27",
    "uk.gov.hmrc"            %% "play-partials"                    % "7.1.0-play-27",
    "uk.gov.hmrc"            %% "url-builder"                      % "3.4.0-play-27",
    "uk.gov.hmrc"            %% "auth-client"                      % "5.1.0-play-26",
    "uk.gov.hmrc"            %% "agent-mtd-identifiers"            % "0.23.0-play-27",
    "uk.gov.hmrc"            %% "tax-year"                         % "1.2.0",
    "com.typesafe.play"      %% "play-json-joda"                   % "2.9.0"

  )

  private val test: Seq[ModuleID] = Seq(
    "org.jsoup"              %  "jsoup"                   % "1.13.1",
    "com.typesafe.play"      %% "play-test"               % PlayVersion.current,
    "org.mockito"            %% "mockito-scala"           % "1.14.4",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "4.0.3",
    "org.pegdown"            % "pegdown"                  % "1.6.0"
  ).map(_ % "test, it")

  private val silencerDependencies: Seq[ModuleID] = Seq(
   "com.github.ghik"        % "silencer-lib"             % silencerVersion % Provided cross CrossVersion.full,
    compilerPlugin("com.github.ghik" % "silencer-plugin"     % silencerVersion cross CrossVersion.full)
  )

  def apply(): Seq[ModuleID] = compile ++ test ++ silencerDependencies
}
