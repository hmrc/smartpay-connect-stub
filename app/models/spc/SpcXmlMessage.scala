package models.spc

import play.api.libs.json.{Format, Json}

import scala.util._
import scala.xml.Elem

/**
 * This represents raw xml message received from Spc (Smart Pay Connect)
 */
final case class SpcXmlMessage(xml: String) {
  val elem: Elem = Try(scala.xml.XML.loadString(xml)) match {
    case Success(n) => n
    case Failure(_) => throw new RuntimeException("not a valid XML")
  }
}

object SpcXmlMessage {
  implicit val format: Format[SpcXmlMessage] = Json.valueFormat[SpcXmlMessage]
}
