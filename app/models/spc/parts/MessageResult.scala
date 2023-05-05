package models.spc.parts
import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait MessageResult

object MessageResult {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[MessageResult] = derived.oformat[MessageResult]()

  case object SuccessResult extends MessageResult { override def toString: String = "success" }
  case object FailureResult extends MessageResult { override def toString: String = "failure" }
}

