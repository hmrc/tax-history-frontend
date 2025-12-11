import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion: String = "10.4.0"
  private val domainVersion: String        = "13.0.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "12.23.0",
    "uk.gov.hmrc" %% "domain-play-30"             % domainVersion,
    "uk.gov.hmrc" %% "tax-year"                   % "6.0.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion,
    "org.jsoup"    % "jsoup"                  % "1.21.2",
    "uk.gov.hmrc" %% "domain-test-play-30"    % domainVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
