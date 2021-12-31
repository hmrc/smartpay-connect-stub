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

package uk.gov.hmrc.smartpayconnectstub.controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartpayconnectstub.controllers.scpState.{initScpState, setTotalAmount}
import uk.gov.hmrc.smartpayconnectstub.models.{AmountNode, CardNode, InteractionNode, PedLogOn, PedLogOnResponse, PosDisplayMessage, ProcessTransaction, ProcessTransactionResponse, SpcMessageHelper, SubmitPayment, SubmitPaymentResponse, UpdatePaymentEnhanced, UpdatePaymentEnhancedResponse}

import javax.inject.{Inject, Singleton}
import scala.xml.{Elem, Node, XML}

@Singleton()
class WebsocketController @Inject()(cc: ControllerComponents)(implicit mat: Materializer, actorSystem: ActorSystem)
    extends BackendController(cc) {

  def ws(): WebSocket = WebSocket.accept[String,String] { implicit request =>
    ActorFlow.actorRef { out =>
      WebSocketActor.props(out)
    }
  }
}

import akka.actor._
case class ScpState(totalAmount: Double, finalAmount:Option[Double])

object scpState {
  case object initScpState
  case class setTotalAmount(totalAmount: Double)
}

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
  val logger = Logger(WebSocketActor.getClass)
  var state = ScpState(0, None)

  def receive = handleSCPMessages orElse handleState

  def handleSCPMessages:Receive = {
    case request: String =>
      logger.info(s"Got message $request")
      val xmlMsg:Elem = XML.loadString(request)
      SpcMessageHelper.getSpcXMLMessage(xmlMsg) match {
        case pedLogOn:PedLogOn =>
          val pedLogOnResponse:Node = PedLogOnResponse(pedLogOn.messageNode,"success").toXML
          self ! initScpState
          out ! pedLogOnResponse.toString
          logger.info(s"Reply $pedLogOnResponse")
        case submitPayment:SubmitPayment =>
          val submitPaymentResponse:Node = SubmitPaymentResponse(submitPayment.messageNode,"success").toXML
          self ! setTotalAmount(submitPayment.amountNode.totalAmount)
          out ! submitPaymentResponse.toString
          logger.info(s"Reply $submitPaymentResponse")
        case processTransaction:ProcessTransaction =>
          val amountNode = AmountNode(state.totalAmount, None)
          val processTransactionResponse:Node = ProcessTransactionResponse(processTransaction.messageNode, amountNode,"success").toXML
          out ! processTransactionResponse.toString
          logger.info(s"Reply $processTransactionResponse")
          Thread.sleep(100)

          val interactionNodeInsertCard = InteractionNode("card_reader","use_chip", "Customer To Insert Or Swipe Card")
          val posDisplayMessageInsertCard = PosDisplayMessage(processTransaction.messageNode, interactionNodeInsertCard)
          out ! posDisplayMessageInsertCard
          logger.info(s"Reply $posDisplayMessageInsertCard")
          Thread.sleep(100)

          val interactionNodeConnecting = InteractionNode("online","in_progress", "Connecting to Acquirer")
          val posDisplayMessageConnecting = PosDisplayMessage(processTransaction.messageNode, interactionNodeConnecting)
          out ! posDisplayMessageConnecting
          logger.info(s"Reply $posDisplayMessageConnecting")
          Thread.sleep(100)

          val interactionNodeProcessing= InteractionNode("online","success", "Processing Transaction")
          val posDisplayMessageProcessing = PosDisplayMessage(processTransaction.messageNode, interactionNodeProcessing)
          out ! posDisplayMessageProcessing
          logger.info(s"Reply $posDisplayMessageProcessing")
          Thread.sleep(100)

          val cardNode = CardNode("2024-12-31", "417666******0019", "VISA CREDIT")
          val updatePaymentEnhanced = UpdatePaymentEnhanced(processTransaction.messageNode, amountNode, cardNode)
          out ! updatePaymentEnhanced
          logger.info(s"Reply $updatePaymentEnhanced")
        case updatePaymentEnhancedResponse:UpdatePaymentEnhancedResponse =>
          //TODO - print recept then finalize

        case x =>  throw new RuntimeException(s"Unknown SmartPay Connect message: $x")
      }

  }

  def handleState:Receive = {
    case initScpState =>
      state.copy(totalAmount = 0, finalAmount = None)
    case setTotalAmount(totalAmount) =>
      state.copy(totalAmount = totalAmount)
  }
}
