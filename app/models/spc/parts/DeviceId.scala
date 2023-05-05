package models.spc.parts

import play.api.libs.json.{Format, Json}

/**
 * Device id used by smartpay connect.
 */
final case class DeviceId(value: String) extends AnyVal

object DeviceId {
  implicit val format: Format[DeviceId] = Json.valueFormat[DeviceId]
}

