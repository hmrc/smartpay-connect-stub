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

package uk.gov.hmrc.smartpayconnectstub.actors

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import play.api.Logger
import uk.gov.hmrc.smartpayconnectstub.models.models.AmountInPence
import uk.gov.hmrc.smartpayconnectstub.models.{AmountNode, CardNode, ConnectingToAcquirer, EventSuccess, Finalise, FinaliseResponse, InsertCard, InteractionNode, OnlineResult, PedLogOff, PedLogOffResponse, PedLogOn, PedLogOnResponse, PosDisplayMessage, PosPrintReceipt, PosPrintReceiptResponse, ProcessTransaction, ProcessTransactionResponse, ProcessingTransaction, SpcXmlHelper, SubmitPayment, SubmitPaymentResponse, Success, TransNum, UpdatePaymentEnhanced, UpdatePaymentEnhancedResponse, card_reader, in_progress, OnlineCategory, use_chip}

import scala.concurrent.duration._
import scala.xml.{Elem, Node}

/**
* Scp - Smart Pay Connect  - class that hold actor/payment state data
* @param totalAmount  tax amount to be payed with transaction
* @param finalAmount  totalAmount plus card/transaction fee(s)
*/
case class ScpState(totalAmount: AmountInPence, finalAmount:Option[AmountInPence])
case object Timeout

object ScpUserActor {
  def props(out: ActorRef, transNum: TransNum) = Props(new ScpUserActor(out, transNum))
}


class ScpUserActor(out: ActorRef, transNum: TransNum) extends Actor {
  var state = ScpState(AmountInPence.zero, None)
  var schedule = startCountDown()
  implicit val ec = context.dispatcher


  def startCountDown(): Cancellable = {
    context.system.scheduler.scheduleOnce(10 second, self, Timeout)
  }

  override def postStop(): Unit = super.postStop()

  def receive: Receive = handlePedLogOn

  def handleScpMessages: Receive = {
    case xmlMsg: Elem =>
      logger.info(s"TransNum:$transNum Got message $xmlMsg")
      schedule.cancel()
      schedule = startCountDown()
      val scpXmlMessage = SpcXmlHelper.getSpcXmlMessage(xmlMsg)
      self ! scpXmlMessage
    case Timeout =>
      logger.info(s"TransNum:$transNum timeout. Closing itself")
      context.stop(self)
    case x => throw new RuntimeException(s"Unknown/Unexpected SmartPay Connect message: $x")
  }

  def handlePedLogOn: Receive = handleScpMessages orElse {
    case pedLogOn: PedLogOn =>
      state.copy(totalAmount = AmountInPence.zero, finalAmount = None)
      val pedLogOnResponse: Node = PedLogOnResponse(pedLogOn.messageNode, Success).toXml
      out ! pedLogOnResponse.toString
      logger.info(s"TransNum:$transNum Reply $pedLogOnResponse")
      context.become(handleSubmitPayment)
  }

  def handleSubmitPayment: Receive = handleScpMessages orElse {
    case submitPayment: SubmitPayment =>
      state.copy(totalAmount = submitPayment.amountNode.totalAmount)
      val submitPaymentResponse: Node = SubmitPaymentResponse(submitPayment.messageNode, Success).toXml
      out ! submitPaymentResponse.toString
      logger.info(s"TransNum:$transNum Reply $submitPaymentResponse")
      context.become(handleProcessTransaction)
  }

  def handleProcessTransaction: Receive = handleScpMessages orElse {
    case processTransaction: ProcessTransaction =>
      val amountNode = AmountNode(state.totalAmount, state.finalAmount)

      val interactionNodeInsertCard = InteractionNode(category = card_reader, event = use_chip, prompt = InsertCard)
      val posDisplayMessageInsertCard = PosDisplayMessage(processTransaction.messageNode, interactionNodeInsertCard)
      out ! posDisplayMessageInsertCard
      logger.info(s"TransNum:$transNum Reply $posDisplayMessageInsertCard")

      val interactionNodeConnecting = InteractionNode(category = OnlineCategory, event = in_progress, prompt = ConnectingToAcquirer)
      val posDisplayMessageConnecting = PosDisplayMessage(processTransaction.messageNode, interactionNodeConnecting)
      out ! posDisplayMessageConnecting
      logger.info(s"TransNum:$transNum Reply $posDisplayMessageConnecting")

      val interactionNodeProcessing = InteractionNode(category = OnlineCategory, event = EventSuccess, prompt = ProcessingTransaction)
      val posDisplayMessageProcessing = PosDisplayMessage(processTransaction.messageNode, interactionNodeProcessing)
      out ! posDisplayMessageProcessing
      logger.info(s"TransNum:$transNum Reply $posDisplayMessageProcessing")

      val cardNode = CardNode("2024-12-31", "417666******0019", "VISA CREDIT")
      val updatePaymentEnhanced = UpdatePaymentEnhanced(processTransaction.messageNode, amountNode, cardNode)
      out ! updatePaymentEnhanced
      logger.info(s"Reply $updatePaymentEnhanced")
      context.become(handleUpdatePaymentEnhancedResponse)
  }

  def handleUpdatePaymentEnhancedResponse: Receive = handleScpMessages orElse {
    case updatePaymentEnhancedResponse: UpdatePaymentEnhancedResponse =>
      state.copy(finalAmount = updatePaymentEnhancedResponse.amountNode.finalAmountO)
      val posPrintReceipt = PosPrintReceipt(updatePaymentEnhancedResponse.messageNode)
      out ! posPrintReceipt
      logger.info(s"TransNum:$transNum Reply $posPrintReceipt")
      context.become(handlePosPrintReceiptResponse)
  }

  def handlePosPrintReceiptResponse: Receive = handleScpMessages orElse {
    case posPrintReceiptResponse: PosPrintReceiptResponse =>
      val amountNode = AmountNode(state.totalAmount, state.finalAmount)
      val processTransactionResponse: Node = ProcessTransactionResponse(posPrintReceiptResponse.messageNode, amountNode, Success, OnlineResult).toXml
      out ! processTransactionResponse.toString
      logger.info(s"TransNum:$transNum Reply $processTransactionResponse")
      context.become(handleFinalise)
  }

  def handleFinalise: Receive = handleScpMessages orElse {
    case finalise: Finalise =>
      val finaliseResponse: Node = FinaliseResponse(finalise.messageNode, Success).toXml
      out ! finaliseResponse.toString
      logger.info(s"TransNum:$transNum Reply $finaliseResponse")
      context.become(handlePedLogOff)
  }

  def handlePedLogOff: Receive = handleScpMessages orElse {
    case pedLogOff: PedLogOff =>
      val pedLogOffResponse: Node = PedLogOffResponse(pedLogOff.messageNode, Success).toXml
      out ! pedLogOffResponse.toString
      logger.info(s"TransNum:$transNum Reply $pedLogOffResponse")
      context.stop(self)
  }

  lazy val logger = Logger(ScpUserActor.getClass)

}
