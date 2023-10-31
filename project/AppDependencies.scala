import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion: String = "7.23.0"

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % "7.26.0-play-28",
    "uk.gov.hmrc" %% "domain"                     % "8.3.0-play-28",
    "uk.gov.hmrc" %% "tax-year"                   % "3.3.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "org.scalatest"       %% "scalatest"               % "3.2.17",
    "org.jsoup"            % "jsoup"                   % "1.16.2",
    "org.mockito"         %% "mockito-scala-scalatest" % "1.17.29",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID]      = compile ++ test

}
