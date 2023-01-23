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

package actors

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import models.InteractionCategories.{CardReader, OnlineCategory}
import models.TranResults.SuccessResult
import models._
import play.api.Logger

import scala.concurrent.ExecutionContextExecutor

/**
* Scp - Smart Pay Connect  - class that hold actor/payment state data
* @param totalAmount  tax amount to be payed with transaction
* @param finalAmount  totalAmount plus card/transaction fee(s)
*/

object StandardMessageFlowUserActor {
  def props(spcFlow:SpcFlow):Props = Props(new StandardMessageFlowUserActor(spcFlow))
}


class StandardMessageFlowUserActor(spcFlow:SpcFlow) extends Actor {
  import SpcParentActor._

  var schedule:Cancellable = startCountDown()
  implicit val ec:ExecutionContextExecutor = context.dispatcher


  def startCountDown(): Cancellable = {
    context.system.scheduler.scheduleOnce(CountDown.value, self, Timeout)
  }

  override def postStop(): Unit = {
    logger.debug(s"Stopping User Actor ${self.path}")
    super.postStop()
  }


  override def preStart(): Unit = {
    logger.debug(s"Starting User Actor ${self.path}")
    super.preStart()
  }

  def receive: Receive = handlePedLogOn orElse handleScpMessages

  def handleScpMessages: Receive = {
    case SpcWSXmlMessage(out, session, xmlMsg) =>
      logger.debug(s"User Actor $self got XML message $xmlMsg")
      schedule.cancel()
      schedule = startCountDown()
      SpcXmlHelper.getSpcXmlMessage(xmlMsg) match {
        case Some(scpXmlMessage) =>
          self ! SpcWSMessage(out, session, scpXmlMessage)
        case None =>
          logger.error(s"User Actor received unknown message")
          context.stop(self)
      }

    case Timeout =>
      logger.error(s"User Actor $self timeout. Closing itself")
      context.stop(self)
    case SpcWSMessage(out, session, unexpected:SpcRequestMessage) => logger.error(s"Unexpected SmartPay Connect message: $unexpected")
      //TODO - check actually what is teh error code
      val errorNode = ErrorNode("XXXXXX", s"Unexpected message [${unexpected.name}] for selected stub flow")
      val errorsNode = ErrorsNode(Seq(errorNode))
      val errorResponse = ErrorMessage(HeaderNode(), unexpected.messageNode, errorsNode, SuccessResult)
      sendScpReplyMessage(out, errorResponse)
      context.stop(self)

    case x =>
      logger.error(s"Unknown SmartPay Connect message: $x")
      context.stop(self)
  }

  def handlePedLogOn: Receive =  {
    case SpcWSMessage(out, session, pedLogOn:PedLogOn) =>
      logger.debug(s"User Actor $self got SpcMessage PedLogOn message $pedLogOn")

      val pedLogOnResponse:SpcResponseMessage = PedLogOnResponse(HeaderNode(),pedLogOn.messageNode, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,pedLogOnResponse)

      context.become(handleSubmitPayment orElse handleScpMessages)
      context.stop(session)
  }

  def handleSubmitPayment: Receive = {
    case SpcWSMessage(out, session,submitPayment: SubmitPayment) =>
      logger.debug(s"User Actor $self got SpcMessage SubmitPayment message $submitPayment")
      val paymentSubmittedData = SubmittedData(
        totalAmount = submitPayment.transactionNode.amountNode.totalAmount,
        currency = submitPayment.transactionNode.amountNode.currency,
        country = submitPayment.transactionNode.amountNode.country,
        transactionNumber = submitPayment.messageNode.transNum,
        transactionDateTime = StubUtil.getCurrentDateTime
      )

        val submitPaymentResponse = SubmitPaymentResponse(HeaderNode(), submitPayment.messageNode, SuccessResult)
        sendScpReplyMessage(out, submitPaymentResponse)

        context.become(handleProcessTransaction(paymentSubmittedData) orElse handleScpMessages)

      context.stop(session)

  }

  //sends UpdatePaymentEnhanced
  def handleProcessTransaction(submittedData: SubmittedData): Receive = {

    case SpcWSMessage(out, session,processTransaction: ProcessTransaction) =>
      logger.debug(s"User Actor $self got SpcMessage processTransaction message $processTransaction")

      //Display sequence - card validation
      spcFlow.displayMessagesValidation.foreach{
        case (interactionEvents, interactionPrompt) =>
          val interactionNode = InteractionNode(category = CardReader, event = interactionEvents, prompt = interactionPrompt)
          val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(),processTransaction.messageNode, interactionNode, SuccessResult, ErrorsNode(Seq.empty))
          sendScpReplyMessage(out,posDisplayMessageInsertCard)
      }

      //UpdatePaymentEnhanced
      val amountNode = AmountNode(submittedData.totalAmount,submittedData.currency, submittedData.country, None)
      val transactionNode = TransactionNode(amountNode = amountNode)
      val cardNode = UpeCardNode(spcFlow.paymentCard)
      val updatePaymentEnhanced = UpdatePaymentEnhanced(HeaderNode(), processTransaction.messageNode, transactionNode, cardNode, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,updatePaymentEnhanced)

