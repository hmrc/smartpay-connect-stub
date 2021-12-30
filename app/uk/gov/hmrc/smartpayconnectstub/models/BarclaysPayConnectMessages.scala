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

import scala.xml.Node

//TODO - do it better

//TODO improve parsing XML



object SpcMessageHelper {
  def getSpcXMLMessage(node: Node):spcXMLMessage = {
    (node \\ "POI_MSG" \ "@type").text match {
      case "interaction" =>
        (node \\ "INTERACTION" \ "@name").text match {
          case "pedLogOn" => PedLogOn.fromXML(node)
//          case "posDisplayMessage" => ???
          case x => throw new RuntimeException(s"Unknown INTERACTION name: $x")
        }
      case "submittal" =>
        (node \\ "SUBMIT" \ "@name").text match {
          case "submitPayment" => SubmitPayment.fromXML(node)
          case x => throw new RuntimeException(s"Unknown SUBMIT name: $x")
        }
      case "transactional" =>
        (node \\ "TRANS" \ "@name").text match {
//          case "processTransaction" => ???
//          case "updatePaymentEnhanced" => ???
          case x => throw new RuntimeException(s"Unknown TRANS name: $x")
        }
      case x => throw new RuntimeException(s"Unknown POI_MSG type: $x")
    }

  }


  def createPedLogOnResponse(pedLogOn: PedLogOn, result: String):PedLogOnResponse = {
    PedLogOnResponse(pedLogOn.messageNode,result)
  }

  def createSubmitPaymentResponse(submitPayment: SubmitPayment, result: String):SubmitPaymentResponse = {
    SubmitPaymentResponse(submitPayment.messageNode,result)
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


case class MessageNode(transNr: String, deviceId: String, sourceId: String)  {
  def toXML:Node = {
    <MESSAGE>
      <TRANS_NUM>{transNr}</TRANS_NUM>
      <DEVICE_ID>{deviceId}</DEVICE_ID>
      <SOURCE_ID>{sourceId}</SOURCE_ID>
    </MESSAGE>
  }
}

case object Header {
  def toXML: Node = {
    <HEADER>
      <BUILD>
        <VERSION>1.34.0</VERSION>
      </BUILD>
    </HEADER>
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
      {Header.toXML}
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
case class SubmitPayment(messageNode: MessageNode, amount: Double)  extends  spcXMLMessage{
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {messageNode.toXML}
      <POI_MSG type="submittal">
        <SUBMIT name="submitPayment">
          <TRANSACTION type="purchase" action="auth_n_settle" source="icc" customer="present">
            <AMOUNT currency="826" country="826">
              <TOTAL>{amount}</TOTAL>
            </AMOUNT>
          </TRANSACTION>
        </SUBMIT>
      </POI_MSG>
    </RLSOLVE_MSG>
  }
}

object SubmitPayment {
  def fromXML(node: Node): SubmitPayment = {
    val messageNode = MessageNode.fromXML(node)
    val amount = (node \\ "AMOUNT" \ "TOTAL").text
    SubmitPayment(messageNode, amount.toDouble)
  }
}

case class SubmitPaymentResponse(messageNode: MessageNode, result: String)  extends  spcXMLMessage{
  def isValid:Boolean = result.compareToIgnoreCase("success") == 0
  def toXML:Node = {
    <RLSOLVE_MSG version="5.0">
      {Header.toXML}
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

