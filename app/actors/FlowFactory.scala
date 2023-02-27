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

import flow.MessageFlow
import scenario.Scenario._
import models._
import scenario.Scenario

object FlowFactory {

  def makeFlow(scenario: Scenario): MessageFlow = {
    scenario match {
      case Scenario.SuccessChipAndPin        => successChipAndPinFlow
      case Scenario.SuccessChipAndPinMulti   => successChipAndPinMultiFlow
      case Scenario.SuccessNoVerification    => successNoVerificationFlow
      case SuccessNoReceipt                  => successNoReceiptFlow
      case DeclinedNotAuthorisedNotVerified  => declinedNotAuthorisedNotVerifiedFlow
      case DeclinedInvalidCard               => declinedInvalidCardFlow
      case DeclinedNotAuthorisedNotVerified2 => declinedNotAuthorisedNotVerified2Flow
      case DeclinedValidationFailed          => declinedValidationFailedFlow
      case DeclinedPedDisconnected           => declinedPedDisconnectedFlow
      case DeclinedBinCheckFailed            => declinedBinCheckFailedFlow
      case DeclinedInvalidCard2              => declinedInvalidCard2Flow
      case DeclinedNoReceipt                 => declinedNoReceiptFlow
      case FallbackPosDecision               => fallbackPosDecisionFlow
      case CancelledOnPedIcc                 => cancelledOnPedIccFlow
      case CancelledByBarclaycard            => cancelledByBarclaycardFlow
    }
  }

  private val successChipAndPinFlow = new StandardMessageFlow(FlowData.successChipAndPinFlowData)
  private val successChipAndPinMultiFlow = new StandardMessageFlow(FlowData.successChipAndPinMultiFlowData)
  private val successNoVerificationFlow = new StandardMessageFlow(FlowData.successNoVerificationFlow)
  private val successNoReceiptFlow = new NoReceiptMessageFlow(FlowData.successNoReceiptFlow)
  private val declinedNotAuthorisedNotVerifiedFlow = new StandardMessageFlow(FlowData.declinedNotAuthorisedNotVerifiedFlow)
  private val declinedNotAuthorisedNotVerified2Flow = new StandardMessageFlow(FlowData.declinedNotAuthorisedNotVerified2Flow)
  private val declinedValidationFailedFlow = new NoSurchargeMessageFlowUserActor(FlowData.declinedValidationFailedFlow, ErrorsNode(Seq(ErrorNode("100007", "Validation of card has failed"))))
  private val declinedPedDisconnectedFlow = new PedDisconnectedMessageFlowUserActor(FlowData.declinedPedDisconnected, ErrorsNode(Seq(ErrorNode("200001", "Terminal Communication Failure"))))
  private val declinedBinCheckFailedFlow = new BinCheckCardDiscardedFlowUserActor(FlowData.declinedBinCheckFailedFlow)
  private val declinedInvalidCard2Flow = new StandardMessageFlow(FlowData.declinedInvalidCard2)
  private val declinedNoReceiptFlow = new NoReceiptMessageFlow(FlowData.declinedNoReceiptFlow)
  private val fallbackPosDecisionFlow = new FallBackFlowUserActor(FlowData.fallbackPosDecisionFlow)
  private val cancelledOnPedIccFlow = new StandardMessageFlow(FlowData.cancelledOnPedIccFlow)
  private val cancelledByBarclaycardFlow = new StandardMessageFlow(FlowData.cancelledByBarclaycardFlow)
  private val declinedInvalidCardFlow = new StandardMessageFlow(FlowData.declinedInvalidCardFlow)
}

object FlowData {

