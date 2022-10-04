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

package models

import models.SpcXmlHelper._
import play.api.libs.json._
import utils.RandomDataGenerator

import scala.xml.{Node, PCData}

/**
 * SCP - Smart Pay Connect - XML nodes that are used to build messages
 */
sealed trait SpcXmlNode {
  def toXml: Node
}


final case class MessageNode(transNum: TransactionId, deviceId: DeviceId, sourceId: SourceId) extends SpcXmlNode {
  def toXml: Node = {
    <MESSAGE>
      <TRANS_NUM>{ transNum.value }</TRANS_NUM>
      <DEVICE_ID>{ deviceId.value }</DEVICE_ID>
      <SOURCE_ID>{ sourceId.value }</SOURCE_ID>
    </MESSAGE>
  }
}

object MessageNode {
  def fromXml(node: Node): MessageNode = {
    val transNr = TransactionId((node \\ "MESSAGE" \ "TRANS_NUM").text)
    val sourceId = SourceId((node \\ "MESSAGE" \ "SOURCE_ID").text)
    val deviceId = DeviceId((node \\ "MESSAGE" \ "DEVICE_ID").text)
    MessageNode(transNr, deviceId, sourceId)
  }
}

final case class InteractionNode(category: InteractionCategory, event: InteractionEvent, prompt: InteractionPrompt) extends SpcXmlNode {
  def toXml: Node = {
    <INTERACTION name="posDisplayMessage">
      <STATUS category={ category.toString } event={ event.toString }/>
      <PROMPT>{ prompt.toString }</PROMPT>
    </INTERACTION>
  }
}


final case class AmountNode(totalAmount: AmountInPence, currency:Currency, country:Country, finalAmountO: Option[AmountInPence]) extends SpcXmlNode {
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
  def fromXml(node: Node): AmountNode = {
    val totalAmount = AmountInPence.fromScpAmount((node \\ "AMOUNT" \ "TOTAL").text)
    val currency = Currency((node \\ "AMOUNT" \ "@currency").text)
    val country = Country((node \\ "AMOUNT" \ "@country").text)
    val finalAmountO = (node \\ "AMOUNT" \ "FINAL").headOption.map(x => AmountInPence(x.text))
    AmountNode(totalAmount, currency, country, finalAmountO)
  }
}

final case class TransactionNode(amountNode: AmountNode, transactionSource: TransactionSource = TransactionSources.Icc) extends SpcXmlNode {
  def toXml: Node = {
      <TRANSACTION type={TransactionTypes.Purchase.toString} action={TransactionActions.AuthorizeAndSettle.toString} source={transactionSource.toString} customer={TransactionCustomers.Present.toString} >
        {amountNode.toXml}
      </TRANSACTION>
  }
}

object TransactionNode {
  def fromXml(node: Node): TransactionNode = {
    val transactionSourceO = (node \\ "TRANSACTION" \ "@source").headOption.map(x=> TransactionSource(x.text)).get
    val amountNode = AmountNode.fromXml(node)
    TransactionNode(amountNode, transactionSourceO)
  }
}

