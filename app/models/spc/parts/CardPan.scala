package models.spc.parts

import play.api.libs.json.{Json, OFormat}

final case class CardPan(value: String) extends AnyVal {
  def receiptValue: String = value.replaceAll(".(?=.{4})", "*")
}

object CardPan {
  implicit val format: OFormat[CardPan] = Json.format[CardPan]
}
