import sbt._

object AppDependencies {

  val bootstrapVersion = "9.13.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"   % bootstrapVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"   % "12.5.0",
    "org.julienrf"          %% "play-json-derived-codecs"     % "11.0.0",
    "com.beachape"          %% "enumeratum-play"              % "1.9.0",
    "org.typelevel"         %% "cats-core"                    % "2.13.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30" % bootstrapVersion
  ).map(_ % Test)
}
