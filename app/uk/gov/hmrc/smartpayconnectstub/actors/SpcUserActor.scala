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
import uk.gov.hmrc.smartpayconnectstub.models.InteractionCategories.{CardReader, OnlineCategory}
import uk.gov.hmrc.smartpayconnectstub.models.InteractionEvents.{EventSuccess, InProgress, UseChip}
import uk.gov.hmrc.smartpayconnectstub.models.InteractionPrompts.{ConnectingToAcquirer, InsertCard, ProcessingTransaction}
import uk.gov.hmrc.smartpayconnectstub.models.PaymentResults.OnlineResult
import uk.gov.hmrc.smartpayconnectstub.models.Results.SuccessResult
import uk.gov.hmrc.smartpayconnectstub.models._

import scala.concurrent.ExecutionContextExecutor
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
  def props():Props = Props(new ScpUserActor())

  case class SpcWSXmlMessage(out: ActorRef, msg:Elem)
  case class SpcWSMessage(out: ActorRef, msg:F2FMessage)
}


class ScpUserActor extends Actor {
  import ScpUserActor._
  var state:ScpState = ScpState(AmountInPence.zero, None)
  var schedule:Cancellable = startCountDown()
  implicit val ec:ExecutionContextExecutor = context.dispatcher


  def startCountDown(): Cancellable = {
    context.system.scheduler.scheduleOnce(10.second, self, Timeout)
  }

  override def postStop(): Unit = {
    logger.info(s"Stopping User Actor!!!!! ${self.path}")
    super.postStop()
  }


  override def preStart(): Unit = {
    logger.info(s"Starting User Actor!!!!! ${self.path}")
    super.preStart()
  }

  def receive: Receive = handlePedLogOn orElse handleScpMessages

  def handleScpMessages: Receive = {
    case SpcWSXmlMessage(out, xmlMsg) =>
      logger.info(s"User Actor $self got XML message $xmlMsg")
      schedule.cancel()
      schedule = startCountDown()
      val scpXmlMessage = SpcXmlHelper.getSpcXmlMessage(xmlMsg)
      self ! SpcWSMessage(out,scpXmlMessage)
    case Timeout =>
      logger.info(s"User Actor $self timeout. Closing itself")
      context.stop(self)
    case x => logger.error(s"Unknown/Unexpected SmartPay Connect message: $x")
  }

  def handlePedLogOn: Receive =  {
    case SpcWSMessage(out,pedLogOn:PedLogOn) =>
      logger.info(s"User Actor $self got SpcMessage PedLogOn message $pedLogOn")
      state.copy(totalAmount = AmountInPence.zero, finalAmount = None)
      val pedLogOnResponse: Node = PedLogOnResponse(HeaderNode(),pedLogOn.messageNode, SuccessResult, ErrorsNode(Seq.empty)).toXml
      logger.info(s"User Actor $self Reply $pedLogOnResponse")
      context.become(handleSubmitPayment orElse handleScpMessages)
      out ! pedLogOnResponse.toString
  }

  def handleSubmitPayment: Receive = {
    case SpcWSMessage(out,submitPayment: SubmitPayment) =>
      logger.info(s"User Actor $self got SpcMessage SubmitPayment message $submitPayment")
      state.copy(totalAmount = submitPayment.amountNode.totalAmount)
      val submitPaymentResponse: Node = SubmitPaymentResponse(HeaderNode(),submitPayment.messageNode, SuccessResult).toXml
      logger.info(s"User Actor $self Reply $submitPaymentResponse")
      out ! submitPaymentResponse.toString
      context.become(handleProcessTransaction orElse handleScpMessages)
  }

  def handleProcessTransaction: Receive = {
    case SpcWSMessage(out,processTransaction: ProcessTransaction) =>
      logger.info(s"User Actor $self got SpcMessage processTransaction message $processTransaction")
      val amountNode = AmountNode(state.totalAmount, state.finalAmount)

      val interactionNodeInsertCard = InteractionNode(category = CardReader, event = UseChip, prompt = InsertCard)
      val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(),processTransaction.messageNode, interactionNodeInsertCard, SuccessResult, ErrorsNode(Seq.empty)).toXml
      out ! posDisplayMessageInsertCard.toString
      logger.info(s"User Actor $self Reply $posDisplayMessageInsertCard")

