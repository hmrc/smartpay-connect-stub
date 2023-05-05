package models.spc.parts

import enumeratum.EnumEntry
import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait PaymentResult extends EnumEntry

object PaymentResult {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[PaymentResult] = derived.oformat()

  case object OnLine extends PaymentResult
  case object Declined extends PaymentResult
  case object Cancelled extends PaymentResult
  case object NotAuthorised extends PaymentResult
}

