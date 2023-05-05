package models.spc.parts

import play.api.libs.json.{Format, Json}

/**
 * Source id used by smartpay connect.
 */
final case class SourceId(value: String) extends AnyVal

object SourceId {
  implicit val format: Format[SourceId] = Json.valueFormat[SourceId]
}
