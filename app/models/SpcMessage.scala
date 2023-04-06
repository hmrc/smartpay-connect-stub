/*
 * Copyright 2023 HM Revenue & Customs
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

import models.TranResults.SuccessResult

import scala.xml.{Node, NodeSeq}

/**
 * SCP - Smart Pay Connect - XML messages
 */
trait F2FMessage {
  val name: String
}

sealed trait SpcMessage extends F2FMessage

sealed trait SpcRequestMessage extends SpcMessage {
  val messageNode: MessageNode
}

sealed trait SpcResponseMessage extends SpcMessage {
  def toXml: Node
  def toXmlString: String = toXml.toString()

}

object SpcResponseMessage

final case class PedLogOn(messageNode: MessageNode, name: String = PedLogOn.name) extends SpcRequestMessage

object PedLogOn {
  def fromXml(node: Node): PedLogOn = {
    val messageNode = MessageNode.fromXml(node)
    PedLogOn(messageNode)
  }

  val name: String = "pedLogOn"
}

final case class PedLogOnResponse(headerNode: HeaderNode, messageNode: MessageNode, result: TranResult, errors: ErrorsNode, name: String = PedLogOnResponse.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }
      { messageNode.toXml }
      <POI_MSG type="interaction">
        <INTERACTION name="pedLogOnResponse">
          { errors.toXml }
          <RESULT>{ result.toString }</RESULT>
        </INTERACTION>
      </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PedLogOnResponse {
  val name: String = "pedLogOnResponse"
}
//icc, keyed
final case class SubmitPayment(messageNode: MessageNode, transactionNode: TransactionNode, name: String = SubmitPayment.name) extends SpcRequestMessage

object SubmitPayment {
  def fromXml(node: Node): SubmitPayment = {
    val messageNode = MessageNode.fromXml(node)
    val transactionNode = TransactionNode.fromXml(node)
    SubmitPayment(messageNode, transactionNode)
  }

  val name: String = "submitPayment"
}

