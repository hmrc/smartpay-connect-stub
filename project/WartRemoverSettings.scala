import play.sbt.routes.RoutesKeys.routes
import play.twirl.sbt.Import.TwirlKeys
import sbt.Keys.*
import sbt.{Compile, Test, *}
import wartremover.Wart
import wartremover.WartRemover.autoImport.*

object WartRemoverSettings {

  lazy val wartRemoverSettingsPlay = Seq(
    wartremoverExcluded ++=
      (Compile / routes).value ++
        Seq(sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala")
        ++ target.value.get // stops a weird wart remover error being thrown
  )

  lazy val wartRemoverSettingsCommon =
    Seq(
      (Compile / compile / wartremoverErrors) ++= {
        if (StrictBuilding.strictBuilding.value) Warts.allBut(
          Wart.DefaultArguments,
          Wart.Equals,
          Wart.ImplicitConversion,
          Wart.ImplicitParameter,
          Wart.JavaSerializable,
          Wart.Nothing,
          Wart.Overloading,
          Wart.PlatformDefault,
          Wart.Product,
          Wart.Serializable,
          Wart.SizeIs,
          Wart.SortedMaxMinOption,
          Wart.Throw,
          Wart.ToString
        )
        else Nil
      },
      Test / compile / wartremoverErrors --= Seq(
        Wart.MutableDataStructures,
        Wart.ScalaApp,
        Wart.StringPlusAny,
        Wart.ThreadSleep,
        Wart.Any,
        Wart.GlobalExecutionContext,
        Wart.Null,
        Wart.NonUnitStatements,
        Wart.PublicInference
      )
    )

}
