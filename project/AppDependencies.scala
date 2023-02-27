import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28"   % "7.13.0",
    "uk.gov.hmrc"           %% "play-frontend-hmrc"           % "6.6.0-play-28",
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-28"           % "0.74.0",
    "org.julienrf"          %% "play-json-derived-codecs"     % "7.0.0",
    "com.beachape"          %% "enumeratum-play"              % "1.7.2",
    "org.typelevel"         %% "cats-core"                    % "2.9.0",
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                  % "3.2.15"  % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.62.2" % Test,
  )
}
