/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.smartpayconnectstub.models

import play.api.libs.json.{Json, OFormat}

import scala.xml.Node

sealed trait ControlMessage extends F2FMessage

object ControlMessage {

  def getControlMessage(node: Node): Option[ControlMessage] = {
    (node \\ "EVENT").text match {
      case CloseWebsocket.name => Some(CloseWebsocket())
      case WebSocketError.name => Some(WebSocketError())
      case _                   => None
    }
  }
}

final case class CloseWebsocket(name: String = CloseWebsocket.name) extends ControlMessage {
  override def toXmlString: String = name
}
case object CloseWebsocket {
  implicit val format: OFormat[CloseWebsocket] = Json.format[CloseWebsocket]
  val name = "SPCWebSocketClosed"
}

final case class WebSocketError(name: String = WebSocketError.name) extends ControlMessage {
  override def toXmlString: String = name
}

case object WebSocketError {
  implicit val format: OFormat[WebSocketError] = Json.format[WebSocketError]
  val name = "SPCWebSocketError"
}

