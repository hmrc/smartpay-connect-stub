import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28"   % "7.13.0",
    "uk.gov.hmrc"           %% "play-frontend-hmrc"           % "6.6.0-play-28",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-28"           % "0.74.0",
    "org.julienrf"          %% "play-json-derived-codecs"     % "7.0.0",
    "com.beachape"          %% "enumeratum-play"              % "1.7.2",
    "com.beachape"          %% "enumeratum"                   % "1.7.2",
    "org.typelevel"         %% "cats-core"                    % "2.9.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %% "scalatest"                  % "3.2.15",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.62.2"
  ).map(_ % Test)
}
