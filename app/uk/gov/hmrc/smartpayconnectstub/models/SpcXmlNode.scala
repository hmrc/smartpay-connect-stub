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


import play.api.libs.json._

import scala.xml.{Node, Null, UnprefixedAttribute}

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
      case PdTransNode.name       => Json.fromJson[PdTransNode](json)
      case CardNode.name        => Json.fromJson[CardNode](json)
      case PtrCardNode.name        => Json.fromJson[PtrCardNode](json)
      case ReceiptNode.name     => Json.fromJson[ReceiptNode](json)
      case ErrorsNode.name      => Json.fromJson[ErrorsNode](json)
      case ErrorNode.name       => Json.fromJson[ErrorNode](json)
      case HeaderNode.name      => Json.fromJson[HeaderNode](json)
      case TransactionNode.name => Json.fromJson[TransactionNode](json)
      case PtrTransactionNode.name   =>  Json.fromJson[PtrTransactionNode](json)
      case _                    => JsError(s"Unknown name")
    }

    def writes(errorMessage: SpcXmlNode): JsValue = {
      errorMessage match {
        case b: MessageNode     => Json.toJson(b)
        case b: InteractionNode => Json.toJson(b)
        case b: AmountNode      => Json.toJson(b)
        case b: PdTransNode       => Json.toJson(b)
        case b: CardNode        => Json.toJson(b)
        case b: PtrCardNode        => Json.toJson(b)
        case b: ReceiptNode     => Json.toJson(b)
        case b: ErrorsNode      => Json.toJson(b)
        case b: ErrorNode       => Json.toJson(b)
        case b: HeaderNode      => Json.toJson(b)
        case b: TransactionNode => Json.toJson(b)
        case b: PtrTransactionNode => Json.toJson(b)

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

final case class AmountNode(totalAmount: AmountInPence, currency:Currency, country:Country, finalAmountO: Option[AmountInPence], name: String = AmountNode.name) extends SpcXmlNode {
  def toXml: Node = {
    val totalAmountNode =
      <AMOUNT currency={ currency.value } country={ country.value }>
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
    val currency = Currency((node \\ "AMOUNT" \ "@currency").text)
    val country = Country((node \\ "AMOUNT" \ "@country").text)
    val finalAmountO = (node \\ "AMOUNT" \ "FINAL").headOption.map(x => AmountInPence(x.text))
    AmountNode(totalAmount, currency, country, finalAmountO)
  }
  val name = "AmountNode"
}


final case class TransactionNode(amountNode: AmountNode,
                                 transactionActionO: Option[TransactionAction],
                                 transactionTypeO: Option[TransactionType],
                                 transactionSourceO: Option[TransactionSource],
                                 transactionCustomerO: Option[TransactionCustomer],
                                 name: String = TransactionNode.name) extends SpcXmlNode {
  def toXml: Node = {
    val transaction =
//    type="purchase" action="auth_n_settle" source={transactionSource} customer="present"
      <TRANSACTION>
        {amountNode.toXml}
      </TRANSACTION>
    val transactionWithAction = transactionActionO.fold(transaction)(transactionAction => transaction % new UnprefixedAttribute("action",transactionAction.toString,Null))
    val transactionWithType = transactionTypeO.fold(transactionWithAction)(transactionType => transactionWithAction % new UnprefixedAttribute("type",transactionType.toString,Null))
    val transactionWithSource = transactionSourceO.fold(transactionWithType)(transactionSource => transactionWithType % new UnprefixedAttribute("source",transactionSource.toString,Null))
    val transactionWithCustomer = transactionCustomerO.fold(transactionWithSource)(transactionCustomer => transactionWithSource % new UnprefixedAttribute("customer",transactionCustomer.toString,Null))
    transactionWithCustomer
  }
}

object TransactionNode {
  implicit val format: OFormat[TransactionNode] = Json.format[TransactionNode]
  def fromXml(node: Node): TransactionNode = {
    val transactionTypeO = (node \\ "TRANSACTION" \ "@type").headOption.map(x=> TransactionType(x.text))
    val transactionActionO = (node \\ "TRANSACTION" \ "@action").headOption.map(x=> TransactionAction(x.text))
    val transactionSourceO = (node \\ "TRANSACTION" \ "@source").headOption.map(x=> TransactionSource(x.text))
    val transactionCustomerO = (node \\ "TRANSACTION" \ "@customer").headOption.map(x=> TransactionCustomer(x.text))
    val amountNode = AmountNode.fromXml(node)
    TransactionNode(amountNode, transactionActionO, transactionTypeO,transactionSourceO, transactionCustomerO)
  }
  val name = "TransactionNode"
}

//TODO - provide current date time in message
final case class PtrTransactionNode(amountNode: AmountNode,
                                 transactionActionO: Option[TransactionAction],
                                 transactionTypeO: Option[TransactionType],
                                 transactionSourceO: Option[TransactionSource],
                                 transactionCustomerO: Option[TransactionCustomer],
                                  transactionReferenceO: Option[TransactionReference],
                                 name: String = PtrTransactionNode.name) extends SpcXmlNode {
  def toXml: Node = {
    val transaction =
    //      <TRANSACTION action="auth_n_settle" type="purchase" source="icc" customer="present" reference="8c1d4648-a57a-4dbd-a272-d4451d70474b" date="2022-04-04" time="17:36:03">
      <TRANSACTION date="2022-04-04" time="17:36:03">
        <SCHEME_REF>01000000003755712AB</SCHEME_REF>
        <AUTH_CODE>D12345</AUTH_CODE>
        <CARDHOLDER_RESULT verification="signature">1E0300</CARDHOLDER_RESULT>
        <AUTH_REQ_CRYPTO>906AC972932D351D</AUTH_REQ_CRYPTO>
        <AUTH_RESP_CODE>00</AUTH_RESP_CODE>
        <STATUS_INFO>6C00</STATUS_INFO>
        <CRYPTO_INFO_DATA>40</CRYPTO_INFO_DATA>
        <TERMINAL_RESULT>8080008020</TERMINAL_RESULT>
        <UNPREDICTABLE_NUM>074D5AED</UNPREDICTABLE_NUM>
        <CRYPTO_TRANSTYPE>00</CRYPTO_TRANSTYPE>
        { amountNode }
      </TRANSACTION>
    val transactionWithAction = transactionActionO.fold(transaction)(transactionAction => transaction % new UnprefixedAttribute("action",transactionAction.toString,Null))
    val transactionWithType = transactionTypeO.fold(transactionWithAction)(transactionType => transactionWithAction % new UnprefixedAttribute("type",transactionType.toString,Null))
    val transactionWithSource = transactionSourceO.fold(transactionWithType)(transactionSource => transactionWithType % new UnprefixedAttribute("source",transactionSource.toString,Null))
    val transactionWithCustomer = transactionCustomerO.fold(transactionWithSource)(transactionCustomer => transactionWithSource % new UnprefixedAttribute("customer",transactionCustomer.toString,Null))
    val transactionWithReference = transactionReferenceO.fold(transactionWithCustomer)(transactionReference => transactionWithCustomer % new UnprefixedAttribute("reference",transactionReference.value,Null))
    transactionWithReference
  }
}

object PtrTransactionNode {
  implicit val format: OFormat[PtrTransactionNode] = Json.format[PtrTransactionNode]
  def fromXml(node: Node): PtrTransactionNode = {
    val transactionTypeO = (node \\ "TRANSACTION" \ "@type").headOption.map(x=> TransactionType(x.text))
    val transactionActionO = (node \\ "TRANSACTION" \ "@action").headOption.map(x=> TransactionAction(x.text))
    val transactionSourceO = (node \\ "TRANSACTION" \ "@source").headOption.map(x=> TransactionSource(x.text))
    val transactionCustomerO = (node \\ "TRANSACTION" \ "@customer").headOption.map(x=> TransactionCustomer(x.text))
    val transactionReferenceO = (node \\ "TRANSACTION" \ "@reference").headOption.map(x=> TransactionReference(x.text))
    val amountNode = AmountNode.fromXml(node)
    PtrTransactionNode(amountNode, transactionActionO, transactionTypeO,transactionSourceO, transactionCustomerO, transactionReferenceO)
  }
  val name = "PtrTransactionNode"
}

final case class PdTransNode(decision: TransactionDecision, name: String = PdTransNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <TRANS name="posDecision">
      <DECISION type={ decision.decisionType }>{ decision.decisionDesc }</DECISION>
    </TRANS>
  }
}

object PdTransNode {
  implicit val format: OFormat[PdTransNode] = Json.format[PdTransNode]
  def fromXml(node: Node): PdTransNode = {
    val transactionDecision = TransactionDecision((node \\ "TRANS" \ "DECISION" \ "@type").text)
    PdTransNode(transactionDecision)
  }
  val name = "PdTransNode"
}

final case class CardNode(
                           currency: Currency,
                           country: Country,
                           endDate: String,
                           startDate: String,
                           pan: String,
                           cardType: CardType,
                           name: String = CardNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <CARD range="0" currency={ currency.value } country={ country.value }>
      <PAN end={ endDate } start={ startDate } seqNum="01">{ pan }</PAN>
      <APPLICATION id="A0000000031010">{ cardType.value }</APPLICATION>
      <TOKENS>
        <TOKEN origin="central">DB89CDDF-4A25-4C46-E053-11221FACA840</TOKEN>
      </TOKENS>
    </CARD>
  }
}

