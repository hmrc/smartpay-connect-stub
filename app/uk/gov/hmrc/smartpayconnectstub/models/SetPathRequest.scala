package uk.gov.hmrc.smartpayconnectstub.models

import play.api.libs.json.{Json, OFormat}

final case class SetPathRequest(deviceId: String, stubPath: String)

object SetPathRequest {
  implicit val format: OFormat[SetPathRequest] = Json.format[SetPathRequest]
}
