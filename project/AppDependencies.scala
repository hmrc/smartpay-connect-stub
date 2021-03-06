import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"           %% "bootstrap-frontend-play-28"   % "5.18.0",
    "uk.gov.hmrc"           %% "play-frontend-hmrc"           % "0.83.0-play-28",
    "uk.gov.hmrc"           %% "play-language"                % "5.1.0-play-28",
    "uk.gov.hmrc"           %% "simple-reactivemongo"         % "8.0.0-play-28",
    "org.julienrf"          %% "play-json-derived-codecs"     % "6.0.0",
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.18.0"             % Test,
    "org.scalatest"           %% "scalatest"                  % "3.2.7"              % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it"
  )
}