object CardNode {
  implicit val format: OFormat[CardNode] = Json.format[CardNode]
  def fromXml(node: Node): CardNode = {
    val currency = Currency((node \\ "CARD" \ "@currency").text)
    val country = Country((node \\ "CARD" \ "@country").text)
    val endDate = (node \\ "CARD" \ "PAN" \ "@end").text
    val startDate = (node \\ "CARD" \ "PAN" \ "@start").text
    val pan = (node \\ "CARD" \ "PAN").text
    val cardType = CardType((node \\ "CARD" \ "APPLICATION").text)
    CardNode(currency, country, endDate, startDate, pan, cardType)
  }
  val name = "CardNode"
}


final case class PtrCardNode(
                           currency: Currency,
                           country: Country,
                           endDate: String,
                           startDate: String,
                           pan: String,
                           cardType: CardType,
                           name: String = PtrCardNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <CARD range="0" currency={ currency.value } country={ country.value }>
      <PAN end={ endDate } start={ startDate } seqNum="01">{ pan }</PAN>
      <APPLICATION id="A0000000031010" version="0096">
        { cardType.value }
        <INTERCHANGE_PROFILE>1800</INTERCHANGE_PROFILE>
        <TRANSACTION_COUNTER>0001</TRANSACTION_COUNTER>
        <USAGE_CONTROL>FF80</USAGE_CONTROL>
        <ACTION_CODES>
          <DENIAL>0000000000</DENIAL>
          <ONLINE>0000000000</ONLINE>
          <DEFAULT>F040008800</DEFAULT>
        </ACTION_CODES>
        <DISCRETIONARY_DATA>
          <ISSUER_SUPPLIEDDATA>060112036000100F00564953414C335445535443415345</ISSUER_SUPPLIEDDATA>
        </DISCRETIONARY_DATA>
      </APPLICATION>
      <TOKENS>
        <TOKEN origin="central">DB89CDDF-4A25-4C46-E053-11221FACA840</TOKEN>
      </TOKENS>
    </CARD>
  }
}

