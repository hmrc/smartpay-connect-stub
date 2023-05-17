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

import scenario.Scenario._
import models._
import scenario.Scenario

object SpcFlows {

  def getFlow(scenario: Scenario): Flow = scenario match {
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

  private val successChipAndPinFlow = new StandardFlow(FlowData(
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
  ))
  private val successChipAndPinMultiFlow = new StandardFlow(FlowData(
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
  ))
  private val successNoVerificationFlow = new StandardFlow(FlowData(
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
  ))
  private val successNoReceiptFlow = new NoReceiptFlow(FlowDataNoReceipt(
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
  ))
  private val declinedNotAuthorisedNotVerifiedFlow = new StandardFlow(FlowData(
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
  ))
  private val declinedNotAuthorisedNotVerified2Flow = new StandardFlow(FlowData(
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
  ))
  private val declinedValidationFailedFlow = new NoSurchargeFlow(FlowData(
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
  ), ErrorsNode(Seq(ErrorNode("100007", "Validation of card has failed"))))
  private val declinedPedDisconnectedFlow = new PedDisconnectedFlow(FlowDataNoReceipt(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.declined,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq.empty,
    displayMessagesAuthentication = Seq.empty
  ), ErrorsNode(Seq(ErrorNode("200001", "Terminal Communication Failure"))))
  private val declinedBinCheckFailedFlow = new BinCheckCardDiscardedFlow(FlowData(
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
  ))
  private val declinedInvalidCard2Flow = new StandardFlow(FlowData(
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
  ))
  private val declinedNoReceiptFlow = new NoReceiptFlow(FlowDataNoReceipt(
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
  ))
  private val fallbackPosDecisionFlow = new FallBackFlow(FlowData(
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
  ))
  private val cancelledOnPedIccFlow = new StandardFlow(FlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptTypeEmpty,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq.empty[(InteractionEvent, InteractionPrompt)],
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  ))
  private val cancelledByBarclaycardFlow = new StandardFlow(FlowData(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptTypeBroken,
    transactionResult             = TranResults.FailureResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq[(InteractionEvent, InteractionPrompt)]((InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)),
    displayMessagesAuthentication = Seq[(InteractionEvent, InteractionPrompt)]((InteractionEvents.Processing, InteractionPrompts.ConnectingToAcquirer), (InteractionEvents.StartedEvent, InteractionPrompts.PinIncorrect))
  ))

  private val declinedInvalidCardFlow = new StandardFlow(FlowData(
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
  ))

}
