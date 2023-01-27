import play.sbt.PlayImport.PlayKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "smartpay-connect-stub"

val silencerVersion = "1.7.5"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
//  .settings(DefaultBuildSettings.scalaSettings: _*)
//  .settings(DefaultBuildSettings.defaultSettings(): _*)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.12.14",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.beachape"   %% "enumeratum"                        % "1.6.1",
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),
    // ***************
    PlayKeys.playDefaultPort := 9263,
    TwirlKeys.templateImports ++= Seq()
  )
  .settings(publishingSettings: _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(ScalariformSettings())
