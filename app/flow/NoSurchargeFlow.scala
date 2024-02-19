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

import models.InteractionCategories.CardReader
import models.TranResults.SuccessResult
import models._

class NoSurchargeFlow(spcFlow: FlowData, errorsNode: ErrorsNode) extends Flow {

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

  private lazy val  handleSubmitPayment: SpcBehaviour = behave {
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

  //Do not send updatePaymentEnhanced but got to print message instead
  private def handleProcessTransaction(submittedData: SubmittedData): SpcBehaviour = behave {

    case processTransaction: ProcessTransaction =>

      //Display sequence - card validation
      val interimMessages = spcFlow.displayMessagesValidation.map{
        case (interactionEvents, interactionPrompt) =>
          val interactionNode = InteractionNode(category = CardReader, event = interactionEvents, prompt = interactionPrompt)
          val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(), processTransaction.messageNode, interactionNode, SuccessResult, ErrorsNode(Seq.empty))
          posDisplayMessageInsertCard
      }

      //PosPrintReceipt client
      val merchantReceiptNode = ReceiptMerchantNode(spcFlow, submittedData, submittedData.totalAmount, None)
      val posPrintReceipt: PosPrintReceipt = PosPrintReceipt(HeaderNode(), processTransaction.messageNode, merchantReceiptNode, SuccessResult, ErrorsNode(Seq.empty))
      (
        interimMessages :+[SpcResponseMessage] posPrintReceipt,
        handlePosPrintReceiptResponse(submittedData, None, merchantReceiptNode) orElse handleTransactionCancelled(submittedData)
      )
  }

  private def handlePosPrintReceiptResponse(submittedData: SubmittedData, finalAmount: Option[AmountInPence], merchantReceiptNode: ReceiptNode): SpcBehaviour = behave {
    case posPrintReceiptResponse: PosPrintReceiptResponse =>

      //PosPrintReceipt client
      val clientReceiptNode = ReceiptNode.createReceiptNode(submittedData, spcFlow, submittedData.totalAmount, finalAmount)

      val posPrintReceipt = PosPrintReceipt(HeaderNode(), posPrintReceiptResponse.messageNode, clientReceiptNode, SuccessResult, ErrorsNode(Seq.empty))
      (
        List(posPrintReceipt),
        handlePosPrintReceiptResponseWithPtr(submittedData, finalAmount, merchantReceiptNode, clientReceiptNode)
      )

  }

  private def handlePosPrintReceiptResponseWithPtr(submittedData: SubmittedData, finalAmount: Option[AmountInPence], merchantReceiptNode: ReceiptNode, clientReceiptNode: ReceiptNode): SpcBehaviour = behave {
    case posPrintReceiptResponse: PosPrintReceiptResponse =>
      //processTransactionResponse
      val amountNode = AmountNode(submittedData.totalAmount, submittedData.currency, submittedData.country, finalAmount)

      val ptrTransactionNode = PtrTransactionNode(
        amountNode      = amountNode,
        verification    = spcFlow.cardVerificationMethod,
        transactionDate = StubUtil.formatTransactionDate(submittedData.transactionDateTime),
        transactionTime = StubUtil.formatTransactionTime(submittedData.transactionDateTime))
      val cardNode = PtrResponseCardNode(spcFlow.paymentCard)

      val processTransactionResponse = ProcessTransactionResponse(
        headerNode           = HeaderNode(),
        messageNode          = posPrintReceiptResponse.messageNode,
        ptrTransactionNode   = ptrTransactionNode,
        ptrCardNode          = cardNode,
        result               = spcFlow.transactionResult,
        paymentResult        = spcFlow.paymentResult,
        receiptNodeCustomerO = Some(clientReceiptNode),
        receiptNodeMerchantO = Some(merchantReceiptNode),
        errorsNode           = errorsNode)
      (List(processTransactionResponse), CommonBehaviours.handleFinalise)
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


}
