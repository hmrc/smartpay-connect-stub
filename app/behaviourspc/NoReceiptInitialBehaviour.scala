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

package behaviourspc

import flow.InitialBehaviour
import models.InteractionCategories.{CardReader, OnlineCategory}
import models.TranResults.SuccessResult
import models._

class NoReceiptInitialBehaviour(flowData: SpcFlowDataNoReceipt) extends InitialBehaviour {

  val initialBehaviour: SpcBehaviour = handlePedLogOn

  private lazy val handlePedLogOn: SpcBehaviour = behave {
    case pedLogOn: PedLogOn =>

      val pedLogOnResponse: SpcResponseMessage = PedLogOnResponse(HeaderNode(), pedLogOn.messageNode, SuccessResult, ErrorsNode(Seq.empty))
      (
        List(pedLogOnResponse),
        handleSubmitPayment orElse CommonBehaviours.handlePedLogOff
      )
  }

  private def handleSubmitPayment: SpcBehaviour = behave {
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
      val interimMessages = flowData.displayMessagesValidation.map{
        case (interactionEvents, interactionPrompt) =>
          val interactionNode = InteractionNode(category = CardReader, event = interactionEvents, prompt = interactionPrompt)
          val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(), processTransaction.messageNode, interactionNode, SuccessResult, ErrorsNode(Seq.empty))
          posDisplayMessageInsertCard
      }

      //UpdatePaymentEnhanced
      val amountNode = AmountNode(submittedData.totalAmount, submittedData.currency, submittedData.country, None)
      val transactionNode = TransactionNode(amountNode = amountNode)
      val cardNode = UpeCardNode(flowData.paymentCard)
      val updatePaymentEnhanced = UpdatePaymentEnhanced(HeaderNode(), processTransaction.messageNode, transactionNode, cardNode, SuccessResult, ErrorsNode(Seq.empty))
      (interimMessages :+[SpcResponseMessage] updatePaymentEnhanced, handleUpdatePaymentEnhancedResponse(submittedData) orElse handleTransactionCancelled(submittedData))
  }

  private def handleUpdatePaymentEnhancedResponse(submittedData: SubmittedData): SpcBehaviour = behave {
    case updatePaymentEnhancedResponse: UpdatePaymentEnhancedResponse =>
      val finalAmount = updatePaymentEnhancedResponse.amountNode.finalAmountO
      val totalAmount = updatePaymentEnhancedResponse.amountNode.totalAmount

      //Display sequence - card Authentication
      val interimMessages = flowData.displayMessagesAuthentication.map{
        case (interactionEvents, interactionPrompt) =>
          val interactionNode = InteractionNode(category = OnlineCategory, event = interactionEvents, prompt = interactionPrompt)
          val posDisplayMessageInsertCard = PosDisplayMessage(HeaderNode(), updatePaymentEnhancedResponse.messageNode, interactionNode, SuccessResult, ErrorsNode(Seq.empty))
          posDisplayMessageInsertCard
      }

      //processTransactionResponse
      val amountNode = AmountNode(totalAmount, submittedData.currency, submittedData.country, finalAmount)

      val ptrTransactionNode = PtrTransactionNode(
        amountNode      = amountNode,
        verification    = flowData.cardVerificationMethod,
        transactionDate = StubUtil.formatTransactionDate(submittedData.transactionDateTime),
        transactionTime = StubUtil.formatTransactionTime(submittedData.transactionDateTime))
      val cardNode = PtrResponseCardNode(flowData.paymentCard)

      val processTransactionResponse = ProcessTransactionResponse(
        headerNode           = HeaderNode(),
        messageNode          = updatePaymentEnhancedResponse.messageNode,
        ptrTransactionNode   = ptrTransactionNode,
        ptrCardNode          = cardNode,
        result               = flowData.transactionResult,
        paymentResult        = flowData.paymentResult,
        receiptNodeCustomerO = None,
        receiptNodeMerchantO = None,
        errorsNode           = ErrorsNode(Seq.empty))
      (interimMessages :+[SpcResponseMessage] processTransactionResponse, CommonBehaviours.handleFinalise)
  }

  private def handleTransactionCancelled(submittedData: SubmittedData): SpcBehaviour = behave {
    case cancelTransaction: CancelTransaction =>

      //processTransactionResponse
      val amountNode = AmountNode(submittedData.totalAmount, submittedData.currency, submittedData.country, None)

      val ptrTransactionNode = PtrTransactionNode(
        amountNode      = amountNode,
        verification    = flowData.cardVerificationMethod,
        transactionDate = StubUtil.formatTransactionDate(submittedData.transactionDateTime),
        transactionTime = StubUtil.formatTransactionTime(submittedData.transactionDateTime))
      val cardNode = PtrResponseCardNode(flowData.paymentCard)

      val processTransactionResponse = ProcessTransactionResponse(
        headerNode           = HeaderNode(),
        messageNode          = cancelTransaction.messageNode,
        ptrTransactionNode   = ptrTransactionNode,
        ptrCardNode          = cardNode,
        result               = flowData.transactionResult,
        paymentResult        = PaymentResults.cancelled,
        receiptNodeCustomerO = None,
        receiptNodeMerchantO = None,
        errorsNode           = ErrorsNode(Seq.empty))

      (List(processTransactionResponse), CommonBehaviours.handleFinalise)
  }

}
