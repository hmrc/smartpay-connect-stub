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

import play.api.libs.json.{Format, JsError, JsResult, JsSuccess, JsValue, Json, OFormat}
import uk.gov.hmrc.smartpayconnectstub.models.Results.{FailureResult, SuccessResult}

import scala.xml.Node

/**
 * SCP - Smart Pay Connect - XML messages
 */
trait F2FMessage {
  val name: String
  def toXmlString: String
}

object F2FMessage {
  implicit val f2fMessageFormat: Format[F2FMessage] = new Format[F2FMessage] {
    def reads(json: JsValue): JsResult[F2FMessage] = (json \ "name").as[String] match {
      case PedLogOn.name                      => Json.fromJson[PedLogOn](json)
      case PedLogOnResponse.name              => Json.fromJson[PedLogOnResponse](json)
      case SubmitPayment.name                 => Json.fromJson[SubmitPayment](json)
      case SubmitPaymentResponse.name         => Json.fromJson[SubmitPaymentResponse](json)
      case ProcessTransaction.name            => Json.fromJson[ProcessTransaction](json)
      case PosDecisionMessage.name            => Json.fromJson[PosDecisionMessage](json)
      case PosDisplayMessage.name             => Json.fromJson[PosDisplayMessage](json)
      case UpdatePaymentEnhanced.name         => Json.fromJson[UpdatePaymentEnhanced](json)
      case UpdatePaymentEnhancedResponse.name => Json.fromJson[UpdatePaymentEnhancedResponse](json)
      case ProcessTransactionResponse.name    => Json.fromJson[ProcessTransactionResponse](json)
      case PosPrintReceipt.name               => Json.fromJson[PosPrintReceipt](json)
      case PosPrintReceiptResponse.name       => Json.fromJson[PosPrintReceiptResponse](json)
      case Finalise.name                      => Json.fromJson[Finalise](json)
      case CompleteTransaction.name           => Json.fromJson[CompleteTransaction](json)
      case FinaliseResponse.name              => Json.fromJson[FinaliseResponse](json)
      case PedLogOff.name                     => Json.fromJson[PedLogOff](json)
      case PedLogOffResponse.name             => Json.fromJson[PedLogOffResponse](json)
      case CancelTransaction.name             => Json.fromJson[CancelTransaction](json)
      case CloseWebsocket.name                => Json.fromJson[CloseWebsocket](json)
      case WebSocketError.name                => Json.fromJson[WebSocketError](json)
      case UnknownMessage.name                => Json.fromJson[UnknownMessage](json)
      case UnexpectedMessage.name             => Json.fromJson[UnexpectedMessage](json)
      case ErrorMessage.name                  => Json.fromJson[ErrorMessage](json)
      case UnexpectedLastMessage.name         => Json.fromJson[UnexpectedLastMessage](json)
      case MessageCreationFailed.name         => Json.fromJson[MessageCreationFailed](json)
      case _                                  => JsError(s"Unknown name")
    }

    def writes(f2fMessage: F2FMessage): JsValue = {
      f2fMessage match {
        case b: UnknownMessage                => Json.toJson(b)
        case b: UnexpectedMessage             => Json.toJson(b)
        case b: PedLogOn                      => Json.toJson(b)
        case b: PedLogOnResponse              => Json.toJson(b)
        case b: SubmitPayment                 => Json.toJson(b)
        case b: SubmitPaymentResponse         => Json.toJson(b)
        case b: ProcessTransaction            => Json.toJson(b)
        case b: PosDecisionMessage            => Json.toJson(b)
        case b: PosDisplayMessage             => Json.toJson(b)
        case b: UpdatePaymentEnhanced         => Json.toJson(b)
        case b: UpdatePaymentEnhancedResponse => Json.toJson(b)
        case b: ProcessTransactionResponse    => Json.toJson(b)
        case b: PosPrintReceipt               => Json.toJson(b)
        case b: PosPrintReceiptResponse       => Json.toJson(b)
        case b: Finalise                      => Json.toJson(b)
        case b: CompleteTransaction           => Json.toJson(b)
        case b: FinaliseResponse              => Json.toJson(b)
        case b: PedLogOff                     => Json.toJson(b)
        case b: PedLogOffResponse             => Json.toJson(b)
        case b: CancelTransaction             => Json.toJson(b)
        case b: CloseWebsocket                => Json.toJson(b)
        case b: WebSocketError                => Json.toJson(b)
        case b: ErrorMessage                  => Json.toJson(b)
        case b: UnexpectedLastMessage         => Json.toJson(b)
        case b: MessageCreationFailed         => Json.toJson(b)
      }
    }
  }
}

