/*
 * Copyright 2024 HM Revenue & Customs
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

package flow

import models.InteractionCategories.{CardReader, OnlineCategory}
import models.TranResults.SuccessResult
import models._

class FallBackFlow(spcFlow: FlowData) extends Flow {

  val initialBehaviour: SpcBehaviour = handlePedLogOn

  private lazy val handlePedLogOn: SpcBehaviour = behave {
    case getTerminalDetails: GetTerminalDetails =>
      (
        List(GetTerminalDetailsResponse(HeaderNode(), getTerminalDetails.messageNode, SuccessResult, ErrorsNode(Seq.empty))),
        handlePedLogOn orElse handleSubmitPayment orElse CommonBehaviours.handlePedLogOff
      )
    case pedLogOn: PedLogOn =>
      val pedLogOnResponse: SpcResponseMessage = PedLogOnResponse(HeaderNode(), pedLogOn.messageNode, SuccessResult, ErrorsNode(Seq.empty))
      (List(pedLogOnResponse), handleSubmitPayment orElse CommonBehaviours.handlePedLogOff)
  }

  private lazy val handleSubmitPayment: SpcBehaviour = behave {
    case submitPayment: SubmitPayment =>
      val paymentSubmittedData = SubmittedData(
        totalAmount         = submitPayment.transactionNode.amountNode.totalAmount,
        currency            = submitPayment.transactionNode.amountNode.currency,
        country             = submitPayment.transactionNode.amountNode.country,
        transactionNumber   = submitPayment.messageNode.transNum,
        transactionDateTime = StubUtil.getCurrentDateTime
      )

      val submitPaymentResponse = SubmitPaymentResponse(HeaderNode(), submitPayment.messageNode, SuccessResult)
      (List(submitPaymentResponse), handleProcessTransaction(paymentSubmittedData))
  }

  //sends UpdatePaymentEnhanced
  private def handleProcessTransaction(submittedData: SubmittedData): SpcBehaviour = behave {
    case processTransaction: ProcessTransaction =>

      //Display sequence - card validation
      val interimResponses = spcFlow.displayMessagesValidation.map{
        case (interactionEvents, interactionPrompt) =>
          val interactionNode = InteractionNode(category = CardReader, event = interactionEvents, prompt = interactionPrompt)
          val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(), processTransaction.messageNode, interactionNode, SuccessResult, ErrorsNode(Seq.empty))
          posDisplayMessageInsertCard
      }

      //UpdatePaymentEnhanced
      val amountNode = AmountNode(submittedData.totalAmount, submittedData.currency, submittedData.country, None)
      val transactionNode = TransactionNode(amountNode = amountNode)
      val cardNode = UpeCardNode(spcFlow.paymentCard)
      val updatePaymentEnhanced = UpdatePaymentEnhanced(HeaderNode(), processTransaction.messageNode, transactionNode, cardNode, SuccessResult, ErrorsNode(Seq.empty))
      (
        interimResponses :+[SpcResponseMessage] updatePaymentEnhanced,
        handleUpdatePaymentEnhancedResponse(submittedData) orElse handleTransactionCancelled(submittedData) orElse handleGetTransactionDetails(submittedData)
      )
  }

  private def handleUpdatePaymentEnhancedResponse(submittedData: SubmittedData): SpcBehaviour = behave {
    case updatePaymentEnhancedResponse: UpdatePaymentEnhancedResponse =>
      val finalAmount = updatePaymentEnhancedResponse.amountNode.finalAmountO
      val totalAmount = updatePaymentEnhancedResponse.amountNode.totalAmount

      //Display sequence - card Authentication
      val interimResponses = spcFlow.displayMessagesAuthentication.map {
        case (interactionEvents, interactionPrompt) =>
          val interactionNode = InteractionNode(category = OnlineCategory, event = interactionEvents, prompt = interactionPrompt)
          val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(), updatePaymentEnhancedResponse.messageNode, interactionNode, SuccessResult, ErrorsNode(Seq.empty))
          posDisplayMessageInsertCard
      }

      //PosPrintReceipt client
      val merchantReceiptNode = ReceiptMerchantNode(spcFlow, submittedData, totalAmount, finalAmount)
      val posPrintReceipt = PosPrintReceipt(HeaderNode(), updatePaymentEnhancedResponse.messageNode, merchantReceiptNode, SuccessResult, ErrorsNode(Seq.empty))

      val posDecisionTransNode = PdTransNode(TransactionDecisions.SignatureRequired)
      val posDecisionMessage = PosDecisionMessage(HeaderNode(), updatePaymentEnhancedResponse.messageNode, posDecisionTransNode)
      (
        interimResponses :+[SpcResponseMessage] posPrintReceipt :+ posDecisionMessage,
        handleTransactionCancelled(submittedData) orElse handleGetTransactionDetails(submittedData)
      )
  }

  private def handleTransactionCancelled(submittedData: SubmittedData): SpcBehaviour = behave {
    case cancelTransaction: CancelTransaction =>

      //processTransactionResponse
      val amountNode = AmountNode(submittedData.totalAmount, submittedData.currency, submittedData.country, None)

      val ptrTransactionNode = PtrTransactionNode(
        amountNode      = amountNode,
        verification    = spcFlow.cardVerificationMethod,
        transactionDate = StubUtil.formatTransactionDate(submittedData.transactionDateTime),
        transactionTime = StubUtil.formatTransactionTime(submittedData.transactionDateTime))
      val cardNode = PtrResponseCardNode(spcFlow.paymentCard)

      val processTransactionResponse = ProcessTransactionResponse(
        headerNode           = HeaderNode(),
        messageNode          = cancelTransaction.messageNode,
        ptrTransactionNode   = ptrTransactionNode,
        ptrCardNode          = cardNode,
        result               = spcFlow.transactionResult,
        paymentResult        = PaymentResults.cancelled,
        receiptNodeCustomerO = None,
        receiptNodeMerchantO = None,
        errorsNode           = ErrorsNode(Seq.empty))
      (List(processTransactionResponse), CommonBehaviours.handleFinalise)
  }

  private def handleGetTransactionDetails(submittedData: SubmittedData): SpcBehaviour = behave {
    case getTransactionDetails: GetTransactionDetails =>
      //processTransactionResponse
      val amountNode = AmountNode(submittedData.totalAmount, submittedData.currency, submittedData.country, None)

      val ptrTransactionNode = PtrTransactionNode(
        amountNode      = amountNode,
        verification    = spcFlow.cardVerificationMethod,
        transactionDate = StubUtil.formatTransactionDate(submittedData.transactionDateTime),
        transactionTime = StubUtil.formatTransactionTime(submittedData.transactionDateTime))
      val cardNode = PtrResponseCardNode(spcFlow.paymentCard)

      val processTransactionResponse = GetTransactionDetailsResponse(
        headerNode           = HeaderNode(),
        messageNode          = getTransactionDetails.messageNode,
        ptrTransactionNode   = ptrTransactionNode,
        ptrCardNode          = cardNode,
        result               = spcFlow.transactionResult,
        paymentResult        = PaymentResults.cancelled,
        receiptNodeCustomerO = None,
        receiptNodeMerchantO = None,
        errorsNode           = ErrorsNode(Seq.empty))
      (List(processTransactionResponse), CommonBehaviours.handleFinalise)
  }

}