final case class SubmitPaymentResponse(headerNode: HeaderNode, messageNode: MessageNode, result: TranResult, name: String = SubmitPaymentResponse.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="submittal">
                                                 <SUBMIT name="submitPaymentResponse">
                                                   <RESULT>{ result.toString }</RESULT>
                                                 </SUBMIT>
                                               </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object SubmitPaymentResponse {
  val name: String = "submitPaymentResponse"
}

final case class ProcessTransaction(messageNode: MessageNode, name: String = ProcessTransaction.name) extends SpcRequestMessage

object ProcessTransaction {
  def fromXml(node: Node): ProcessTransaction = {
    val messageNode = MessageNode.fromXml(node)
    ProcessTransaction(messageNode)
  }

  val name: String = "processTransaction"
}

final case class PosDecisionMessage(headerNode: HeaderNode, messageNode: MessageNode, transNode: PdTransNode, name: String = PosDecisionMessage.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="transactional">
                                                 { transNode.toXml }
                                               </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PosDecisionMessage {
  val name: String = "posDecision"
}

final case class PosDisplayMessage(headerNode: HeaderNode, messageNode: MessageNode, interactionNode: InteractionNode, result: TranResult, errors: ErrorsNode, name: String = PosDisplayMessage.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="interaction">
                                                 { interactionNode.toXml }
                                               </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PosDisplayMessage {
  val name: String = "posDisplayMessage"
}

final case class UpdatePaymentEnhanced(headerNode: HeaderNode, messageNode: MessageNode, transactionNode: TransactionNode, cardNode: UpeCardNode, result: TranResult, errors: ErrorsNode, name: String = UpdatePaymentEnhanced.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="transactional">
                                                 <TRANS name="updatePaymentEnhanced">
                                                   <PAYMENT>
                                                     <ACQUIRER id="X">XXXX</ACQUIRER>
                                                     <BANK id="X">XXXXXX-XXXXX-XXXXXX</BANK>
                                                     <MERCHANT number={ StubUtil.MERCHANT_NUMBER }/>
                                                     { transactionNode.toXml }
                                                     { cardNode.toXml }
                                                   </PAYMENT>
                                                 </TRANS>
                                               </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object UpdatePaymentEnhanced {
  val name: String = "updatePaymentEnhanced"
}

final case class UpdatePaymentEnhancedResponse(messageNode: MessageNode, amountNode: AmountNode, name: String = UpdatePaymentEnhancedResponse.name) extends SpcRequestMessage

object UpdatePaymentEnhancedResponse {
  def fromXml(node: Node): UpdatePaymentEnhancedResponse = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    UpdatePaymentEnhancedResponse(messageNode, amountNode)
  }

  val name: String = "updatePaymentEnhancedResponse"
}

final case class ProcessTransactionResponse(
    headerNode:           HeaderNode,
    messageNode:          MessageNode,
    ptrTransactionNode:   PtrTransactionNode,
    ptrCardNode:          PtrResponseCardNode,
    result:               TranResult,
    paymentResult:        PaymentResult,
    receiptNodeCustomerO: Option[ReceiptNode],
    receiptNodeMerchantO: Option[ReceiptNode],
    errorsNode:           ErrorsNode,
    name:                 String              = ProcessTransactionResponse.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }
      <POI_MSG type="transactional">
        <TRANS name="processTransactionResponse">
          { errorsNode.toXml }
          <RESULT>{ result.toString }</RESULT>
          <INTERFACE>
            <TERMINAL serialNumber="XXXXXXXXXXXXXXXXX">
              <TERMINAL_TYPE>XX</TERMINAL_TYPE>
            </TERMINAL>
          </INTERFACE>
          <PAYMENT>
            <PAYMENT_RESULT>{ paymentResult.toString }</PAYMENT_RESULT>
            <ACQUIRER id="X">XXXXXX</ACQUIRER>
            <BANK id="X">XXXXXXXXX</BANK>
            <MERCHANT number={ StubUtil.MERCHANT_NUMBER }/>
            <HOST_RESP responseCode="XX"><![CDATA[ NOT AUTHORISED ]]></HOST_RESP>
            { ptrTransactionNode.toXml }
            { ptrCardNode.toXml }
          </PAYMENT>
          { receiptNodeMerchantO.fold(NodeSeq.Empty)(receiptNodeMerchant => receiptNodeMerchant.toXml(ReceiptTypes.MerchantSignatureReceipt)) }
          { receiptNodeCustomerO.fold(NodeSeq.Empty)(receiptNodeCustomer => receiptNodeCustomer.toXml(ReceiptTypes.CustomerReceipt)) }
        </TRANS>
      </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object ProcessTransactionResponse {
  val name: String = "processTransactionResponse"
}

final case class PosPrintReceipt(headerNode: HeaderNode, messageNode: MessageNode, receiptNode: ReceiptNode, result: TranResult, errors: ErrorsNode, name: String = PosPrintReceipt.name) extends SpcResponseMessage {
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
  val name: String = "posPrintReceipt"
}

final case class PosPrintReceiptResponse(messageNode: MessageNode, result: TranResult, name: String = PosPrintReceiptResponse.name) extends SpcRequestMessage

object PosPrintReceiptResponse {
  def fromXml(node: Node): PosPrintReceiptResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = TranResult((node \\ "INTERACTION" \ "RESPONSE").text)
    PosPrintReceiptResponse(messageNode, result)
  }

  val name: String = "posPrintReceiptResponse"
}

final case class Finalise(messageNode: MessageNode, name: String = Finalise.name) extends SpcRequestMessage

object Finalise {
  def fromXml(node: Node): Finalise = {
    val messageNode = MessageNode.fromXml(node)
    Finalise(messageNode)
  }

  val name: String = "finalise"
}

final case class CompleteTransaction(messageNode: MessageNode, name: String = CompleteTransaction.name) extends SpcRequestMessage

object CompleteTransaction {
  def fromXml(node: Node): CompleteTransaction = {
    val messageNode = MessageNode.fromXml(node)
    CompleteTransaction(messageNode)
  }

  val name: String = "completeTransaction"
}

final case class FinaliseResponse(headerNode: HeaderNode, messageNode: MessageNode, result: TranResult, name: String = FinaliseResponse.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="transactional">
                                                 <TRANS name="finaliseResponse">
                                                   <RESULT>{ result.toString }</RESULT>
                                                 </TRANS>
                                               </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object FinaliseResponse {
  val name: String = "finaliseResponse"
}

final case class PedLogOff(messageNode: MessageNode, name: String = PedLogOff.name) extends SpcRequestMessage

object PedLogOff {
  def fromXml(node: Node): PedLogOff = {
    val messageNode = MessageNode.fromXml(node)
    PedLogOff(messageNode)
  }

  val name: String = "pedLogOff"
}

final case class PedLogOffResponse(headerNode: HeaderNode, messageNode: MessageNode, result: TranResult, name: String = PedLogOffResponse.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }<POI_MSG type="interaction">
                                                 <INTERACTION name="pedLogOffResponse">
                                                   <RESULT>{ result.toString }</RESULT>
                                                 </INTERACTION>
                                               </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object PedLogOffResponse {
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
  def fromXml(node: Node): CancelTransaction = {
    val messageNode = MessageNode.fromXml(node)
    CancelTransaction(messageNode)
  }

  val name: String = "cancelTransaction"
}

final case class ErrorMessage(headerNode: HeaderNode, messageNode: MessageNode, errorsNode: ErrorsNode, result: TranResult, name: String = ErrorMessage.name) extends SpcResponseMessage {
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
  def fromXml(node: Node): ErrorMessage = {
    val headerNode = HeaderNode.fromXml(node)
    val messageNode = MessageNode.fromXml(node)
    val errorsNode = ErrorsNode.fromXml(node)
    ErrorMessage(headerNode, messageNode, errorsNode, SuccessResult)
  }

  val name: String = "error"
}

final case class GetTransactionDetails(messageNode: MessageNode, name: String = GetTransactionDetails.name) extends SpcRequestMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { messageNode.toXml }
      <POI_MSG type="transactional">
        <TRANS name="getTransactionDetails"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object GetTransactionDetails {
  def fromXml(node: Node): GetTransactionDetails = {
    val messageNode = MessageNode.fromXml(node)
    GetTransactionDetails(messageNode)
  }

  val name: String = "getTransactionDetails"
}

final case class GetTransactionDetailsResponse(
    headerNode:           HeaderNode,
    messageNode:          MessageNode,
    ptrTransactionNode:   PtrTransactionNode,
    ptrCardNode:          PtrResponseCardNode,
    result:               TranResult,
    paymentResult:        PaymentResult,
    receiptNodeCustomerO: Option[ReceiptNode],
    receiptNodeMerchantO: Option[ReceiptNode],
    errorsNode:           ErrorsNode,
    name:                 String              = GetTransactionDetailsResponse.name) extends SpcResponseMessage {
  def toXml: Node = {
    <RLSOLVE_MSG version="5.0">
      { headerNode.toXml }{ messageNode.toXml }
      <POI_MSG type="transactional">
        <TRANS name="getTransactionDetailsResponse">
          { errorsNode.toXml }
          <RESULT>{ result.toString }</RESULT>
          <INTERFACE>
            <TERMINAL serialNumber="XXXXXXXXXXXXXXXXX">
              <TERMINAL_TYPE>XX</TERMINAL_TYPE>
            </TERMINAL>
          </INTERFACE>
          <PAYMENT>
            <PAYMENT_RESULT>{ paymentResult.toString }</PAYMENT_RESULT>
            <ACQUIRER id="X">XXXXXX</ACQUIRER>
            <BANK id="X">XXXXXXXXX</BANK>
            <MERCHANT number={ StubUtil.MERCHANT_NUMBER }/>
            <HOST_RESP responseCode="XX"><![CDATA[ NOT AUTHORISED ]]></HOST_RESP>
            { ptrTransactionNode.toXml }
            { ptrCardNode.toXml }
          </PAYMENT>
          { receiptNodeMerchantO.fold(NodeSeq.Empty)(receiptNodeMerchant => receiptNodeMerchant.toXml(ReceiptTypes.MerchantSignatureReceipt)) }
          { receiptNodeCustomerO.fold(NodeSeq.Empty)(receiptNodeCustomer => receiptNodeCustomer.toXml(ReceiptTypes.CustomerReceipt)) }
        </TRANS>
      </POI_MSG>
    </RLSOLVE_MSG>
  }

}

object GetTransactionDetailsResponse {
  val name: String = "getTransactionDetailsResponse"
}
