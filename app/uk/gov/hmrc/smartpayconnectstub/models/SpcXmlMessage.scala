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

import scala.xml.Node

/**
 * SCP - Smart Pay Connect - XML messages
 */
trait spcXmlMessage

case class PedLogOn(messageNode: MessageNode) extends spcXmlMessage {
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXml}
      <POI_MSG type="interaction">
        <INTERACTION name="pedLogOn"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PedLogOn {
  def fromXml(node: Node): PedLogOn = {
    val messageNode = MessageNode.fromXml(node)
    PedLogOn(messageNode)
  }
}


case class  PedLogOnResponse (messageNode: MessageNode, result: Result) extends  spcXmlMessage {
  def isValid:Boolean = result match {
    case Success => true
    case Failure => false
  }
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXml}
      {messageNode.toXml}
      <POI_MSG type="interaction">
        <INTERACTION name="pedLogOnResponse">
          <RESULT>{result.toString}</RESULT>
        </INTERACTION>
      </POI_MSG>
    </RLSOLVE_MSG>

  }
}

object PedLogOnResponse {
  def fromXml(node: Node): PedLogOnResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "INTERACTION" \ "RESULT").text)
    PedLogOnResponse(messageNode, result)
  }
}

case class SubmitPayment(messageNode: MessageNode, amountNode: AmountNode)  extends  spcXmlMessage{
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXml}
      <POI_MSG type="submittal">
        <SUBMIT name="submitPayment">
          <TRANSACTION type="purchase" action="auth_n_settle" source="icc" customer="present">
            {amountNode.toXml}
          </TRANSACTION>
        </SUBMIT>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object SubmitPayment {
  def fromXml(node: Node): SubmitPayment = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    SubmitPayment(messageNode, amountNode)
  }
}

case class SubmitPaymentResponse(messageNode: MessageNode, result: Result)  extends  spcXmlMessage{
  def isValid:Boolean = result match {
    case Success => true
    case Failure => false
  }
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXml}
      {messageNode.toXml}
      <POI_MSG type="submittal">
        <SUBMIT name="submitPaymentResponse">
          <RESULT>{result.toString}</RESULT>
        </SUBMIT>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object SubmitPaymentResponse {
  def fromXml(node: Node): SubmitPaymentResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "SUBMIT" \ "RESULT").text)
    SubmitPaymentResponse(messageNode, result)
  }
}


case class ProcessTransaction(messageNode: MessageNode) extends spcXmlMessage {
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXml}
      <POI_MSG type="transactional">
        <TRANS name="processTransaction"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object ProcessTransaction {
  def fromXml(node: Node): ProcessTransaction = {
    val messageNode = MessageNode.fromXml(node)
    ProcessTransaction(messageNode)
  }
}

case class PosDisplayMessage(messageNode: MessageNode, interactionNode: InteractionNode) extends spcXmlMessage {
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXml}
      {messageNode.toXml}
      <POI_MSG type="interaction">
        {interactionNode.toXml}
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PosDisplayMessage {
  def fromXml(node: Node): PosDisplayMessage = {
    val messageNode = MessageNode.fromXml(node)
    val interactionNode = InteractionNode.fromXml(node)
    PosDisplayMessage(messageNode, interactionNode)
  }
}


case class UpdatePaymentEnhanced(messageNode: MessageNode, amountNode: AmountNode, cardNode: CardNode) extends spcXmlMessage {
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXml}
      {messageNode.toXml}
      <POI_MSG type="transactional">
        <TRANS name="updatePaymentEnhanced">
          <PAYMENT>
            <TRANSACTION action="auth_n_settle" customer="present"
                         source="contactlessEMV" type="purchase">
              {amountNode.toXml}
            </TRANSACTION>
            {cardNode.toXml}
          </PAYMENT>
        </TRANS>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object UpdatePaymentEnhanced {
  def fromXml(node: Node): UpdatePaymentEnhanced = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    val cardNode = CardNode.fromXml(node)
    UpdatePaymentEnhanced(messageNode, amountNode, cardNode)
  }
}

case class UpdatePaymentEnhancedResponse(messageNode: MessageNode, amountNode: AmountNode) extends spcXmlMessage {
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXml}
      <POI_MSG type="transactional">
        <TRANS name="updatePaymentEnhancedResponse">
          <PAYMENT>
            <TRANSACTION>
              {amountNode.toXml}
            </TRANSACTION>
          </PAYMENT>
        </TRANS>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object UpdatePaymentEnhancedResponse {
  def fromXml(node: Node): UpdatePaymentEnhancedResponse = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    UpdatePaymentEnhancedResponse(messageNode, amountNode)
  }
}


