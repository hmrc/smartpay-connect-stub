package models

import play.api.libs.json.{Format, Json}

final case class TransactionNumber(value: String) extends AnyVal

object TransactionNumber {
  implicit val format: Format[TransactionNumber] = Json.valueFormat[TransactionNumber]
}
