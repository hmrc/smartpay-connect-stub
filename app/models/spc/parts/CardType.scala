package models.spc.parts

import play.api.libs.json.{Format, Json}

final case class CardType(value: String) extends AnyVal

object CardType {
  implicit val format: Format[CardType] = Json.valueFormat
  val VisaCredit: CardType = CardType("Visa Credit")
}

