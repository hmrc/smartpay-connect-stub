package models.spc.parts

import julienrf.json.derived
import play.api.libs.json.OFormat

/**
 * SPC- Smart Pay Connect - Interaction Node events
 */
sealed trait CardVerificationMethod

object CardVerificationMethod {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[CardVerificationMethod] = derived.oformat[CardVerificationMethod]()

  final case object Pin extends CardVerificationMethod { override def toString: String = "PIN verified" }
  final case object Signature extends CardVerificationMethod { override def toString: String = "Signature verified" }
  final case object PinAndSignature extends CardVerificationMethod { override def toString: String = "PIN and Signature verified" }
  final case object OnDevice extends CardVerificationMethod { override def toString: String = "On Device verified" }
  final case object NotPerformed extends CardVerificationMethod { override def toString: String = "Not verified" }
  final case object Failed extends CardVerificationMethod { override def toString: String = "Failed verification" }
  final case object Unknown extends CardVerificationMethod { override def toString: String = "Unknown verification" }
}
