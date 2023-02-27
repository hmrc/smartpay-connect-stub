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

import behaviour.Behaviour.{B, behave, done}
import flow.MessageFlow
import models.TranResults.SuccessResult
import models._

class PedDisconnectedMessageFlowUserActor(spcFlow: SpcFlowDataNoReceipt, errorsNode: ErrorsNode) extends MessageFlow {

  val initialBehaviour: B = handlePedLogOn

  private lazy val handlePedLogOn: B = behave {
    case pedLogOn: PedLogOn =>

      val pedLogOnResponse: SpcResponseMessage = PedLogOnResponse(HeaderNode(), pedLogOn.messageNode, SuccessResult, ErrorsNode(Seq.empty))
      (List(
        pedLogOnResponse),
        handleSubmitPayment orElse handlePedLogOff
      )
  }

  private lazy val handleSubmitPayment: B = behave {
    case submitPayment: SubmitPayment =>
      val paymentSubmittedData = SubmittedData(
        totalAmount         = submitPayment.transactionNode.amountNode.totalAmount,
        currency            = submitPayment.transactionNode.amountNode.currency,
        country             = submitPayment.transactionNode.amountNode.country,
        transactionNumber   = submitPayment.messageNode.transNum,
        transactionDateTime = StubUtil.getCurrentDateTime
      )

      val submitPaymentResponse = SubmitPaymentResponse(HeaderNode(), submitPayment.messageNode, SuccessResult)
      (
        List(submitPaymentResponse),
        handleProcessTransaction(paymentSubmittedData) orElse handleTransactionCancelled(paymentSubmittedData)
      )
  }

  //Do not send updatePaymentEnhanced but got to print message instead
  private def handleProcessTransaction(submittedData: SubmittedData): B = behave {

    case processTransaction: ProcessTransaction =>
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
        messageNode          = processTransaction.messageNode,
        ptrTransactionNode   = ptrTransactionNode,
        ptrCardNode          = cardNode,
        result               = spcFlow.transactionResult,
        paymentResult        = spcFlow.paymentResult,
        receiptNodeCustomerO = None,
        receiptNodeMerchantO = None,
        errorsNode           = errorsNode)
      (
        List(processTransactionResponse),
        handleFinalise
      )
  }

  private def handleTransactionCancelled(submittedData: SubmittedData): B = behave {
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
      (
        List(processTransactionResponse),
        handleFinalise
      )
  }

  private lazy val handleFinalise: B = behave {
    case finalise: Finalise =>

      val finaliseResponse = FinaliseResponse(HeaderNode(), finalise.messageNode, SuccessResult)
      (
        List(finaliseResponse),
        handlePedLogOff
      )
  }

  private lazy val handlePedLogOff: B = behave {
    case pedLogOff: PedLogOff =>
      val pedLogOffResponse = PedLogOffResponse(HeaderNode(), pedLogOff.messageNode, SuccessResult)
      (
        List(pedLogOffResponse),
        done
      )

  }
}
