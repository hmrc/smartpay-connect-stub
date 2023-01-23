import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28"   % "5.18.0",
    "uk.gov.hmrc"           %% "play-frontend-hmrc"           % "6.2.0-play-28",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-28"           % "0.73.0",
    "org.julienrf"          %% "play-json-derived-codecs"     % "6.0.0",
    "com.beachape"          %% "enumeratum-play"              % "1.7.2",
    "org.julienrf"          %% "play-json-derived-codecs"     % "6.0.0", //choose carefully
    "org.typelevel"         %% "cats-core"                    % "2.9.0",
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.18.0" % Test,
    "org.scalatest"           %% "scalatest"                  % "3.2.7"  % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8" % Test,
  )
}