      val interactionNodeConnecting = InteractionNode(category = OnlineCategory, event = InProgress, prompt = ConnectingToAcquirer)
      val posDisplayMessageConnecting = PosDisplayMessage(HeaderNode(),processTransaction.messageNode, interactionNodeConnecting, SuccessResult, ErrorsNode(Seq.empty)).toXml
      out ! posDisplayMessageConnecting.toString
      logger.info(s"User Actor $self Reply $posDisplayMessageConnecting")

      val interactionNodeProcessing = InteractionNode(category = OnlineCategory, event = EventSuccess, prompt = ProcessingTransaction)
      val posDisplayMessageProcessing = PosDisplayMessage(HeaderNode(), processTransaction.messageNode, interactionNodeProcessing, SuccessResult, ErrorsNode(Seq.empty)).toXml
      out ! posDisplayMessageProcessing.toString
      logger.info(s"User Actor $self Reply $posDisplayMessageProcessing")

      val cardNode = CardNode("2024-12-31", "417666******0019", "VISA CREDIT")
      val updatePaymentEnhanced = UpdatePaymentEnhanced(HeaderNode(), processTransaction.messageNode, amountNode, cardNode, SuccessResult, ErrorsNode(Seq.empty)).toXml
      logger.info(s"Reply $updatePaymentEnhanced")
      out ! updatePaymentEnhanced.toString
      context.become(handleUpdatePaymentEnhancedResponse orElse handleScpMessages)
  }

  def handleUpdatePaymentEnhancedResponse: Receive = {
    case SpcWSMessage(out,updatePaymentEnhancedResponse: UpdatePaymentEnhancedResponse) =>
      logger.info(s"User Actor $self got SpcMessage updatePaymentEnhancedResponse message $updatePaymentEnhancedResponse")
      state.copy(finalAmount = updatePaymentEnhancedResponse.amountNode.finalAmountO)
      val posPrintReceipt = PosPrintReceipt(HeaderNode(), updatePaymentEnhancedResponse.messageNode, ReceiptNode(ReceiptType("customer"), "" ), SuccessResult,ErrorsNode(Seq.empty)).toXml
      logger.info(s"User Actor $self Reply $posPrintReceipt")
      out ! posPrintReceipt.toString
      context.become(handlePosPrintReceiptResponse orElse handleScpMessages)
  }

  def handlePosPrintReceiptResponse: Receive = {
    case SpcWSMessage(out,posPrintReceiptResponse: PosPrintReceiptResponse) =>
      logger.info(s"User Actor $self got SpcMessage posPrintReceiptResponse message $posPrintReceiptResponse")
      val amountNode = AmountNode(state.totalAmount, state.finalAmount)
      val processTransactionResponse: Node = ProcessTransactionResponse(HeaderNode(), posPrintReceiptResponse.messageNode, amountNode, SuccessResult,OnlineResult, ErrorsNode(Seq.empty)).toXml
      logger.info(s"User Actor $self Reply $processTransactionResponse")
      out ! processTransactionResponse.toString
      context.become(handleFinalise orElse handleScpMessages)
  }

  def handleFinalise: Receive = {
    case SpcWSMessage(out,finalise: Finalise) =>
      logger.info(s"User Actor $self got SpcMessage finalise message $finalise")
      val finaliseResponse: Node = FinaliseResponse(HeaderNode(), finalise.messageNode, SuccessResult).toXml
      logger.info(s"User Actor $self Reply $finaliseResponse")
      out ! finaliseResponse.toString
      context.become(handlePedLogOff orElse handleScpMessages)
  }

  def handlePedLogOff: Receive = {
    case SpcWSMessage(out,pedLogOff: PedLogOff) =>
      logger.info(s"User Actor $self got SpcMessage pedLogOff message $pedLogOff")
      val pedLogOffResponse: Node = PedLogOffResponse(HeaderNode(), pedLogOff.messageNode, SuccessResult).toXml
      logger.info(s"User Actor $self Reply $pedLogOffResponse")
      out ! pedLogOffResponse.toString
      context.stop(self)
  }

  lazy val logger:Logger = Logger(ScpUserActor.getClass)

}
