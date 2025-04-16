resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns
)

addSbtPlugin("uk.gov.hmrc"        % "sbt-auto-build"     % "3.24.0")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"      % "2.2.1")
addSbtPlugin("uk.gov.hmrc"        % "sbt-distributables" % "2.6.0")
addSbtPlugin("org.playframework"  % "sbt-plugin"         % "3.0.7")
addSbtPlugin("io.github.irundaia" % "sbt-sassify"        % "1.5.2")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"        % "0.6.4")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"       % "2.5.2")
