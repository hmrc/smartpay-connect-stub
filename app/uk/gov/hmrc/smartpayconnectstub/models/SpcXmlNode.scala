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


import play.api.libs.json.{JsError, JsResult, JsValue}
import play.api.libs.json.{Format, Json, OFormat}

import scala.xml.Node

/**
 * SCP - Smart Pay Connect - XML nodes that are used to build messages
 */
sealed trait SpcXmlNode {
  val name: String
}

object SpcXmlNode {
  implicit val errorMessageFormat: Format[SpcXmlNode] = new Format[SpcXmlNode] {
    def reads(json: JsValue): JsResult[SpcXmlNode] = (json \ "name").as[String] match {
      case MessageNode.name     => Json.fromJson[MessageNode](json)
      case InteractionNode.name => Json.fromJson[InteractionNode](json)
      case AmountNode.name      => Json.fromJson[AmountNode](json)
      case TransNode.name       => Json.fromJson[TransNode](json)
      case CardNode.name        => Json.fromJson[CardNode](json)
      case ReceiptNode.name     => Json.fromJson[ReceiptNode](json)
      case ErrorsNode.name      => Json.fromJson[ErrorsNode](json)
      case ErrorNode.name       => Json.fromJson[ErrorNode](json)
      case HeaderNode.name      => Json.fromJson[HeaderNode](json)
      case _                    => JsError(s"Unknown name")
    }

    def writes(errorMessage: SpcXmlNode): JsValue = {
      errorMessage match {
        case b: MessageNode     => Json.toJson(b)
        case b: InteractionNode => Json.toJson(b)
        case b: AmountNode      => Json.toJson(b)
        case b: TransNode       => Json.toJson(b)
        case b: CardNode        => Json.toJson(b)
        case b: ReceiptNode     => Json.toJson(b)
        case b: ErrorsNode      => Json.toJson(b)
        case b: ErrorNode       => Json.toJson(b)
        case b: HeaderNode      => Json.toJson(b)

      }
    }
  }
}

final case class MessageNode(transNum: TransactionId, deviceId: DeviceId, sourceId: SourceId, name: String = MessageNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <MESSAGE>
      <TRANS_NUM>{ transNum.value }</TRANS_NUM>
      <DEVICE_ID>{ deviceId.value }</DEVICE_ID>
      <SOURCE_ID>{ sourceId.value }</SOURCE_ID>
    </MESSAGE>
  }
}

object MessageNode {
  implicit val format: OFormat[MessageNode] = Json.format[MessageNode]
  def fromXml(node: Node): MessageNode = {
    val transNr = TransactionId((node \\ "MESSAGE" \ "TRANS_NUM").text)
    val sourceId = SourceId((node \\ "MESSAGE" \ "SOURCE_ID").text)
    val deviceId = DeviceId((node \\ "MESSAGE" \ "DEVICE_ID").text)
    MessageNode(transNr, deviceId, sourceId)
  }
  val name = "MessageNode"
}

final case class InteractionNode(category: InteractionCategory, event: InteractionEvent, prompt: InteractionPrompt, name: String = InteractionNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <INTERACTION name="posDisplayMessage">
      <STATUS category={ category.toString } event={ event.toString }/>
      <PROMPT>{ prompt.toString }</PROMPT>
    </INTERACTION>
  }
}

object InteractionNode {
  implicit val format: OFormat[InteractionNode] = Json.format[InteractionNode]
  def fromXml(node: Node): InteractionNode = {
    val category = InteractionCategory((node \\ "INTERACTION" \ "STATUS" \ "@category").text)
    val event = InteractionEvent((node \\ "INTERACTION" \ "STATUS" \ "@event").text)
    val prompt = InteractionPrompt((node \\ "INTERACTION" \ "PROMPT").text)
    InteractionNode(category, event, prompt)
  }
  val name = "InteractionNode"
}

final case class AmountNode(totalAmount: AmountInPence, finalAmountO: Option[AmountInPence], name: String = AmountNode.name) extends SpcXmlNode {
  def toXml: Node = {
    val totalAmountNode =
      <AMOUNT currency="826" country="826">
        <TOTAL>{ totalAmount.value }</TOTAL>
      </AMOUNT>

    finalAmountO.map{ finalAmount =>
      SpcXmlHelper.addNode(totalAmountNode, { <FINAL>{ finalAmount.value }</FINAL> })
    }.getOrElse(totalAmountNode)
  }
}

