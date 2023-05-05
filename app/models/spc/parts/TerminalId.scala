package models.spc.parts


import play.api.libs.json.{Format, Json}

final case class TerminalId(value: String) extends AnyVal {
  def receiptValue: String = value.replaceAll(".(?=.{4})", "*")
}

object TerminalId {
  implicit val format: Format[TerminalId] = Json.valueFormat
}
