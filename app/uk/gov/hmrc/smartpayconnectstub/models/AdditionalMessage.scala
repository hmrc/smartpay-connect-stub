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

sealed trait AdditionalMessage extends F2FMessage

final case class UnexpectedMessage(f2fMessage: F2FMessage, name: String = UnexpectedMessage.name) extends AdditionalMessage {
  override def toXmlString: String = f2fMessage.toXmlString
}

object UnexpectedMessage {
  implicit val format: OFormat[UnexpectedMessage] = Json.format[UnexpectedMessage]
  val name = "UnexpectedMessage"
}

final case class UnknownMessage(message: String, name: String = UnknownMessage.name) extends AdditionalMessage {
  override def toXmlString: String = message
}

object UnknownMessage {
  implicit val format: OFormat[UnknownMessage] = Json.format[UnknownMessage]
  val name = "UnknownMessage"
}

final case class FailedAtCreationMessage(messageName: String, name: String = FailedAtCreationMessage.name) extends AdditionalMessage {
  override def toXmlString: String = messageName
}

object FailedAtCreationMessage {
  implicit val format: OFormat[FailedAtCreationMessage] = Json.format[FailedAtCreationMessage]
  val name = "FailedAtCreationMessage"
}

final case class UnexpectedLastMessage(f2fMessage: Option[F2FMessage], name: String = UnexpectedLastMessage.name) extends AdditionalMessage {
  override def toXmlString: String = f2fMessage.map(_.toXmlString).toString
}

object UnexpectedLastMessage {
  implicit val format: OFormat[UnexpectedLastMessage] = Json.format[UnexpectedLastMessage]
  val name = "UnexpectedLastMessage"
}

final case class MessageCreationFailed(messageName: String, name: String = MessageCreationFailed.name) extends AdditionalMessage {
  override def toXmlString: String = messageName.toString
}

object MessageCreationFailed {
  implicit val format: OFormat[MessageCreationFailed] = Json.format[MessageCreationFailed]
  val name = "MessageCreationFailed"
}