sealed trait SpcMessage extends F2FMessage {
  def toXml: Node
  def toXmlString: String = toXml.toString()
}

sealed trait SpcRequestMessage extends SpcMessage {

}

sealed trait SpcResponseMessage extends SpcMessage {
  def isValid: Boolean = true

}

object SpcResponseMessage {
  implicit val f2fResponseMessageFormat: Format[SpcResponseMessage] = new Format[SpcResponseMessage] {
    def reads(json: JsValue): JsResult[SpcResponseMessage] = F2FMessage.f2fMessageFormat.reads(json) match {
      case JsSuccess(f2FMessage, _) => f2FMessage match {
        case spcResponseMessage: SpcResponseMessage => JsSuccess(spcResponseMessage)
        case _                                      => JsError("Incorrect ScpResponseMessage type")
      }
      case _ => JsError("")
    }
    def writes(spcResponseMessage: SpcResponseMessage): JsValue = F2FMessage.f2fMessageFormat.writes(spcResponseMessage)
  }
}

final case class PedLogOn(messageNode: MessageNode, name: String = PedLogOn.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }<POI_MSG type="interaction">
      <INTERACTION name="pedLogOn"/>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PedLogOn {
  implicit val format: OFormat[PedLogOn] = Json.format[PedLogOn]
  def fromXml(node: Node): PedLogOn = {
    val messageNode = MessageNode.fromXml(node)
    PedLogOn(messageNode)
  }

  val name: String = "pedLogOn"
}

final case class PedLogOnResponse(headerNode: HeaderNode, messageNode: MessageNode, result: Result, errors: ErrorsNode, name: String = PedLogOnResponse.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }
      { messageNode.toXml }
      <POI_MSG type="interaction">
        <INTERACTION name="pedLogOnResponse">
          { errors.toXml }
          <RESULT>
            { result.toString }
          </RESULT>
        </INTERACTION>
      </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PedLogOnResponse {
  implicit val format: OFormat[PedLogOnResponse] = Json.format[PedLogOnResponse]
  def fromXml(node: Node): PedLogOnResponse = {
    val headerNode = HeaderNode.fromXml(node)
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "INTERACTION" \ "RESULT").text)
    val errorsNode = ErrorsNode.fromXml(node)
    PedLogOnResponse(headerNode, messageNode, result, errorsNode)
  }
  val name: String = "pedLogOnResponse"
}
//icc, keyed
final case class SubmitPayment(messageNode: MessageNode, amountNode: AmountNode, name: String = SubmitPayment.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }<POI_MSG type="submittal">
      <SUBMIT name="submitPayment">
        <TRANSACTION type="purchase" action="auth_n_settle" source="keyed" customer="present">
          { amountNode.toXml }
        </TRANSACTION>
      </SUBMIT>
    </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object SubmitPayment {
  implicit val format: OFormat[SubmitPayment] = Json.format[SubmitPayment]
  def fromXml(node: Node): SubmitPayment = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    SubmitPayment(messageNode, amountNode)
  }

  val name: String = "submitPayment"
}

final case class SubmitPaymentResponse(headerNode: HeaderNode, messageNode: MessageNode, result: Result, name: String = SubmitPaymentResponse.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="submittal">
      <SUBMIT name="submitPaymentResponse">
        <RESULT>
          { result.toString }
        </RESULT>
      </SUBMIT>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object SubmitPaymentResponse {
  implicit val format: OFormat[SubmitPaymentResponse] = Json.format[SubmitPaymentResponse]
  def fromXml(node: Node): SubmitPaymentResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "SUBMIT" \ "RESULT").text)
    val headerNode = HeaderNode.fromXml(node)
    SubmitPaymentResponse(headerNode, messageNode, result)
  }

  val name: String = "submitPaymentResponse"
}

final case class ProcessTransaction(messageNode: MessageNode, name: String = ProcessTransaction.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }<POI_MSG type="transactional">
      <TRANS name="processTransaction"/>
    </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object ProcessTransaction {
  implicit val format: OFormat[ProcessTransaction] = Json.format[ProcessTransaction]
  def fromXml(node: Node): ProcessTransaction = {
    val messageNode = MessageNode.fromXml(node)
    ProcessTransaction(messageNode)
  }

  val name: String = "processTransaction"
}

