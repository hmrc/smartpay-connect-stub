import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-30"   % "8.4.0",
    "uk.gov.hmrc"           %% "play-frontend-hmrc-play-30"   % "8.5.0",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"           % "1.7.0",
    "org.julienrf"          %% "play-json-derived-codecs"     % "10.1.0",
    "com.beachape"          %% "enumeratum-play"              % "1.8.0",
    "org.typelevel"         %% "cats-core"                    % "2.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %% "scalatest"                  % "3.2.17",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.62.2"
  ).map(_ % Test)
}
