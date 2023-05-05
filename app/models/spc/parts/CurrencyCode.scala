package models.spc.parts


/**
 * Currency Iso Code https://en.wikipedia.org/wiki/ISO_4217
 */
final case class CurrencyCode(value: String) extends AnyVal

object CurrencyCode {
  val gbp: CurrencyCode = CurrencyCode("GBP")
}