  val successChipAndPinFlowData: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.OnlineResult,
    receiptNodeName               = ReceiptTypeName.ReceiptType1Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq(
      (InteractionEvents.StartedEvent, InteractionPrompts.CustomerEnterPin),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.InProgress, InteractionPrompts.ConnectingToAcquirer),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
    )
  )

  val successChipAndPinMultiFlowData: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.OnlineResult,
    receiptNodeName               = ReceiptTypeName.ReceiptType2Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.UseChip, InteractionPrompts.InsertOrSwipeCard),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.InProgress, InteractionPrompts.SelectAppOnPed),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq(
      (InteractionEvents.StartedEvent, InteractionPrompts.CustomerEnterPin),
      (InteractionEvents.FailedRetry, InteractionPrompts.PinIncorrect),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.InProgress, InteractionPrompts.ConnectingToAcquirer),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction)
    )
  )

  val successNoVerificationFlow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.OnlineResult,
    receiptNodeName               = ReceiptTypeName.ReceiptType1Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq((InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)),
    displayMessagesAuthentication = Seq(
      (InteractionEvents.Processing, InteractionPrompts.ConnectingToAcquirer),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction)
    )
  )

  val successNoReceiptFlow: SpcFlowDataNoReceipt = SpcFlowDataNoReceipt(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.OnlineResult,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.UseChip, InteractionPrompts.InsertCardInChipReader),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq(
      (InteractionEvents.StartedEvent, InteractionPrompts.CustomerEnterPin),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.InProgress, InteractionPrompts.ConnectingToAcquirer),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
    )
  )

  val declinedNotAuthorisedNotVerifiedFlow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.declined,
    receiptNodeName               = ReceiptTypeName.ReceiptType3Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.UseChip, InteractionPrompts.InsertOrSwipeCard),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq(
      (InteractionEvents.InProgress, InteractionPrompts.ConnectingToAcquirer),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
    )
  )

  val declinedInvalidCardFlow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.declined,
    receiptNodeName               = ReceiptTypeName.ReceiptType4Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.unknown,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  )

  val declinedNotAuthorisedNotVerified2Flow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.declined,
    receiptNodeName               = ReceiptTypeName.ReceiptType6Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.UseChip, InteractionPrompts.InsertOrSwipeCard),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  )

  val declinedValidationFailedFlow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.declined,
    receiptNodeName               = ReceiptTypeName.ReceiptType7Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.UseChip, InteractionPrompts.InsertOrSwipeCard),
      (InteractionEvents.InProgress, InteractionPrompts.ConnectingToAcquirer),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.Fallforward, InteractionPrompts.InsertCardInChipReader)
    ),
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  )

  val declinedPedDisconnected: SpcFlowDataNoReceipt = SpcFlowDataNoReceipt(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.declined,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq.empty,
    displayMessagesAuthentication = Seq.empty
  )

  val declinedBinCheckFailedFlow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit_BinCheckFail,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptType1Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  )

  val declinedInvalidCard2: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.declined,
    receiptNodeName               = ReceiptTypeName.ReceiptType9Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.unknown,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  )

  val declinedNoReceiptFlow: SpcFlowDataNoReceipt = SpcFlowDataNoReceipt(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.declined,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.UseChip, InteractionPrompts.InsertCardInChipReader),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  )

  val fallbackPosDecisionFlow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptType1Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq(
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)
    ),
    displayMessagesAuthentication = Seq(
      (InteractionEvents.StartedEvent, InteractionPrompts.CustomerEnterPin),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
      (InteractionEvents.InProgress, InteractionPrompts.ConnectingToAcquirer),
      (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction),
    )
  )

  val cancelledOnPedIccFlow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptTypeEmpty,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq.empty[(InteractionEvent, InteractionPrompt)],
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  )

  val cancelledByBarclaycardFlow: SpcFlowData = SpcFlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptTypeBroken,
    transactionResult             = TranResults.FailureResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq[(InteractionEvent, InteractionPrompt)]((InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)),
    displayMessagesAuthentication = Seq[(InteractionEvent, InteractionPrompt)]((InteractionEvents.Processing, InteractionPrompts.ConnectingToAcquirer), (InteractionEvents.StartedEvent, InteractionPrompts.PinIncorrect))
  )
}
