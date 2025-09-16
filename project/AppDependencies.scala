import sbt.*

object AppDependencies {

  private val bootstrapPlayVersion: String = "9.19.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "12.10.0",
    "uk.gov.hmrc" %% "domain-play-30" % "11.0.0",
    "uk.gov.hmrc" %% "tax-year" % "6.0.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion,
    "org.jsoup" % "jsoup" % "1.20.1"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
