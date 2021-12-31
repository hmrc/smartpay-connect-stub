/*
 * Copyright 2021 HM Revenue & Customs
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

import scala.xml.{Elem, Node}

//TODO - do it better

//TODO improve parsing XML



object SpcMessageHelper {
  def getSpcXMLMessage(node: Node):spcXMLMessage = {
    (node \\ "POI_MSG" \ "@type").text match {
      case "interaction" =>
        (node \\ "INTERACTION" \ "@name").text match {
          case "pedLogOn" => PedLogOn.fromXML(node)
          case "posDisplayMessage" => PosDisplayMessage.fromXML(node)
          case x => throw new RuntimeException(s"Unknown INTERACTION name: $x")
        }
      case "submittal" =>
        (node \\ "SUBMIT" \ "@name").text match {
          case "submitPayment" => SubmitPayment.fromXML(node)
          case x => throw new RuntimeException(s"Unknown SUBMIT name: $x")
        }
      case "transactional" =>
        (node \\ "TRANS" \ "@name").text match {
          case "processTransaction" => ProcessTransaction.fromXML(node)
          case "updatePaymentEnhancedResponse" => UpdatePaymentEnhancedResponse.fromXML(node)
          case x => throw new RuntimeException(s"Unknown TRANS name: $x")
        }
      case x => throw new RuntimeException(s"Unknown POI_MSG type: $x")
    }
  }



  def addNode(to: Node, newNode: Node) = to match {
    case Elem(prefix, label, attributes, scope, child@_*) => Elem(prefix, label, attributes, scope, child ++ newNode: _*)
    case _ => println("could not find node"); to
  }
}


trait spcXMLNode

case class MessageNode(transNr: String, deviceId: String, sourceId: String) extends spcXMLNode{
  def toXML:Node = {
    <MESSAGE>
      <TRANS_NUM>{transNr}</TRANS_NUM>
      <DEVICE_ID>{deviceId}</DEVICE_ID>
      <SOURCE_ID>{sourceId}</SOURCE_ID>
    </MESSAGE>
  }
}
object MessageNode {
  def fromXML(node: Node) = {
    val transNr = (node \\ "TRANS_NUM").text
    val sourceId = (node \\ "SOURCE_ID").text
    val deviceId = (node \\ "DEVICE_ID").text
    MessageNode(transNr, sourceId, deviceId)
  }
}

case object HeaderNode extends spcXMLNode{
  def toXML: Node = {
    <HEADER>
      <BUILD>
        <VERSION>1.34.0</VERSION>
      </BUILD>
    </HEADER>
  }
}

case class InteractionNode(category: String, event: String, prompt: String) extends spcXMLNode{
  def toXML:Node = {
    <INTERACTION name="posDisplayMessage">
      <STATUS category={category} event={event}/>
      <PROMPT>{prompt}</PROMPT>
    </INTERACTION>
  }
}
object InteractionNode {
  def fromXML(node: Node) = {
    val category = (node \\ "STATUS" \ "@category").text
    val event = (node \\ "STATUS" \ "@event").text
    val prompt = (node \\ "INTERACTION" \ "PROMPT").text
    InteractionNode(category, event, prompt)
  }
}

case class AmountNode(totalAmount: Double, finalAmountO: Option[Double]) extends spcXMLNode{
  def toXML:Node = {
    val totalAmountNode =
      <AMOUNT currency="826" country="826">
        <TOTAL>{totalAmount}</TOTAL>
       </AMOUNT>

    finalAmountO.map{ finalAmount =>
      val finalAmountNode = <FINAL>{finalAmount}</FINAL>
      SpcMessageHelper.addNode(finalAmountNode,totalAmountNode)
    }.getOrElse(totalAmountNode)
  }
}
object AmountNode {
  def fromXML(node: Node): AmountNode = {
    val totalAmount = (node \\ "AMOUNT" \ "TOTAL").text
    val finalAmountO = (node \\ "AMOUNT" \ "FINAL").headOption.map(_.text)
    AmountNode(totalAmount.toDouble,finalAmountO.map(_.toDouble))
  }
}


case class CardNode(end: String, pan:String, cardType:String) extends spcXMLNode{
  def toXML:Node = {
    <CARD>
      <PAN end={end} seqNum="01">{pan}</PAN>
      <APPLICATION id="A0000000031010">{cardType}</APPLICATION>
    </CARD>
  }
}
object CardNode {
  def fromXML(node: Node): CardNode = {
    val end = (node \\ "CARD" \ "PAN" \ "@end").text
    val pan = (node \\ "CARD" \ "PAN").text
    val cardType = (node \\ "CARD" \ "APPLICATION").text
    CardNode(end, pan, cardType)
  }
}



trait spcXMLMessage

case class PedLogOn(messageNode: MessageNode) extends spcXMLMessage{
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXML}
      <POI_MSG type="interaction">
        <INTERACTION name="pedLogOn"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PedLogOn {
  def fromXML(node: Node): PedLogOn = {
    val messageNode = MessageNode.fromXML(node)
    PedLogOn(messageNode)
  }
}


case class  PedLogOnResponse (messageNode: MessageNode, result: String) extends  spcXMLMessage{
  def isValid:Boolean = result.compareToIgnoreCase("success") == 0
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXML}
      {messageNode.toXML}
      <POI_MSG type="interaction">
        <INTERACTION name="pedLogOnResponse">
          <RESULT>{result}</RESULT>
        </INTERACTION>
      </POI_MSG>
    </RLSOLVE_MSG>

  }
}

object PedLogOnResponse {
  def fromXML(node: Node): PedLogOnResponse = {
    val messageNode = MessageNode.fromXML(node)
    val result = (node \\ "RESULT").text
    PedLogOnResponse(messageNode, result)
  }
}



//TODO Amount as BIgInt, roundign etc create separate class and methods
case class SubmitPayment(messageNode: MessageNode, amountNode: AmountNode)  extends  spcXMLMessage{
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXML}
      <POI_MSG type="submittal">
        <SUBMIT name="submitPayment">
          <TRANSACTION type="purchase" action="auth_n_settle" source="icc" customer="present">
            {amountNode.toXML}
          </TRANSACTION>
        </SUBMIT>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object SubmitPayment {
  def fromXML(node: Node): SubmitPayment = {
    val messageNode = MessageNode.fromXML(node)
    val amountNode = AmountNode.fromXML(node)
    SubmitPayment(messageNode, amountNode)
  }
}

case class SubmitPaymentResponse(messageNode: MessageNode, result: String)  extends  spcXMLMessage{
  def isValid:Boolean = result.compareToIgnoreCase("success") == 0
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXML}
      {messageNode.toXML}
      <POI_MSG type="submittal">
        <SUBMIT name="submitPaymentResponse">
          <RESULT>{result}</RESULT>
        </SUBMIT>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object SubmitPaymentResponse {
  def fromXML(node: Node): SubmitPaymentResponse = {
    val messageNode = MessageNode.fromXML(node)
    val result = (node \\ "RESULT").text
    SubmitPaymentResponse(messageNode, result)
  }
}


case class ProcessTransaction(messageNode: MessageNode) extends spcXMLMessage {
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXML}
      <POI_MSG type="transactional">
        <TRANS name="processTransaction"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object ProcessTransaction {
  def fromXML(node: Node): ProcessTransaction = {
    val messageNode = MessageNode.fromXML(node)
    ProcessTransaction(messageNode)
  }
}

case class PosDisplayMessage(messageNode: MessageNode, interactionNode: InteractionNode) extends spcXMLMessage {
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXML}
      {messageNode.toXML}
      <POI_MSG type="interaction">
        {interactionNode.toXML}
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object PosDisplayMessage {
  def fromXML(node: Node): PosDisplayMessage = {
    val messageNode = MessageNode.fromXML(node)
    val interactionNode = InteractionNode.fromXML(node)
    PosDisplayMessage(messageNode, interactionNode)
  }
}


case class UpdatePaymentEnhanced(messageNode: MessageNode, amountNode: AmountNode, cardNode: CardNode) extends spcXMLMessage {
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {HeaderNode.toXML}
      {messageNode.toXML}
      <POI_MSG type="transactional">
        <TRANS name="updatePaymentEnhanced">
          <PAYMENT>
            <TRANSACTION action="auth_n_settle" customer="present"
                         source="contactlessEMV" type="purchase">
              {amountNode.toXML}
            </TRANSACTION>
            {cardNode.toXML}
          </PAYMENT>
        </TRANS>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object UpdatePaymentEnhanced {
  def fromXML(node: Node): UpdatePaymentEnhanced = {
    val messageNode = MessageNode.fromXML(node)
    val amountNode = AmountNode.fromXML(node)
    val cardNode = CardNode.fromXML(node)
    UpdatePaymentEnhanced(messageNode, amountNode, cardNode)
  }
}

case class UpdatePaymentEnhancedResponse(messageNode: MessageNode, amountNode: AmountNode) extends spcXMLMessage {
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXML}
      <POI_MSG type="transactional">
        <TRANS name="updatePaymentEnhancedResponse">
          <PAYMENT>
            <TRANSACTION>
              {amountNode.toXML}
            </TRANSACTION>
          </PAYMENT>
        </TRANS>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object UpdatePaymentEnhancedResponse {
  def fromXML(node: Node): UpdatePaymentEnhancedResponse = {
    val messageNode = MessageNode.fromXML(node)
    val amountNode = AmountNode.fromXML(node)
    UpdatePaymentEnhancedResponse(messageNode, amountNode)
  }
}

case class ProcessTransactionResponse(messageNode: MessageNode,amountNode: AmountNode ,result: String)  extends  spcXMLMessage{
  def isValid:Boolean = result.compareToIgnoreCase("success") == 0
  def toXML:Node = {
      <RLSOLVE_MSG version="5.0">
        {HeaderNode.toXML}
        {messageNode.toXML}
        <POI_MSG type="transactional">
          <TRANS name="processTransactionResponse">
            <RESULT>success</RESULT>
            <INTERFACE>
              <TERMINAL serialNumber="200417333011101013619956">
                <TERMINAL_TYPE>22</TERMINAL_TYPE>
              </TERMINAL>
            </INTERFACE>
            <PAYMENT>
              <PAYMENT_RESULT>declined</PAYMENT_RESULT>
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
                {amountNode.toXML}
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
  def fromXML(node: Node): ProcessTransactionResponse = {
    val messageNode = MessageNode.fromXML(node)
    val amountNode = AmountNode.fromXML(node)
    val result = (node \\ "RESULT").text
    ProcessTransactionResponse(messageNode, amountNode, result)
  }
}