object AmountNode {
  implicit val format: OFormat[AmountNode] = Json.format[AmountNode]
  def fromXml(node: Node): AmountNode = {
    val totalAmount = AmountInPence.fromScpAmount((node \\ "AMOUNT" \ "TOTAL").text)
    val finalAmountO = (node \\ "AMOUNT" \ "FINAL").headOption.map(x => AmountInPence(x.text))
    AmountNode(totalAmount, finalAmountO)
  }
  val name = "AmountNode"
}

final case class TransNode(transName: String, decisionO: Option[TransactionDecision], name: String = TransNode.name) extends SpcXmlNode {
  def toXml: Node = {
    val transNode = <TRANS name={ transName }/>

    decisionO.map{ decision =>
      SpcXmlHelper.addNode(<DECISION type={ decision.decisionType }>{ decision.decisionDesc }</DECISION>, transNode)
    }.getOrElse(transNode)
  }
}

object TransNode {
  implicit val format: OFormat[TransNode] = Json.format[TransNode]
  def fromXml(node: Node): TransNode = {
    val transName = (node \\ "TRANS" \ "@name").text
    val transactionDecision = (node \\ "TRANS" \ "DECISION" \ "@type").headOption.map(x => TransactionDecision(x.text))
    TransNode(transName, transactionDecision)
  }
  val name = "TransNode"
}

final case class CardNode(end: String, pan: String, cardType: String, name: String = CardNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <CARD>
      <PAN end={ end } seqNum="01">{ pan }</PAN>
      <APPLICATION id="A0000000031010">{ cardType }</APPLICATION>
    </CARD>
  }
}

object CardNode {
  implicit val format: OFormat[CardNode] = Json.format[CardNode]
  def fromXml(node: Node): CardNode = {
    val end = (node \\ "CARD" \ "PAN" \ "@end").text
    val pan = (node \\ "CARD" \ "PAN").text
    val cardType = (node \\ "CARD" \ "APPLICATION").text
    CardNode(end, pan, cardType)
  }
  val name = "CardNode"
}

final case class ReceiptNode(receiptType: ReceiptType, receiptPrint: String, name: String = ReceiptNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <RECEIPT type={ receiptType.receiptType } format="plain">{ receiptPrint }</RECEIPT>
  }
}

object ReceiptNode {
  implicit val format: OFormat[ReceiptNode] = Json.format[ReceiptNode]
  def fromXml(node: Node): ReceiptNode = {
    val receiptType = ReceiptType((node \\ "RECEIPT" \ "@type").text)
    val receiptPrint = (node \\ "RECEIPT").text
    ReceiptNode(receiptType, receiptPrint)
  }
  val name = "ReceiptNode"
}

//TODO - DO parse all errors
final case class ErrorsNode(errorNode: Seq[ErrorNode], name: String = ErrorsNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <ERRORS>
      { errorNode.foreach(_.toXml) }
    </ERRORS>
  }
}

object ErrorsNode {
  implicit val format: OFormat[ErrorsNode] = Json.format[ErrorsNode]
  def fromXml(node: Node): ErrorsNode = {
    val errors = (node \\ "TRANS" \ "ERRORS").headOption.map(_.map(ErrorNode.fromXml))
    ErrorsNode(errors.getOrElse(Seq.empty[ErrorNode]))
  }
  val name = "ErrorsNode"
}

final case class ErrorNode(code: String, description: String, name: String = ErrorNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <ERROR code={ code }>{ description }</ERROR>
  }
}

object ErrorNode {
  implicit val format: OFormat[ErrorNode] = Json.format[ErrorNode]
  def fromXml(node: Node): ErrorNode = {
    val code = (node \\ "ERROR" \ "@code").text
    val description = (node \\ "ERROR").text
    ErrorNode(code, description)
  }
  val name = "ErrorNode"
}

final case class HeaderNode(name: String = HeaderNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <HEADER>
      <BUILD>
        <VERSION>1.34.0</VERSION>
      </BUILD>
    </HEADER>
  }
}

object HeaderNode {
  implicit val format: OFormat[HeaderNode] = Json.format[HeaderNode]
  def fromXml(node: Node): HeaderNode = {
    HeaderNode()
  }
  val name = "HeaderNode"
}
