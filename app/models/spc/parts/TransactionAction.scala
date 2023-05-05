package models.spc.parts

import julienrf.json.derived
import play.api.libs.json.Format

sealed trait TransactionAction

object TransactionAction {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: Format[TransactionAction] = derived.oformat[TransactionAction]()

  case object AuthorizeAndSettle extends TransactionAction { override def toString: String = "auth_n_settle" }
}
