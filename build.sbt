import play.sbt.PlayImport.PlayKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "smartpay-connect-stub"


lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
//  .settings(DefaultBuildSettings.scalaSettings: _*)
//  .settings(DefaultBuildSettings.defaultSettings(): _*)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.10",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencies ++= Seq(
      "com.beachape"   %% "enumeratum"                        % "1.7.2"
    ),
    PlayKeys.playDefaultPort := 9263,
    TwirlKeys.templateImports ++= Seq()
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(ScalariformSettings())