//TODO - provide current date time in message
final case class PtrTransactionNode(amountNode: AmountNode,
                                    transactionSource: TransactionSource = TransactionSources.Icc,
                                    verification: CardVerificationMethod,
                                    transactionDate:String,
                                    transactionTime: String) extends SpcXmlNode {
  def toXml: Node = {
      <TRANSACTION action={TransactionActions.AuthorizeAndSettle.toString}  type={TransactionTypes.Purchase.toString} source={transactionSource.toString} customer={TransactionCustomers.Present.toString} reference={RandomDataGenerator.generateTransactionReference.value} date={ transactionDate } time= {transactionTime}>
        <SCHEME_REF>XXXXXXXXXXXXXXXX</SCHEME_REF>
        <AUTH_CODE>{ StubUtil.AUTH_CODE }</AUTH_CODE>
        <CARDHOLDER_RESULT verification={ verification.toString }>XXXXXX</CARDHOLDER_RESULT>
        <AUTH_REQ_CRYPTO>XXXXXXXXXXXXX</AUTH_REQ_CRYPTO>
        <AUTH_RESP_CODE>XX</AUTH_RESP_CODE>
        <STATUS_INFO>XXXX</STATUS_INFO>
        <CRYPTO_INFO_DATA>XX</CRYPTO_INFO_DATA>
        <TERMINAL_RESULT>XXXXXXXXX</TERMINAL_RESULT>
        <UNPREDICTABLE_NUM>XXXXXXX</UNPREDICTABLE_NUM>
        <CRYPTO_TRANSTYPE>XX</CRYPTO_TRANSTYPE>
        { amountNode.toXml }
      </TRANSACTION>
  }
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

final case class UpeCardNode(paymentCard: PaymentCard) extends SpcXmlNode {
  def toXml: Node = {
      <CARD range="X" currency={paymentCard.currency.value} country={paymentCard.country.value}>
        <PAN end={paymentCard.receiptEndMasked} start={paymentCard.startDate} seqNum={paymentCard.seqNum}>{paymentCard.receiptPan}</PAN>
        <APPLICATION id={ StubUtil.APPLICATION_ID }>
          {paymentCard.cardSchema.value}
        </APPLICATION>
        <TOKENS>
          <TOKEN origin="xxxxxxx">XXXXXXXXXXXXXXXXXXXXXXXX</TOKEN>
        </TOKENS>
      </CARD>
    }
}

final case class PtrResponseCardNode(paymentCard: PaymentCard) extends SpcXmlNode {
  def toXml: Node = {
    <CARD range="X" currency={ paymentCard.currency.value } country={ paymentCard.country.value }>
      <PAN end={ paymentCard.endDate } start={ paymentCard.startDate } seqNum={paymentCard.seqNum}>{ paymentCard.pan }</PAN>
      <APPLICATION id={ StubUtil.APPLICATION_ID } version="XXXXX">
        {paymentCard.cardSchema.value}
        <INTERCHANGE_PROFILE>XXXX</INTERCHANGE_PROFILE>
        <TRANSACTION_COUNTER>XXXX</TRANSACTION_COUNTER>
        <USAGE_CONTROL>XXXX</USAGE_CONTROL>
        <ACTION_CODES>
          <DENIAL>XXXXXXXXXX</DENIAL>
          <ONLINE>XXXXXXXXXX</ONLINE>
          <DEFAULT>XXXXXXXXXX</DEFAULT>
        </ACTION_CODES>
        <DISCRETIONARY_DATA>
          <ISSUER_SUPPLIEDDATA>XXXXXXXXXXXXXXXXXXXXXXXX</ISSUER_SUPPLIEDDATA>
        </DISCRETIONARY_DATA>
      </APPLICATION>
      <TOKENS>
        <TOKEN origin="xxxxxxx">XXXXXXXXXXXXXXXXXXXXXXXX</TOKEN>
      </TOKENS>
    </CARD>
  }
}



trait ReceiptNode {

  val spcFlow:                SpcFlow
  val submittedData:          SubmittedData
  val totalAmount:            AmountInPence
  val finalAmount:            Option[AmountInPence]


  def receiptType:            ReceiptType = ReceiptTypes.CustomerReceipt
  def transactionDatetime:    Long = submittedData.transactionDateTime
  def duplicate:              Boolean = true

  def maybeTerminalId:        Option[String] = Some(StubUtil.TERMINAL_ID)
  def maybeAuthCode:          Option[String] = Some(StubUtil.AUTH_CODE)
  def maybeAvailableSpend:    Option[AmountInPence] = spcFlow.paymentCard.availableSpend
  def maybePanSequence:       Option[String] = Some(spcFlow.paymentCard.seqNum)
  def maybePanStartDate:      Option[String] = Some(spcFlow.paymentCard.receiptStart)

  val name:String = "ReceiptNode"

  def receiptToXml: Node = {
    <RECEIPT>
      <APPLICATION_ID>{ StubUtil.APPLICATION_ID }</APPLICATION_ID>
      <CARD_SCHEME>{ spcFlow.paymentCard.cardSchema.value }</CARD_SCHEME>
      <CURRENCY_CODE>{ submittedData.currency.toThreeLetterIcoCode }</CURRENCY_CODE>
      <CUSTOMER_PRESENCE>{ CustomerPresence.present.toString }</CUSTOMER_PRESENCE>
      <FINAL_AMOUNT>{ finalAmount.getOrElse(totalAmount).formatInDecimal }</FINAL_AMOUNT>
      <MERCHANT_NUMBER>{StubUtil.MERCHANT_NUMBER }</MERCHANT_NUMBER>
      <PAN_NUMBER>{ getPanNumber }</PAN_NUMBER>
      <PAN_EXPIRY>{getEndDate }</PAN_EXPIRY>
      <TOKEN>XXXXXXXXXXXXXXXXX</TOKEN>
      <TOTAL_AMOUNT>{ totalAmount.formatInDecimal }</TOTAL_AMOUNT>
      <TRANSACTION_DATA_SOURCE>{ TransactionSources.Icc }</TRANSACTION_DATA_SOURCE>
      <TRANSACTION_DATE>{ StubUtil.formatReceiptDate(transactionDatetime) }</TRANSACTION_DATE>
      <TRANSACTION_NUMBER>{ submittedData.transactionNumber.value }</TRANSACTION_NUMBER>
      <TRANSACTION_RESPONSE>{ getTransactionResponse }</TRANSACTION_RESPONSE>
      <TRANSACTION_TIME>{ StubUtil.formatReceiptTime(transactionDatetime) }</TRANSACTION_TIME>
      <TRANSACTION_TYPE>{ TransactionTypes.Purchase.toString }</TRANSACTION_TYPE>
      <VERIFICATION_METHOD>{ spcFlow.cardVerificationMethod.toString }</VERIFICATION_METHOD>
      <DUPLICATE>{ duplicate }</DUPLICATE>
    </RECEIPT>
      .maybeAddNode(maybeAuthCode.map(x=> { <AUTH_CODE>{ x }</AUTH_CODE> }))
      .maybeAddNode(maybeAvailableSpend.map(x=> { <AVAILABLE_SPEND>{ x }</AVAILABLE_SPEND> }))
      .maybeAddNode(maybePanSequence.map(x=> { <PAN_SEQUENCE>{ x }</PAN_SEQUENCE> }))
      .maybeAddNode(maybePanStartDate.map(x=> { <PAN_START>{ x }</PAN_START> }))
      .maybeAddNode(maybeTerminalId.map(x=> { <TERMINAL_ID>{ x }</TERMINAL_ID> }))

  }

  def toXml: Node = {
    <RECEIPT type={ receiptType.receiptType } format="xml">{ PCData(receiptToXml.toString()) }</RECEIPT>
  }

  def toXml(receiptType: ReceiptType): Node = {
    <RECEIPT type={ receiptType.receiptType } format="xml">{ PCData(receiptToXml.toString()) }</RECEIPT>
  }

  def getPanNumber: String = receiptType match{
    case _@ReceiptTypes.CustomerReceipt => spcFlow.paymentCard.receiptPan
    case _ => spcFlow.paymentCard.receiptPanMasked
  }

  def getEndDate: String = receiptType match{
    case _@ReceiptTypes.CustomerReceipt => spcFlow.paymentCard.receiptEnd
    case _ => spcFlow.paymentCard.receiptEndMasked
  }

  def getTransactionResponse: String = spcFlow.paymentResult match {
    case _@PaymentResults.OnlineResult => maybeAuthCode.getOrElse("Error TransactionResponse.Auth is missing")
    case _ => spcFlow.paymentResult.toString
  }

}

sealed trait ReceiptTypeName

object ReceiptTypeName {
  case object ReceiptType1Name extends ReceiptTypeName

  case object ReceiptType2Name extends ReceiptTypeName

  case object ReceiptType3Name extends ReceiptTypeName

  case object ReceiptType4Name extends ReceiptTypeName

  case object ReceiptType5Name extends ReceiptTypeName

  case object ReceiptType6Name extends ReceiptTypeName

  case object ReceiptType7Name extends ReceiptTypeName

  case object ReceiptType8Name extends ReceiptTypeName

  case object ReceiptType9Name extends ReceiptTypeName
}

object ReceiptNode {
  import ReceiptTypeName._
  def createReceiptNode(submittedData: SubmittedData, spcFlow: SpcFlow, totalAmount: AmountInPence, finalAmount: Option[AmountInPence]):ReceiptNode = {
    spcFlow.receiptNodeName match {
      case ReceiptType1Name => ReceiptType1Node(spcFlow, submittedData, totalAmount, finalAmount)
      case ReceiptType2Name => ReceiptType2Node(spcFlow, submittedData, totalAmount, finalAmount)
      case ReceiptType3Name => ReceiptType3Node(spcFlow, submittedData, totalAmount, finalAmount)
      case ReceiptType4Name => ReceiptType4Node(spcFlow, submittedData, totalAmount, finalAmount)
      case ReceiptType5Name => ReceiptType5Node(spcFlow, submittedData, totalAmount, finalAmount)
      case ReceiptType6Name => ReceiptType6Node(spcFlow, submittedData, totalAmount, finalAmount)
      case ReceiptType7Name => ReceiptType7Node(spcFlow, submittedData, totalAmount, finalAmount)
      case ReceiptType8Name => ReceiptType8Node(spcFlow, submittedData, totalAmount, finalAmount)
      case ReceiptType9Name => ReceiptType9Node(spcFlow, submittedData, totalAmount, finalAmount)
    }
  }

}

final case class ReceiptType1Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                            ) extends ReceiptNode with SpcXmlNode {
  override val maybeAvailableSpend: Option[AmountInPence] = None
}

