package models.spc.parts

import play.api.libs.json.{Format, Json}

final case class Country(value: String) extends AnyVal

object Country {
  implicit val format: Format[Country] = Json.valueFormat[Country]
  val UkCountry: Country = Country("826")
  val US = Country("840")
  val Bel = Country("056")
}
