package models.spc.parts

import models.TransactionNumber
import play.api.libs.json.{Json, OFormat}

import scala.xml.Node

final case class MessageNode(
                              transactionNumber: TransactionNumber,
                              deviceId:          DeviceId,
                              sourceId:          SourceId
                            ) extends SpcXmlNode {

  def toXml: Node = {
    <MESSAGE>
      <TRANS_NUM>{ transactionNumber.value }</TRANS_NUM>
      <DEVICE_ID>{ deviceId.value }</DEVICE_ID>
      <SOURCE_ID>{ sourceId.value }</SOURCE_ID>
    </MESSAGE>
  }
}

object MessageNode {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[MessageNode] = Json.format[MessageNode]
}
