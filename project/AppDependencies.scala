import sbt._

object AppDependencies {

  val bootstrapVersion = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"   % bootstrapVersion,
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"   % "12.32.1",
    "com.beachape"          %% "enumeratum-play"              % "1.9.7"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30" % bootstrapVersion
  ).map(_ % Test)
}