object PtrCardNode {
  implicit val format: OFormat[PtrCardNode] = Json.format[PtrCardNode]
  def fromXml(node: Node): PtrCardNode = {
    val currency = Currency((node \\ "CARD" \ "@currency").text)
    val country = Country((node \\ "CARD" \ "@country").text)
    val endDate = (node \\ "CARD" \ "PAN" \ "@end").text
    val startDate = (node \\ "CARD" \ "PAN" \ "@start").text
    val pan = (node \\ "CARD" \ "PAN").text
    val cardType = CardType((node \\ "CARD" \ "APPLICATION").text)
    PtrCardNode(currency, country, endDate, startDate, pan, cardType)
  }
  val name = "PtrCardNode"
}

final case class ReceiptNode(receiptType: ReceiptType, receiptPrint: String, name: String = ReceiptNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <RECEIPT type={ receiptType.receiptType } format="plain">{ receiptPrint }</RECEIPT>
  }
}

object ReceiptNode {
  implicit val format: OFormat[ReceiptNode] = Json.format[ReceiptNode]
  def fromXml(node: Node): ReceiptNode = {
    val receiptType = ReceiptType((node \\ "RECEIPT" \ "@type").headOption.map(_.text).getOrElse("")) //TODO 2 receipt
    val receiptPrint = (node \\ "RECEIPT").headOption.map(_.text).getOrElse("")
    ReceiptNode(receiptType, receiptPrint)
  }
  val name = "ReceiptNode"
}

//TODO - DO parse all errors
final case class ErrorsNode(errorNode: Seq[ErrorNode], name: String = ErrorsNode.name) extends SpcXmlNode {
  def toXml: Node = {
    <ERRORS>
      {errorNode.foreach(_.toXml)}
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