      context.become(handleUpdatePaymentEnhancedResponse(submittedData) orElse handleScpMessages)
      context.stop(session)
  }

  def handleUpdatePaymentEnhancedResponse(submittedData: SubmittedData): Receive = {
    case SpcWSMessage(out, session,updatePaymentEnhancedResponse: UpdatePaymentEnhancedResponse) =>
      logger.debug(s"User Actor $self got SpcMessage updatePaymentEnhancedResponse message $updatePaymentEnhancedResponse")
      val finalAmount = updatePaymentEnhancedResponse.amountNode.finalAmountO
      val totalAmount = updatePaymentEnhancedResponse.amountNode.totalAmount

      //Display sequence - card Authentication
      spcFlow.displayMessagesAuthentication.foreach{
        case (interactionEvents, interactionPrompt) =>
          val interactionNode = InteractionNode(category = OnlineCategory, event = interactionEvents, prompt = interactionPrompt)
          val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(),updatePaymentEnhancedResponse.messageNode, interactionNode, SuccessResult, ErrorsNode(Seq.empty))
          sendScpReplyMessage(out,posDisplayMessageInsertCard)
      }

      //PosPrintReceipt client
      val merchantReceiptNode = ReceiptMerchantNode(spcFlow, submittedData, totalAmount, finalAmount)
      val posPrintReceipt = PosPrintReceipt(HeaderNode(), updatePaymentEnhancedResponse.messageNode, merchantReceiptNode, SuccessResult,ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,posPrintReceipt)

      context.become(handlePosPrintReceiptResponse(submittedData, totalAmount, finalAmount, merchantReceiptNode ) orElse handleScpMessages)
      context.stop(session)
  }

  def handlePosPrintReceiptResponse(submittedData: SubmittedData, totalAmount: AmountInPence, finalAmount: Option[AmountInPence], merchantReceiptNode:ReceiptNode): Receive = {
    case SpcWSMessage(out,session,posPrintReceiptResponse: PosPrintReceiptResponse) =>
      logger.debug(s"User Actor $self got SpcMessage posPrintReceiptResponse message $posPrintReceiptResponse")


      //PosPrintReceipt client
      val clientReceiptNode = ReceiptNode.createReceiptNode(submittedData, spcFlow, totalAmount, finalAmount)

      val posPrintReceipt = PosPrintReceipt(HeaderNode(), posPrintReceiptResponse.messageNode, clientReceiptNode, SuccessResult,ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,posPrintReceipt)

      context.become(handlePosPrintReceiptResponseWithPtr(submittedData, totalAmount, finalAmount, merchantReceiptNode, clientReceiptNode ) orElse handleScpMessages)
      context.stop(session)

  }


  def handlePosPrintReceiptResponseWithPtr(submittedData: SubmittedData,  totalAmount: AmountInPence,finalAmount: Option[AmountInPence], merchantReceiptNode:ReceiptNode, clientReceiptNode:ReceiptNode): Receive = {
    case SpcWSMessage(out,session,posPrintReceiptResponse: PosPrintReceiptResponse) =>
      logger.debug(s"User Actor $self got SpcMessage posPrintReceiptResponse message $posPrintReceiptResponse")

      //processTransactionResponse
      val amountNode = AmountNode(totalAmount, submittedData.currency, submittedData.country, finalAmount)

      val ptrTransactionNode = PtrTransactionNode(
        amountNode = amountNode,
        verification = spcFlow.cardVerificationMethod,
        transactionDate = StubUtil.formatTransactionDate(submittedData.transactionDateTime),
        transactionTime = StubUtil.formatTransactionTime(submittedData.transactionDateTime))
      val cardNode = PtrResponseCardNode(spcFlow.paymentCard)

      val processTransactionResponse = ProcessTransactionResponse(
        headerNode = HeaderNode(),
        messageNode = posPrintReceiptResponse.messageNode,
        ptrTransactionNode = ptrTransactionNode,
        ptrCardNode = cardNode,
        result = spcFlow.transactionResult,
        paymentResult =spcFlow.paymentResult,
        receiptNodeCustomerO = Some(clientReceiptNode),
        receiptNodeMerchantO = Some(merchantReceiptNode),
        errorsNode = ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,processTransactionResponse)

      context.become(handleFinalise orElse handleScpMessages)
      context.stop(session)

  }

  def handleFinalise: Receive = {
    case SpcWSMessage(out, session,finalise: Finalise) =>
      logger.debug(s"User Actor $self got SpcMessage finalise message $finalise")

      val finaliseResponse = FinaliseResponse(HeaderNode(), finalise.messageNode, SuccessResult)
      sendScpReplyMessage(out,finaliseResponse)

      context.become(handlePedLogOff orElse handleScpMessages)
      context.stop(session)
  }

  def handlePedLogOff: Receive = {
    case SpcWSMessage(out, session,pedLogOff: PedLogOff) =>
      logger.debug(s"User Actor $self got SpcMessage pedLogOff message $pedLogOff")

      val pedLogOffResponse = PedLogOffResponse(HeaderNode(), pedLogOff.messageNode, SuccessResult)
      sendScpReplyMessage(out,pedLogOffResponse)

      context.stop(session)
      context.stop(self)
  }

  lazy val logger:Logger = Logger(StandardMessageFlowUserActor.getClass)

  private def sendScpReplyMessage(out:ActorRef, spcResponseMessage: SpcResponseMessage) = {
    out ! spcResponseMessage.toXml.toString
    logger.debug(s"User Actor $self Reply $spcResponseMessage")
    Thread.sleep(500)
  }

}
