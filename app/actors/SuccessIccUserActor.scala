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

package actors

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import models.{AmountInPence, AmountNode, CancelTransaction, CardNode, CompleteTransaction, Country, Currency, ErrorMessage, ErrorsNode, F2FMessage, Finalise, FinaliseResponse, HeaderNode, InteractionNode, PaymentResults, PdTransNode, PedLogOff, PedLogOffResponse, PedLogOn, PedLogOnResponse, PosDecisionMessage, PosDisplayMessage, PosPrintReceipt, PosPrintReceiptResponse, ProcessTransaction, ProcessTransactionResponse, PtrCardNode, PtrTransactionNode, ReceiptNode, ReceiptTypes, Results, ScpState, SpcRequestMessage, SpcResponseMessage, SpcXmlHelper, StubTestData, SubmitPayment, SubmitPaymentResponse, Timeout, TransactionActions, TransactionCustomers, TransactionDecisions, TransactionNode, TransactionSources, TransactionTypes, UpdatePaymentEnhanced, UpdatePaymentEnhancedResponse}
import play.api.Logger
import models.InteractionCategories.{CardReader, OnlineCategory}
import models.InteractionEvents.{EventSuccess, InProgress, Processing, UseChip}
import models.InteractionPrompts.{ConnectingToAcquirer, InsertCard, ProcessingTransaction}
import models.Results.SuccessResult

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.xml.Elem

/**
* Scp - Smart Pay Connect  - class that hold actor/payment state data
* @param totalAmount  tax amount to be payed with transaction
* @param finalAmount  totalAmount plus card/transaction fee(s)
*/

object SuccessIccdUserActor {
  def props():Props = Props(new SuccessIccdUserActor())
}


class SuccessIccdUserActor extends Actor {
  import SpcParentActor._
  var state:ScpState = ScpState(AmountInPence.zero, StubTestData.VisaCredit, None, Country.Uk, Currency.Gbp, TransactionSources.Icc)
  var schedule:Cancellable = startCountDown()
  implicit val ec:ExecutionContextExecutor = context.dispatcher


  def startCountDown(): Cancellable = {
    context.system.scheduler.scheduleOnce(15.second, self, Timeout)
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
      val posDisplayMessageInProgress = ErrorMessage(HeaderNode(),unexpected.messageNode, ErrorsNode(Seq(StubTestData.incorrectMessageFlowErrorNode)), SuccessResult)
      sendScpReplyMessage(out,posDisplayMessageInProgress)

    case x => logger.error(s"Unknown SmartPay Connect message: $x")
  }

  def handlePedLogOn: Receive =  {
    case SpcWSMessage(out, session, pedLogOn:PedLogOn) =>
      logger.debug(s"User Actor $self got SpcMessage PedLogOn message $pedLogOn")
      state.copy(totalAmount = AmountInPence.zero, finalAmount = None)

      val pedLogOnResponse:SpcResponseMessage = PedLogOnResponse(HeaderNode(),pedLogOn.messageNode, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,pedLogOnResponse)

      context.become(handleSubmitPayment orElse handleScpMessages)
      context.stop(session)
  }

  def handleSubmitPayment: Receive = {
    case SpcWSMessage(out, session,submitPayment: SubmitPayment) =>
      logger.debug(s"User Actor $self got SpcMessage SubmitPayment message $submitPayment")
      state.copy(totalAmount = submitPayment.transactionNode.amountNode.totalAmount,
        currency = submitPayment.transactionNode.amountNode.currency,
        country = submitPayment.transactionNode.amountNode.country,
        source = submitPayment.transactionNode.transactionSourceO.getOrElse(TransactionSources.Icc))

      val submitPaymentResponse = SubmitPaymentResponse(HeaderNode(),submitPayment.messageNode, SuccessResult)
      sendScpReplyMessage(out,submitPaymentResponse)

      context.become(handleProcessTransaction orElse handleScpMessages)
      context.stop(session)

  }

  def handleProcessTransaction: Receive = {
    case SpcWSMessage(out, session,processTransaction: ProcessTransaction) =>
      logger.debug(s"User Actor $self got SpcMessage processTransaction message $processTransaction")

      val interactionNodeInsertCard = InteractionNode(category = CardReader, event = UseChip, prompt = InsertCard)
      val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(),processTransaction.messageNode, interactionNodeInsertCard, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,posDisplayMessageInsertCard)

      val interactionNodeInProgress = InteractionNode(category = CardReader, event = InProgress, prompt = ConnectingToAcquirer)
      val posDisplayMessageInProgress = PosDisplayMessage(HeaderNode(),processTransaction.messageNode, interactionNodeInProgress, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,posDisplayMessageInProgress)

      val interactionNodeProcessing = InteractionNode(category = CardReader, event = Processing, prompt = ProcessingTransaction)
      val posDisplayMessageProcessing = PosDisplayMessage(HeaderNode(), processTransaction.messageNode, interactionNodeProcessing, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,posDisplayMessageProcessing)


      val amountNode = AmountNode(state.totalAmount, state.currency, state.country, state.finalAmount)
      val transactionNode = TransactionNode(amountNode,Some(TransactionActions.AuthorizeAndSettle), Some(TransactionTypes.Purchase), Some(state.source), Some(TransactionCustomers.Present))
      val visaCardNode = CardNode(state.paymentCard.currency, state.paymentCard.country, state.paymentCard.endDate, state.paymentCard.startDate, state.paymentCard.pan, state.paymentCard.cardType)
      val updatePaymentEnhanced = UpdatePaymentEnhanced(HeaderNode(), processTransaction.messageNode, transactionNode, visaCardNode, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,updatePaymentEnhanced)

