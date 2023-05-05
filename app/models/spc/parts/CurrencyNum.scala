package models.spc.parts

import play.api.libs.json.{Format, Json}

/**
 * Currency Iso Num https://en.wikipedia.org/wiki/ISO_4217
 */
final case class CurrencyNum(value: String) extends AnyVal

object CurrencyNum {
  implicit val format: Format[CurrencyNum] = Json.valueFormat[CurrencyNum]
  val gbp: CurrencyNum = CurrencyNum("826")
  val usd = CurrencyNum("840")
  val eur = CurrencyNum("978")
}


//final case class CurrencyNum(value: String) {
//  def toCurrencyCode = value match {
//    case "826" => "GBP"
//    case "840" => "USD"
//    case "978" => "EUR"
//    case x     => x
//  }
//}

