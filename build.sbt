import play.sbt.PlayImport.PlayKeys

val appName = "smartpay-connect-stub"


lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.10",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    PlayKeys.playDefaultPort := 9263,
    TwirlKeys.templateImports ++= Seq()
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(ScalariformSettings())
  .settings(scalacOptions ++= Seq(
    "-Wconf:cat=unused-imports&src=html/.*:s",
    "-Wconf:src=routes/.*:s"
  ))