final case class ReceiptType2Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode {
  override val maybeAvailableSpend: Option[AmountInPence] = None
  override val maybePanSequence: Option[String] = None
}

final case class ReceiptType3Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode  {
  override val maybeAuthCode: Option[String] = None
  override val maybeAvailableSpend: Option[AmountInPence] = None
}

final case class ReceiptType4Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode  {
  override val maybeAuthCode: Option[String] = None
  override val maybeAvailableSpend: Option[AmountInPence] = None
  override val maybeTerminalId: Option[String] = None
}

final case class ReceiptType5Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode  {
  override val maybePanStartDate: Option[String] = None
}

final case class ReceiptType6Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode  {
  override val maybeAuthCode: Option[String] = None
  override val maybePanStartDate: Option[String] = None
  }

final case class ReceiptType7Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode  {
  override val maybeAuthCode: Option[String] = None
  override val maybeAvailableSpend: Option[AmountInPence] = None
  override val maybePanStartDate: Option[String] = None

}

final case class ReceiptType8Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode {
  override val maybePanSequence: Option[String] = None
  override val maybePanStartDate: Option[String] = None
}

final case class ReceiptType9Node(
                                   spcFlow: SpcFlow,
                                   submittedData: SubmittedData,
                                   totalAmount: AmountInPence,
                                   finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode {
  override val maybePanSequence: Option[String] = None
  override val maybeTerminalId: Option[String] = None
  override val maybeAuthCode: Option[String] = None
  override val maybeAvailableSpend: Option[AmountInPence] = None
}

final case class ReceiptMerchantNode(
                                      spcFlow: SpcFlow,
                                      submittedData: SubmittedData,
                                      totalAmount: AmountInPence,
                                      finalAmount: Option[AmountInPence]
                                 ) extends SpcXmlNode with ReceiptNode  {

  override val receiptType: ReceiptType = ReceiptTypes.MerchantReceipt
  override val maybeAuthCode: Option[String] = None
  override val maybeAvailableSpend: Option[AmountInPence] = None
  override val maybeTerminalId: Option[String] = None
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
        <VERSION>{StubUtil.VERSION}</VERSION>
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


