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

import akka.actor.{Actor, ActorRef, Props, Terminated}
import scenario.Scenario._
import models._
import play.api.Logger
import scenario.Scenario

import scala.xml.{Elem, XML}

object SpcParentActor {
  def props(): Props = Props(new SpcParentActor())

  final case class SpcWSStringMessage(out: ActorRef, msg: String, scenario: Scenario)
  final case class SpcWSXmlMessage(out: ActorRef, session: ActorRef, msg: Elem)
  final case class SpcWSMessage(out: ActorRef, session: ActorRef, msg: F2FMessage)
}

class SpcParentActor extends Actor {
  import SpcParentActor._

  override def postStop(): Unit = {
    logger.debug(s"Stopping Parent Actor $self")
    super.postStop()
  }

  override def preStart(): Unit = {
    logger.debug(s"Starting Parent Actor $self")
    super.preStart()
  }

  def receive: Receive = handleScpMessages(Map.empty[TransactionId, ActorRef])

  private def handleScpMessages(userActors: Map[TransactionId, ActorRef]): Receive = {
    case SpcWSStringMessage(out, request, stubPath) =>
      val session = context.sender()
      logger.debug(s"Parent Actor got message $request")
      logger.debug(s"Parent Actor userActors $userActors")
      val xmlMsg: Elem = XML.loadString(request)
      val transNum = SpcXmlHelper.getSpcXmlMessageNode(xmlMsg).transNum

      userActors.get(transNum).fold {
        logger.debug(s"Parent actor is going to create user actor with name ${transNum.value}")

        val actorRef = stubPath match {

          case SuccessChipAndPin =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.successChipAndPinFlow), s"${transNum.value}")

          case SuccessChipAndPinMulti =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.successChipAndPinMultiFlow), s"${transNum.value}")

          case SuccessNoVerification =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.successNoVerificationFlow), s"${transNum.value}")

          case SuccessNoReceipt =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(NoReceiptMessageFlowUserActor.props(SpcFlows.successNoReceiptFlow), s"${transNum.value}")

          //          case SuccessContactlessEMV =>
          //            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
          //            val spcFlow:SpcFlow = SpcFlow(
          //              paymentCard = StubUtil.VisaCredit,
          //              paymentResult = PaymentResults.OnlineResult,
          //              receiptNodeName = ReceiptTypeName.ReceiptType5Name,
          //              transactionResult = TranResults.SuccessResult,
          //              cardVerificationMethod = CardVerificationMethod.not_performed,
          //              transactionSource = TransactionSources.Icc,
          //              displayMessagesValidation = Seq(
          //                (InteractionEvents.UseChip,InteractionPrompts.InsertOrSwipeCard),
          //                (InteractionEvents.InProgress,InteractionPrompts.ConnectingToAcquirer),
          //                (InteractionEvents.EventSuccess,InteractionPrompts.ProcessingTransaction)
          //              ),
          //              displayMessagesAuthentication = Seq.empty[(InteractionEvent,InteractionPrompt)]
          //            )
          //            context.actorOf(StandardMessageFlowUserActor.props(spcFlow),s"${transNum.value}")

          //          case SuccessNoVerificationPreAuth2 =>
          //            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
          //            val spcFlow:SpcFlow = SpcFlow(
          //              paymentCard = StubUtil.VisaCredit,
          //              paymentResult = PaymentResults.OnlineResult,
          //              receiptNodeName = ReceiptTypeName.ReceiptType5Name,
          //              transactionResult = TranResults.SuccessResult,
          //              cardVerificationMethod = CardVerificationMethod.not_performed,
          //              transactionSource = TransactionSources.Icc,
          //              displayMessagesValidation = Seq(
          //                (InteractionEvents.UseChip,InteractionPrompts.InsertOrSwipeCard),
          //                (InteractionEvents.InProgress,InteractionPrompts.ConnectingToAcquirer),
          //                (InteractionEvents.EventSuccess,InteractionPrompts.ProcessingTransaction)
          //              ),
          //              displayMessagesAuthentication = Seq.empty[(InteractionEvent,InteractionPrompt)]
          //            )
          //            context.actorOf(StandardMessageFlowUserActor.props(spcFlow),s"${transNum.value}")

          case DeclinedNotAuthorisedNotVerified =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.declinedNotAuthorisedNotVerifiedFlow), s"${transNum.value}")

          case DeclinedInvalidCard =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.declinedInvalidCardFlow), s"${transNum.value}")

          case DeclinedNotAuthorisedNotVerified2 =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.declinedNotAuthorisedNotVerified2Flow), s"${transNum.value}")

          case DeclinedValidationFailed =>
            logger.debug(s"Parent actor is going to create NoSurchargeMessageFlowUserActor for stubPath:$stubPath")
            val errorsNode = ErrorsNode(Seq(ErrorNode("100007", "Validation of card has failed")))
            context.actorOf(NoSurchargeMessageFlowUserActor.props(SpcFlows.declinedValidationFailedFlow, errorsNode), s"${transNum.value}")

          case DeclinedPedDisconnected =>
            logger.debug(s"Parent actor is going to create PedDisconnectedMessageFlowUserActor for stubPath:$stubPath")
            val spcFlowNoReceipt = SpcFlowNoReceipt(
              paymentCard                   = StubUtil.VisaCredit,
              paymentResult                 = PaymentResults.declined,
              transactionResult             = TranResults.SuccessResult,
              cardVerificationMethod        = CardVerificationMethod.not_performed,
              transactionSource             = TransactionSources.Icc,
              displayMessagesValidation     = Seq.empty,
              displayMessagesAuthentication = Seq.empty
            )

            val errorsNode = ErrorsNode(Seq(ErrorNode("200001", "Terminal Communication Failure")))

            context.actorOf(PedDisconnectedMessageFlowUserActor.props(spcFlowNoReceipt, errorsNode), s"${transNum.value}")

          case DeclinedBinCheckFailed =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(BinCheckCardDiscardedFlowUserActor.props(SpcFlows.declinedBinCheckFailedFlow), s"${transNum.value}")

          //          case SuccessContactlessEMV3 =>
          //            logger.debug(s"Parent actor is going to create NoSurchargeMessageFlowUserActor for stubPath:$stubPath")
          //            val spcFlow:SpcFlow = SpcFlow(
          //              paymentCard = StubUtil.VisaCredit,
          //              paymentResult = PaymentResults.OnlineResult,
          //              receiptNodeName = ReceiptTypeName.ReceiptType8Name,
          //              transactionResult = TranResults.SuccessResult,
          //              cardVerificationMethod = CardVerificationMethod.not_performed,
          //              transactionSource = TransactionSources.Icc,
          //              displayMessagesValidation = Seq(
          //                (InteractionEvents.UseChip,InteractionPrompts.InsertOrSwipeCard),
          //                (InteractionEvents.InProgress,InteractionPrompts.ConnectingToAcquirer),
          //                (InteractionEvents.EventSuccess,InteractionPrompts.ProcessingTransaction)
          //              ),
          //              displayMessagesAuthentication = Seq.empty[(InteractionEvent,InteractionPrompt)]
          //            )
          //            context.actorOf(StandardMessageFlowUserActor.props(spcFlow),s"${transNum.value}")
          //

          case DeclinedInvalidCard2 =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.declinedInvalidCard2), s"${transNum.value}")

          case DeclinedNoReceipt =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(NoReceiptMessageFlowUserActor.props(SpcFlows.declinedNoReceiptFlow), s"${transNum.value}")

          case FallbackPosDecision =>
            logger.debug(s"Parent actor is going to create StandardMessageFlowUserActor for stubPath:$stubPath")
            context.actorOf(FallBackFlowUserActor.props(SpcFlows.fallbackPosDecisionFlow), s"${transNum.value}")

          case CancelledOnPedIcc =>
            logger.debug(s"Parent actor is going to create CancelledOnPedIcc for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.cancelledOnPedIccFlow), s"${transNum.value}")

          case CancelledByBarclaycard =>
            logger.debug(s"Parent actor is going to create CancelledByBarclaycard for stubPath:$stubPath")
            context.actorOf(StandardMessageFlowUserActor.props(SpcFlows.cancelledByBarclaycardFlow), s"${transNum.value}")

          case _ =>
            logger.debug(s"Parent actor is going to create Default for stubPath:$stubPath")
            val spcFlow: SpcFlow = SpcFlow(
              paymentCard                   = StubUtil.VisaCredit,
              paymentResult                 = PaymentResults.OnlineResult,
              receiptNodeName               = ReceiptTypeName.ReceiptType1Name,
              transactionResult             = TranResults.SuccessResult,
              cardVerificationMethod        = CardVerificationMethod.pin,
              transactionSource             = TransactionSources.Icc,
              displayMessagesValidation     = Seq((InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)),
              displayMessagesAuthentication = Seq((InteractionEvents.Processing, InteractionPrompts.ConnectingToAcquirer), (InteractionEvents.EventSuccess, InteractionPrompts.ProcessingTransaction))
            )
            context.actorOf(StandardMessageFlowUserActor.props(spcFlow), s"${transNum.value}")
        }

        //        val actorRef = context.actorOf(SuccessIccdUserActor.props(),s"${transNum.value}")
        logger.debug(s"Parent actor created new user Actor $actorRef")
        logger.debug(s"Parent Actor  Send XML message $request to:$actorRef for transactionId:${transNum}")
        context.watch(actorRef)
        val newUserActors = userActors + (transNum -> actorRef)
        logger.debug(s"Parent Actor newUserActors $newUserActors")
        actorRef ! SpcWSXmlMessage(out, session, xmlMsg)
        context.become(handleScpMessages(newUserActors))
      }{ actorRef =>
        logger.debug(s"Parent actor sending messages to existing Actor $actorRef")
        logger.debug(s"Parent Actor  Send XML message $request to:$actorRef for transactionId:${transNum}")
        actorRef ! SpcWSXmlMessage(out, session, xmlMsg)

      }
    case Terminated(ref) =>
      logger.error(s"Actor user has been terminated $ref")
      userActors.filter(x => x._2 == ref).keys.headOption.map { stoppedActor =>
        context.become(handleScpMessages(userActors - stoppedActor))
      }

    case e => logger.error(s"Scp Parent Actor not handled message: $e")
  }

  private lazy val logger = Logger(SpcParentActor.getClass)
}