      context.become(handleUpdatePaymentEnhancedResponse orElse handleScpMessages)
      context.stop(session)
  }

  def handleUpdatePaymentEnhancedResponse: Receive = {
    case SpcWSMessage(out, session,updatePaymentEnhancedResponse: UpdatePaymentEnhancedResponse) if updatePaymentEnhancedResponse.amountNode.currency == state.currency && updatePaymentEnhancedResponse.amountNode.country == state.country =>
      logger.debug(s"User Actor $self got SpcMessage updatePaymentEnhancedResponse message $updatePaymentEnhancedResponse")
      state.copy(finalAmount = updatePaymentEnhancedResponse.amountNode.finalAmountO, totalAmount = updatePaymentEnhancedResponse.amountNode.totalAmount)

      val interactionNodeInProgress = InteractionNode(category = OnlineCategory, event = InProgress, prompt = ConnectingToAcquirer)
      val posDisplayMessageInProgress = PosDisplayMessage(HeaderNode(),updatePaymentEnhancedResponse.messageNode, interactionNodeInProgress, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,posDisplayMessageInProgress)

      val interactionNodeSuccess = InteractionNode(category = OnlineCategory, event = EventSuccess, prompt = ProcessingTransaction)
      val posDisplayMessageSuccess = PosDisplayMessage(HeaderNode(),updatePaymentEnhancedResponse.messageNode, interactionNodeSuccess, SuccessResult, ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,posDisplayMessageSuccess)

      val posPrintReceipt = PosPrintReceipt(HeaderNode(), updatePaymentEnhancedResponse.messageNode, ReceiptNode(ReceiptTypes.MerchantSignatureReceipt, StubTestData.securityReceipt ), SuccessResult,ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,posPrintReceipt)

      context.become(handlePosPrintReceiptResponse orElse handleScpMessages)
      context.stop(session)

    case SpcWSMessage(out, session,cancelTransaction: CancelTransaction) =>

      val amountNode = AmountNode(state.totalAmount, state.paymentCard.currency, state.paymentCard.country, state.finalAmount)
      val ptrTransactionNode = PtrTransactionNode(amountNode,Some(TransactionActions.AuthorizeAndSettle), Some(TransactionTypes.Purchase), Some(state.source), Some(TransactionCustomers.Present),Some(StubTestData.transactionReference))
      val visaPtrCardNode = PtrCardNode(state.paymentCard.currency, state.paymentCard.country, state.paymentCard.endDate, state.paymentCard.startDate, state.paymentCard.pan, state.paymentCard.cardType)

      val processTransactionResponse = ProcessTransactionResponse(HeaderNode(), messageNode = cancelTransaction.messageNode, ptrTransactionNode = ptrTransactionNode, ptrCardNode = visaPtrCardNode, result = Results.SuccessResult, paymentResult = PaymentResults.cancelled, receiptNodeCustomerO = None, receiptNodeMerchantO = None, errorsNode = ErrorsNode(Seq.empty))
      sendScpReplyMessage(out,processTransactionResponse)


      context.become(handleFinalise orElse handleScpMessages)
      context.stop(session)
  }


  def handlePosPrintReceiptResponse: Receive = {
    case SpcWSMessage(out,session,posPrintReceiptResponse: PosPrintReceiptResponse) =>
      logger.debug(s"User Actor $self got SpcMessage posPrintReceiptResponse message $posPrintReceiptResponse")

      val amountNode = AmountNode(state.totalAmount, state.paymentCard.currency, state.paymentCard.country, state.finalAmount)
      val ptrTransactionNode = PtrTransactionNode(amountNode,Some(TransactionActions.AuthorizeAndSettle), Some(TransactionTypes.Purchase), Some(state.source), Some(TransactionCustomers.Present),Some(StubTestData.transactionReference))
      val visaPtrCardNode = PtrCardNode(state.paymentCard.currency, state.paymentCard.country, state.paymentCard.endDate, state.paymentCard.startDate, state.paymentCard.pan, state.paymentCard.cardType)
      val customerReceiptNode = ReceiptNode(ReceiptTypes.CustomerReceipt, StubTestData.customerDuplicateReceipt )
      val merchantReceiptNode = ReceiptNode(ReceiptTypes.MerchantSignatureReceipt, StubTestData.securityReceipt )

      val processTransactionResponse = ProcessTransactionResponse(headerNode = HeaderNode(), messageNode = posPrintReceiptResponse.messageNode, ptrTransactionNode = ptrTransactionNode, ptrCardNode = visaPtrCardNode, result = Results.SuccessResult, paymentResult = PaymentResults.OnlineResult, receiptNodeCustomerO = Some(customerReceiptNode), receiptNodeMerchantO = Some(merchantReceiptNode), errorsNode = ErrorsNode(Seq.empty))
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

  lazy val logger:Logger = Logger(SuccessIccdUserActor.getClass)

  private def sendScpReplyMessage(out:ActorRef, spcResponseMessage: SpcResponseMessage) = {
    out ! spcResponseMessage.toXml.toString
    logger.debug(s"User Actor $self Reply $spcResponseMessage")
    Thread.sleep(500)
  }

}