final case class PosDecisionMessage(headerNode: HeaderNode, messageNode: MessageNode, transNode: TransNode, name: String = PosDecisionMessage.name) extends SpcResponseMessage {
  override def isValid: Boolean = transNode.decisionO.nonEmpty

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="transactional">
      { transNode.toXml }
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PosDecisionMessage {
  implicit val format: OFormat[PosDecisionMessage] = Json.format[PosDecisionMessage]
  def fromXml(node: Node): PosDecisionMessage = {
    val messageNode = MessageNode.fromXml(node)
    val transNode = TransNode.fromXml(node)
    val headerNode = HeaderNode.fromXml(node)
    PosDecisionMessage(headerNode, messageNode, transNode)
  }

  val name: String = "posDecision"
}

final case class PosDisplayMessage(headerNode: HeaderNode, messageNode: MessageNode, interactionNode: InteractionNode, result: Result, errors: ErrorsNode, name: String = PosDisplayMessage.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="interaction">
      { interactionNode.toXml }
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PosDisplayMessage {
  implicit val format: OFormat[PosDisplayMessage] = Json.format[PosDisplayMessage]
  def fromXml(node: Node): PosDisplayMessage = {
    val messageNode = MessageNode.fromXml(node)
    val interactionNode = InteractionNode.fromXml(node)
    val headerNode = HeaderNode.fromXml(node)
    PosDisplayMessage(headerNode, messageNode, interactionNode, SuccessResult, ErrorsNode(Seq.empty[ErrorNode]))
  }

  val name: String = "posDisplayMessage"
}

final case class UpdatePaymentEnhanced(headerNode: HeaderNode, messageNode: MessageNode, amountNode: AmountNode, cardNode: CardNode, result: Result, errors: ErrorsNode, name: String = UpdatePaymentEnhanced.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="transactional">
      <TRANS name="updatePaymentEnhanced">
        <PAYMENT>
          <TRANSACTION action="auth_n_settle" customer="present" source="contactlessEMV" type="purchase">
            { amountNode.toXml }
          </TRANSACTION>{ cardNode.toXml }
        </PAYMENT>
      </TRANS>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object UpdatePaymentEnhanced {
  implicit val format: OFormat[UpdatePaymentEnhanced] = Json.format[UpdatePaymentEnhanced]
  def fromXml(node: Node): UpdatePaymentEnhanced = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    val cardNode = CardNode.fromXml(node)
    val headerNode = HeaderNode.fromXml(node)
    UpdatePaymentEnhanced(headerNode, messageNode, amountNode, cardNode, SuccessResult, ErrorsNode(Seq.empty[ErrorNode]))
  }

  val name: String = "updatePaymentEnhanced"
}

final case class UpdatePaymentEnhancedResponse(messageNode: MessageNode, amountNode: AmountNode, name: String = UpdatePaymentEnhancedResponse.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }<POI_MSG type="transactional">
      <TRANS name="updatePaymentEnhancedResponse">
        <PAYMENT>
          <TRANSACTION>
            { amountNode.toXml }
          </TRANSACTION>
        </PAYMENT>
      </TRANS>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object UpdatePaymentEnhancedResponse {
  implicit val format: OFormat[UpdatePaymentEnhancedResponse] = Json.format[UpdatePaymentEnhancedResponse]
  def fromXml(node: Node): UpdatePaymentEnhancedResponse = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    UpdatePaymentEnhancedResponse(messageNode, amountNode)
  }

  val name: String = "updatePaymentEnhancedResponse"
}

final case class ProcessTransactionResponse(headerNode: HeaderNode, messageNode: MessageNode, amountNode: AmountNode, result: Result, paymentResult: PaymentResult, errorsNode: ErrorsNode, name: String = ProcessTransactionResponse.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def isPaymentSuccessful: Boolean = paymentResult match {
    case PaymentResults.OnlineResult => true
    case _                           => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }
      <POI_MSG type="transactional">
        <TRANS name="processTransactionResponse">
          { errorsNode.toXml }
          <RESULT>
            { result.toString }
          </RESULT>
          <INTERFACE>
            <TERMINAL serialNumber="200417333011101013619956">
              <TERMINAL_TYPE>22</TERMINAL_TYPE>
            </TERMINAL>
          </INTERFACE>
          <PAYMENT>
            <PAYMENT_RESULT>
              { paymentResult.toString }
            </PAYMENT_RESULT>
            <ACQUIRER id="V">VISA</ACQUIRER>
            <BANK id="B">Barclays-Dummy-51922203</BANK>
            <MERCHANT number="9876543"/>
            <HOST_RESP responseCode="00">
              <![CDATA[ NOT AUTHORISED ]]>
            </HOST_RESP>
            <TRANSACTION action="auth_n_settle" customer="present" date="2021-11-24" reference="7942dec3-5750-498c-8df0-3dd9d9487362" source="contactlessEMV" time="11:52:20" type="purchase">
              <AUTH_CODE>D12345</AUTH_CODE>
              <CARDHOLDER_RESULT verification="not_performed">000000</CARDHOLDER_RESULT>
              <DESCRIPTION>SCpdq Test Transaction</DESCRIPTION>
              <AUTH_REQ_CRYPTO>EDA8B89CECB011C3</AUTH_REQ_CRYPTO>
              <AUTH_RESP_CODE>00</AUTH_RESP_CODE>
              <CRYPTO_INFO_DATA>80</CRYPTO_INFO_DATA>
              <TERMINAL_RESULT>0000000000</TERMINAL_RESULT>
              <UNPREDICTABLE_NUM>3FECD91E</UNPREDICTABLE_NUM>
              <CRYPTO_TRANSTYPE>00</CRYPTO_TRANSTYPE>{ amountNode.toXml }
            </TRANSACTION>
            <CARD range="0">
              <PAN end="2024-12-31" seqNum="01">417666******0019</PAN>
              <APPLICATION id="A0000000031010" version="0096">
                VISA CREDIT
                <INTERCHANGE_PROFILE>2000</INTERCHANGE_PROFILE>
                <TRANSACTION_COUNTER>058E</TRANSACTION_COUNTER>
                <DISCRETIONARY_DATA>
                  <ISSUER_SUPPLIEDDATA>06011103A00000</ISSUER_SUPPLIEDDATA>
                </DISCRETIONARY_DATA>
              </APPLICATION>
              <TOKENS>
                <TOKEN origin="central">D1887DDC-2BF8-6A47-E053-11221FAC1C89</TOKEN>
              </TOKENS>
            </CARD>
          </PAYMENT>
          <RECEIPT format="plain" type="merchant">
            <![CDATA[* * DUPLICATE * *

AID: A0000000031010
VISA CREDIT
Card: ************0019
PAN Seq Nr: 01

CONTACTLESS
SALE
DECLINED
TOTAL: GBP10.00

Merchant: 9876543
TID: 23212075
Trans no: 58
Date: 24/11/2021 Time: 11:52:20

MERCHANT COPY]]>
          </RECEIPT>
          <RECEIPT format="plain" type="customer">
            <![CDATA[* * DUPLICATE * *

AID: A0000000031010
VISA CREDIT
Card: ************0019
PAN Seq Nr: 01

CONTACTLESS
SALE
DECLINED
TOTAL: GBP10.00

Merchant: **76543
TID: ****2075
Trans no: 58
Date: 24/11/2021 Time: 11:52:20

Please retain for your records

CUSTOMER COPY]]>
          </RECEIPT>
        </TRANS>
      </POI_MSG>
    </RLSOLVE_MSG>
  }


}

