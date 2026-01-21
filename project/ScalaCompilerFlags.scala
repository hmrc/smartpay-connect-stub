object ScalaCompilerFlags {

  lazy val scalaCompilerOptions: Seq[String] = Seq(
    "-language:implicitConversions",
    "-language:reflectiveCalls",
    "-language:strictEquality",
    "-Wconf:msg=unused\\simport&src=html/.*:s",
    "-Wconf:src=routes/.*:s"
  )

  lazy val strictScalaCompilerOptions: Seq[String] = Seq(
    "-Xfatal-warnings",
    "-Wunused:explicits",
    "-Wunused:implicits",
    "-Wunused:imports",
    "-Wunused:locals",
    "-Wunused:params",
    "-Wunused:patvars",
    "-Wunused:privates",
    "-deprecation",
    "-feature",
    "-unchecked"
  )
}
