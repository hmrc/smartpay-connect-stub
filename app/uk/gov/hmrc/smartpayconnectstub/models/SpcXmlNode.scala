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

import uk.gov.hmrc.smartpayconnectstub.models.models.AmountInPence

import scala.xml.Node

/**
 * SCP - Smart Pay Connect - XML nodes that are used to build messages
 */
trait spcXmlNode

case class MessageNode(transNum: TransNum, deviceId: String, sourceId: String) extends spcXmlNode{
  def toXml:Node = {
    <MESSAGE>
      <TRANS_NUM>{transNum}</TRANS_NUM>
      <DEVICE_ID>{deviceId}</DEVICE_ID>
      <SOURCE_ID>{sourceId}</SOURCE_ID>
    </MESSAGE>
  }
}
object MessageNode {
  def fromXml(node: Node): MessageNode = {
    val transNr = TransNum((node \\ "MESSAGE" \ "TRANS_NUM").text)
    val sourceId = (node \\ "MESSAGE" \ "SOURCE_ID").text
    val deviceId = (node \\ "MESSAGE" \ "DEVICE_ID").text
    MessageNode(transNr, sourceId, deviceId)
  }
}

case object HeaderNode extends spcXmlNode{
  def toXml: Node = {
    <HEADER>
      <BUILD>
        <VERSION>1.34.0</VERSION>
      </BUILD>
    </HEADER>
  }
}

case class InteractionNode(category: InteractionCategory, event: InteractionEvent, prompt: InteractionPrompt) extends spcXmlNode{
  def toXml:Node = {
    <INTERACTION name="posDisplayMessage">
      <STATUS category={category.toString} event={event.toString}/>
      <PROMPT>{prompt.toString}</PROMPT>
    </INTERACTION>
  }
}
object InteractionNode {
  def fromXml(node: Node) = {
    val category = InteractionCategory((node \\ "INTERACTION" \ "STATUS" \ "@category").text)
    val event = InteractionEvent((node \\ "INTERACTION" \ "STATUS" \ "@event").text)
    val prompt = InteractionPrompt((node \\ "INTERACTION" \ "PROMPT").text)
    InteractionNode(category, event, prompt)
  }
}

case class AmountNode(totalAmount: AmountInPence, finalAmountO: Option[AmountInPence]) extends spcXmlNode{
  def toXml:Node = {
    val totalAmountNode =
      <AMOUNT currency="826" country="826">
        <TOTAL>{totalAmount}</TOTAL>
      </AMOUNT>

    finalAmountO.map{ finalAmount =>
      SpcXmlHelper.addNode(<FINAL>{finalAmount}</FINAL>,totalAmountNode)
    }.getOrElse(totalAmountNode)
  }
}
object AmountNode {
  def fromXml(node: Node): AmountNode = {
    val totalAmount = AmountInPence((node \\ "AMOUNT" \ "TOTAL").text)
    val finalAmountO = (node \\ "AMOUNT" \ "FINAL").headOption.map(x=> AmountInPence(x.text))
    AmountNode(totalAmount,finalAmountO)
  }
}


case class CardNode(end: String, pan:String, cardType:String) extends spcXmlNode{
  def toXml:Node = {
    <CARD>
      <PAN end={end} seqNum="01">{pan}</PAN>
      <APPLICATION id="A0000000031010">{cardType}</APPLICATION>
    </CARD>
  }
}
object CardNode {
  def fromXml(node: Node): CardNode = {
    val end = (node \\ "CARD" \ "PAN" \ "@end").text
    val pan = (node \\ "CARD" \ "PAN").text
    val cardType = (node \\ "CARD" \ "APPLICATION").text
    CardNode(end, pan, cardType)
  }
}