object ProcessTransactionResponse {
  implicit val format: OFormat[ProcessTransactionResponse] = Json.format[ProcessTransactionResponse]
  def fromXml(node: Node): ProcessTransactionResponse = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    val result = Result((node \\ "TRANS" \ "RESULT").text)
    val paymentResult = PaymentResult((node \\ "PAYMENT" \ "PAYMENT_RESULT").text)
    val errorsNode = ErrorsNode.fromXml(node)
    val headerNode = HeaderNode.fromXml(node)
    ProcessTransactionResponse(headerNode, messageNode, amountNode, result, paymentResult, errorsNode)
  }

  val name: String = "processTransactionResponse"
}

final case class PosPrintReceipt(headerNode: HeaderNode, messageNode: MessageNode, receiptNode: ReceiptNode, result: Result, errors: ErrorsNode, name: String = PosPrintReceipt.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="interaction">
      <INTERACTION name="posPrintReceipt">
        { receiptNode.toXml }
      </INTERACTION>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PosPrintReceipt {
  implicit val format: OFormat[PosPrintReceipt] = Json.format[PosPrintReceipt]
  def fromXml(node: Node): PosPrintReceipt = {
    val messageNode = MessageNode.fromXml(node)
    val receiptNode = ReceiptNode.fromXml(node)
    val headerNode = HeaderNode.fromXml(node)
    PosPrintReceipt(headerNode, messageNode, receiptNode, SuccessResult, ErrorsNode(Seq.empty[ErrorNode]))
  }

  val name: String = "posPrintReceipt"
}

final case class PosPrintReceiptResponse(messageNode: MessageNode, result: Result, name: String = PosPrintReceiptResponse.name) extends SpcRequestMessage {
  //  override def isValid: Boolean = result match {
  //    case SuccessResult => true
  //    case FailureResult => false
  //  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }<POI_MSG type="interaction">
      <INTERACTION name="posPrintReceiptResponse">
        <RESPONSE>
          { result.toString }
        </RESPONSE>
      </INTERACTION>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PosPrintReceiptResponse {
  implicit val format: OFormat[PosPrintReceiptResponse] = Json.format[PosPrintReceiptResponse]
  def fromXml(node: Node): PosPrintReceiptResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "INTERACTION" \ "RESPONSE").text)
    PosPrintReceiptResponse(messageNode, result)
  }

  val name: String = "posPrintReceiptResponse"
}