case class ProcessTransactionResponse(messageNode: MessageNode,amountNode: AmountNode ,result: Result, paymentResult: PaymentResult)  extends  spcXmlMessage{
  def isValid:Boolean = result match {
    case Success => true
    case Failure => false
  }
  def toXml:Node = {
      <RLSOLVE_MSG version="5.0">
        {HeaderNode.toXml}
        {messageNode.toXml}
        <POI_MSG type="transactional">
          <TRANS name="processTransactionResponse">
            <RESULT>{result.toString}</RESULT>
            <INTERFACE>
              <TERMINAL serialNumber="200417333011101013619956">
                <TERMINAL_TYPE>22</TERMINAL_TYPE>
              </TERMINAL>
            </INTERFACE>
            <PAYMENT>
              <PAYMENT_RESULT>{paymentResult.toString}</PAYMENT_RESULT>
              <ACQUIRER id="V">VISA</ACQUIRER>
              <BANK id="B">Barclays-Dummy-51922203</BANK>
              <MERCHANT number="9876543"/>
              <HOST_RESP responseCode="00"><![CDATA[ NOT AUTHORISED ]]></HOST_RESP>
              <TRANSACTION action="auth_n_settle" customer="present"
                           date="2021-11-24"
                           reference="7942dec3-5750-498c-8df0-3dd9d9487362"
                           source="contactlessEMV" time="11:52:20" type="purchase">
                <AUTH_CODE> D12345 </AUTH_CODE>
                <CARDHOLDER_RESULT verification="not_performed">000000</CARDHOLDER_RESULT>
                <DESCRIPTION>SCpdq Test Transaction</DESCRIPTION>
                <AUTH_REQ_CRYPTO>EDA8B89CECB011C3</AUTH_REQ_CRYPTO>
                <AUTH_RESP_CODE>00</AUTH_RESP_CODE>
                <CRYPTO_INFO_DATA>80</CRYPTO_INFO_DATA>
                <TERMINAL_RESULT>0000000000</TERMINAL_RESULT>
                <UNPREDICTABLE_NUM>3FECD91E</UNPREDICTABLE_NUM>
                <CRYPTO_TRANSTYPE>00</CRYPTO_TRANSTYPE>
                {amountNode.toXml}
              </TRANSACTION>
              <CARD range="0">
                <PAN end="2024-12-31" seqNum="01">417666******0019</PAN>
                <APPLICATION id="A0000000031010" version="0096">VISA CREDIT<INTERCHANGE_PROFILE>2000</INTERCHANGE_PROFILE>
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
            <RECEIPT format="plain" type="merchant"><![CDATA[* * DUPLICATE * *

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

MERCHANT COPY]]></RECEIPT>
            <RECEIPT format="plain" type="customer"><![CDATA[* * DUPLICATE * *

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

CUSTOMER COPY]]></RECEIPT>
          </TRANS>
        </POI_MSG>
      </RLSOLVE_MSG>
  }
}

object ProcessTransactionResponse {
  def fromXml(node: Node): ProcessTransactionResponse = {
    val messageNode = MessageNode.fromXml(node)
    val amountNode = AmountNode.fromXml(node)
    val result = Result((node \\ "TRANS" \ "RESULT").text)
    val paymentResult = PaymentResult((node \\ "PAYMENT" \ "PAYMENT_RESULT").text)
    ProcessTransactionResponse(messageNode, amountNode, result, paymentResult)
  }
}


case class PosPrintReceipt(messageNode: MessageNode) extends spcXmlMessage {
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXml}
      {messageNode.toXml}
      <POI_MSG type="interaction">
        <INTERACTION name="posPrintReceipt">
          <RECEIPT format="plain" type="merchant"><![CDATA[
AID: A0000000031010
VISA CREDIT
Card: ************0019
PAN Seq Nr: 01

CONTACTLESS
SALE
CANCELLED
TOTAL: GBP10.00

Trans no: 58
Date: 24/11/2021 Time: 11:19:56

MERCHANT COPY]]></RECEIPT>
        </INTERACTION>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PosPrintReceipt {
  def fromXml(node: Node): PosPrintReceipt = {
    val messageNode = MessageNode.fromXml(node)
    PosPrintReceipt(messageNode)
  }
}


case class  PosPrintReceiptResponse (messageNode: MessageNode, result: Result) extends spcXmlMessage {
  def isValid:Boolean = result match {
    case Success => true
    case Failure => false
  }
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXml}
      <POI_MSG type="interaction">
        <INTERACTION name="posPrintReceiptResponse">
          <RESPONSE>{result.toString}</RESPONSE>
        </INTERACTION>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PosPrintReceiptResponse {
  def fromXml(node: Node): PosPrintReceiptResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "INTERACTION" \ "RESPONSE").text)
    PosPrintReceiptResponse(messageNode, result)
  }
}

case class Finalise(messageNode: MessageNode) extends spcXmlMessage {
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXml}
      <POI_MSG type="transactional">
        <TRANS name="finalise"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object Finalise {
  def fromXml(node: Node): Finalise = {
    val messageNode = MessageNode.fromXml(node)
    Finalise(messageNode)
  }
}


case class  FinaliseResponse (messageNode: MessageNode, result: Result) extends spcXmlMessage {
  def isValid:Boolean = result match {
    case Success => true
    case Failure => false
  }
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXml}
      {messageNode.toXml}
      <POI_MSG type="transactional">
        <TRANS name="finaliseResponse">
          <RESULT>{result.toString}</RESULT>
        </TRANS>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object FinaliseResponse {
  def fromXml(node: Node): FinaliseResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "TRANS" \ "RESULT").text)
    FinaliseResponse(messageNode, result)
  }
}

case class PedLogOff(messageNode: MessageNode) extends spcXmlMessage {
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXml}
      <POI_MSG type="interaction">
        <INTERACTION name="pedLogOff"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PedLogOff {
  def fromXml(node: Node): PedLogOff = {
    val messageNode = MessageNode.fromXml(node)
    PedLogOff(messageNode)
  }
}


case class  PedLogOffResponse (messageNode: MessageNode, result: Result) extends spcXmlMessage {
  def isValid:Boolean = result match {
    case Success => true
    case Failure => false
  }
  def toXml:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXml}
      {messageNode.toXml}
      <POI_MSG type="interaction">
        <INTERACTION name="pedLogOffResponse">
          <RESULT>{result.toString}</RESULT>
        </INTERACTION>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PedLogOffResponse {
  def fromXml(node: Node): PedLogOffResponse = {
    val messageNode = MessageNode.fromXml(node)
    val result = Result((node \\ "TRANS" \ "RESULT").text)
    PedLogOffResponse(messageNode, result)
  }
}