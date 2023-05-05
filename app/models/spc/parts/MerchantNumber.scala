package models.spc.parts

import play.api.libs.json.{Format, Json}

final case class MerchantNumber(value: String) extends AnyVal {
  def receiptValue: String = value.replaceAll(".(?=.{4})", "*")
}

object MerchantNumber {
  implicit val format: Format[MerchantNumber] = Json.valueFormat
}