final case class Finalise(messageNode: MessageNode, name: String = Finalise.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }<POI_MSG type="transactional">
      <TRANS name="finalise"/>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object Finalise {
  implicit val format: OFormat[Finalise] = Json.format[Finalise]
  def fromXml(node: Node): Finalise = {
    val messageNode = MessageNode.fromXml(node)
    Finalise(messageNode)
  }

  val name: String = "finalise"
}

final case class CompleteTransaction(messageNode: MessageNode, name: String = CompleteTransaction.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }<POI_MSG type="transactional">
      <TRANS name="completeTransaction"/>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object CompleteTransaction {
  implicit val format: OFormat[CompleteTransaction] = Json.format[CompleteTransaction]
  def fromXml(node: Node): CompleteTransaction = {
    val messageNode = MessageNode.fromXml(node)
    CompleteTransaction(messageNode)
  }

  val name: String = "completeTransaction"
}

final case class FinaliseResponse(headerNode: HeaderNode, messageNode: MessageNode, result: Result, name: String = FinaliseResponse.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="transactional">
      <TRANS name="finaliseResponse">
        <RESULT>
          { result.toString }
        </RESULT>
      </TRANS>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object FinaliseResponse {
  implicit val format: OFormat[FinaliseResponse] = Json.format[FinaliseResponse]
  def fromXml(node: Node): FinaliseResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "TRANS" \ "RESULT").text)
    val headerNode = HeaderNode.fromXml(node)
    FinaliseResponse(headerNode, messageNode, result)
  }

  val name: String = "finaliseResponse"
}

final case class PedLogOff(messageNode: MessageNode, name: String = PedLogOff.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }<POI_MSG type="interaction">
      <INTERACTION name="pedLogOff"/>
    </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PedLogOff {
  implicit val format: OFormat[PedLogOff] = Json.format[PedLogOff]
  def fromXml(node: Node): PedLogOff = {
    val messageNode = MessageNode.fromXml(node)
    PedLogOff(messageNode)
  }

  val name: String = "pedLogOff"
}

final case class PedLogOffResponse(headerNode: HeaderNode, messageNode: MessageNode, result: Result, name: String = PedLogOffResponse.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="interaction">
      <INTERACTION name="pedLogOffResponse">
        <RESULT>
          { result.toString }
        </RESULT>
      </INTERACTION>
    </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PedLogOffResponse {
  implicit val format: OFormat[PedLogOffResponse] = Json.format[PedLogOffResponse]
  def fromXml(node: Node): PedLogOffResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "INTERACTION" \ "RESULT").text)
    val headerNode = HeaderNode.fromXml(node)
    PedLogOffResponse(headerNode, messageNode, result)
  }

  val name: String = "pedLogOffResponse"
}

final case class CancelTransaction(messageNode: MessageNode, name: String = CancelTransaction.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }
      <POI_MSG type="transactional">
        <TRANS name="cancelTransaction"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object CancelTransaction {
  implicit val format: OFormat[CancelTransaction] = Json.format[CancelTransaction]
  def fromXml(node: Node): CancelTransaction = {
    val messageNode = MessageNode.fromXml(node)
    CancelTransaction(messageNode)
  }

  val name: String = "cancelTransaction"
}

final case class ErrorMessage(headerNode: HeaderNode, messageNode: MessageNode, errorsNode: ErrorsNode, result: Result, name: String = ErrorMessage.name) extends SpcResponseMessage {
  override def isValid: Boolean = result match {
    case SuccessResult => true
    case FailureResult => false
  }

  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }
      <POI_MSG type="error">
        { errorsNode.toXml }
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object ErrorMessage {
  implicit val format: OFormat[ErrorMessage] = Json.format[ErrorMessage]
  def fromXml(node: Node): ErrorMessage = {
    val headerNode = HeaderNode.fromXml(node)
    val messageNode = MessageNode.fromXml(node)
    val errorsNode = ErrorsNode.fromXml(node)
    ErrorMessage(headerNode, messageNode, errorsNode, SuccessResult)
  }

  val name: String = "error"
}
