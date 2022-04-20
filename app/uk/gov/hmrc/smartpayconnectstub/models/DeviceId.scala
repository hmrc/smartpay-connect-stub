package uk.gov.hmrc.smartpayconnectstub.models

import uk.gov.hmrc.smartpayconnectstub.utils.ValueClassBinder.valueClassBinder
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.PathBindable
import reactivemongo.bson.BSONObjectID

final case class DeviceId(value: String)

object DeviceId {
  implicit val format: Format[DeviceId] = implicitly[Format[String]].inmap(DeviceId(_), _.value)
  implicit val journeyIdBinder: PathBindable[DeviceId] = valueClassBinder(_.value)
  def fresh: DeviceId = DeviceId(BSONObjectID.generate.stringify)
}
