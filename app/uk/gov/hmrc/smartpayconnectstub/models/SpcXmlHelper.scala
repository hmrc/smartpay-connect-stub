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

import scala.xml.{Elem, Node}

/**
 * SPC- Smart Pay Connect helper functions
 */
object SpcXmlHelper {
  def getSpcXMLMessage(node: Node):spcXmlMessage = {
    (node \\ "POI_MSG" \ "@type").text match {
      case "interaction" =>
        (node \\ "INTERACTION" \ "@name").text match {
          case "pedLogOn" => PedLogOn.fromXml(node)
          case "posDisplayMessage" => PosDisplayMessage.fromXml(node)
          case x => throw new RuntimeException(s"Unknown INTERACTION name: $x")
        }
      case "submittal" =>
        (node \\ "SUBMIT" \ "@name").text match {
          case "submitPayment" => SubmitPayment.fromXml(node)
          case x => throw new RuntimeException(s"Unknown SUBMIT name: $x")
        }
      case "transactional" =>
        (node \\ "TRANS" \ "@name").text match {
          case "processTransaction" => ProcessTransaction.fromXml(node)
          case "updatePaymentEnhancedResponse" => UpdatePaymentEnhancedResponse.fromXml(node)
          case x => throw new RuntimeException(s"Unknown TRANS name: $x")
        }
      case x => throw new RuntimeException(s"Unknown POI_MSG type: $x")
    }
  }

  def getSpcXMLMessageNode(node:Node):MessageNode= {
    MessageNode.fromXml(node)
  }


  def addNode(to: Node, newNode: Node):Node = to match {
    case Elem(prefix, label, attributes, scope, child@_*) => Elem(prefix, label, attributes, scope, true, child ++ newNode: _*)
    case _ => println("could not find node"); to
  }
}