object SpcFlows {

  val successChipAndPinFlow: SpcFlow = SpcFlow(
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

  val successChipAndPinMultiFlow: SpcFlow = SpcFlow(
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

  val successNoVerificationFlow: SpcFlow = SpcFlow(
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

  val successNoReceiptFlow: SpcFlowNoReceipt = SpcFlowNoReceipt(
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

  val declinedNotAuthorisedNotVerifiedFlow: SpcFlow = SpcFlow(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptTypeEmpty,
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

  val declinedInvalidCardFlow: SpcFlow = SpcFlow(
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

  val declinedNotAuthorisedNotVerified2Flow: SpcFlow = SpcFlow(
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

  val declinedValidationFailedFlow: SpcFlow = SpcFlow(
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

  val declinedBinCheckFailedFlow: SpcFlow = SpcFlow(
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

  val declinedInvalidCard2: SpcFlow = SpcFlow(
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

  val declinedNoReceiptFlow: SpcFlowNoReceipt = SpcFlowNoReceipt(
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

  val fallbackPosDecisionFlow: SpcFlow = SpcFlow(
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

  val cancelledOnPedIccFlow: SpcFlow = SpcFlow(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptType4Name,
    transactionResult             = TranResults.SuccessResult,
    cardVerificationMethod        = CardVerificationMethod.not_performed,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq.empty[(InteractionEvent, InteractionPrompt)],
    displayMessagesAuthentication = Seq.empty[(InteractionEvent, InteractionPrompt)]
  )

  val cancelledByBarclaycardFlow: SpcFlow = SpcFlow(
    paymentCard                   = StubUtil.VisaCredit,
    paymentResult                 = PaymentResults.cancelled,
    receiptNodeName               = ReceiptTypeName.ReceiptTypeEmpty,
    transactionResult             = TranResults.FailureResult,
    cardVerificationMethod        = CardVerificationMethod.pin,
    transactionSource             = TransactionSources.Icc,
    displayMessagesValidation     = Seq((InteractionEvents.Processing, InteractionPrompts.ProcessingTransaction)),
    displayMessagesAuthentication = Seq((InteractionEvents.Processing, InteractionPrompts.ConnectingToAcquirer), (InteractionEvents.StartedEvent, InteractionPrompts.PinIncorrect))
  )
}
