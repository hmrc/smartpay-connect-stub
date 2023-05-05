package models.spc.parts

import play.api.libs.json.{Format, Json}

/**
 * Transaction id used by smartpay connect.
 * Format docs: https://confluence.tools.tax.service.gov.uk/display/OPS/Purchase+Description
 */
final case class PurchaseDescription(value: String)

object PurchaseDescription {
  implicit val format: Format[PurchaseDescription] = Json.valueFormat[PurchaseDescription]
}