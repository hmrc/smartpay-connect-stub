import play.sbt.PlayImport.PlayKeys

val appName = "smartpay-connect-stub"

val strictBuilding: SettingKey[Boolean] = StrictBuilding.strictBuilding //defining here so it can be set before running sbt like `sbt 'set Global / strictBuilding := true' ...`
StrictBuilding.strictBuildingSetting

lazy val scalaCompilerOptions: Seq[String] = Seq(
  "-language:implicitConversions",
  "-language:reflectiveCalls",
  "-Wconf:cat=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

lazy val strictScalaCompilerOptions: Seq[String] = Seq(
  "-Xfatal-warnings",
  "-Xlint:-missing-interpolator,_",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:-byname-implicit",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:explicits",
  "-Ywarn-unused:params",
  "-Ywarn-unused:implicits",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wconf:cat=unused-imports&src=html/.*:s",
  "-Wconf:src=routes/.*:s"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.10",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort := 9263,
    TwirlKeys.templateImports ++= Seq(),
    scalacOptions ++= scalaCompilerOptions,
    scalacOptions ++= {
      if (StrictBuilding.strictBuilding.value) strictScalaCompilerOptions else Nil
    },
    Compile / doc / scalacOptions := Seq(), //this will allow to have warnings in `doc` task
    Test / doc / scalacOptions := Seq() //this will allow to have warnings in `doc` task
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(ScalariformSettings())
  .settings(WartRemoverSettings.wartRemoverSettingsCommon)
  .settings(WartRemoverSettings.wartRemoverSettingsPlay)
