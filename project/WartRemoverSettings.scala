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
          Wart.ImplicitConversion,
          Wart.ImplicitParameter,
          Wart.Nothing,
          Wart.Overloading,
          Wart.SizeIs,
          Wart.SortedMaxMinOption,
          Wart.Throw,
          Wart.ToString,
          Wart.JavaSerializable,
          Wart.Serializable,
          Wart.Product,
          Wart.PlatformDefault
        )
        else Nil
      },
      Test / compile / wartremoverErrors --= Seq(
        Wart.MutableDataStructures,
        Wart.ScalaApp,
        Wart.StringPlusAny,
        Wart.ThreadSleep,
        Wart.Any,
        Wart.Equals,
        Wart.GlobalExecutionContext,
        Wart.Null,
        Wart.NonUnitStatements,
        Wart.PublicInference
      )
    )

}
