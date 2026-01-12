import play.sbt.PlayImport.PlayKeys

val appName = "smartpay-connect-stub"

val strictBuilding: SettingKey[Boolean] = StrictBuilding.strictBuilding //defining here so it can be set before running sbt like `sbt 'set Global / strictBuilding := true' ...`
StrictBuilding.strictBuildingSetting

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "3.3.7",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort := 9263,
    TwirlKeys.templateImports ++= Seq(),
    scalacOptions ++= ScalaCompilerFlags.scalaCompilerOptions,
    scalacOptions ++= {
      if (StrictBuilding.strictBuilding.value) ScalaCompilerFlags.strictScalaCompilerOptions else Nil
    },
    Compile / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task
    Test / doc / scalacOptions := Seq() //this will allow to have warnings in `doc` task
  )
  .settings(scalafmtOnCompile := true)
  .settings(WartRemoverSettings.wartRemoverSettingsCommon)
  .settings(WartRemoverSettings.wartRemoverSettingsPlay)
  .settings(SbtUpdatesSettings.sbtUpdatesSettings)

