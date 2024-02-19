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

package scenario

import enumeratum.Enum
import julienrf.json.derived
import play.api.libs.json.OFormat

import scala.collection.immutable

sealed trait Scenario extends enumeratum.EnumEntry {
  def value: String
  def description: String
}

object Scenario extends Enum[Scenario] {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[Scenario] = derived.oformat[Scenario]()

  val default: Scenario = SuccessChipAndPin

  final case object SuccessChipAndPin extends Scenario { val value = "success_chip_and_pin"; val description = "Success chip & pin with all data on receipt" }
  final case object SuccessChipAndPinMulti extends Scenario { val value = "success_chip_and_pin_multi_card"; val description = "Success chip & pin with no sequenceNumber on receipt" }

  //FlowType1
  final case object SuccessNoVerification extends Scenario { val value = "success_no_verification"; val description = "Success no verification card with all data on receipt" }
  final case object SuccessNoReceipt extends Scenario { val value = "success_no_receipt"; val description = "Success chip & pin with no receipt." }
  //FlowType8, ReceiptType 5
  //  final case object SuccessContactlessEMV extends StubPath { val value = "success_contactless_EMV"; val description = "Success contactlessEMV card with no startDate on receipt" }
  //  //FlowType8, ReceiptType 5
  //  final case object SuccessNoVerificationPreAuth2 extends StubPath { val value = "success_no_verification_pre_auth2"; val description = "Success no chip & pin card with no authCode, startDate on receipt" }
  //  //FlowType4, ReceiptType 8
  //  final case object SuccessContactlessEMV3 extends StubPath { val value = "success_contactless_EMV3"; val description = "Success contactlessEMV card with availableSpent additionally on receipt" }

  //FlowType5 ReceiptType3
  final case object DeclinedNotAuthorisedNotVerified extends Scenario { val value = "declined_no_verification_no_authorisation"; val description = "Declined/Not Authorised for chip & pin card with authCode missing on receipt" }
  final case object DeclinedNotAuthorisedNotVerified2 extends Scenario { val value = "declined_no_verification_no_authorisation2"; val description = "Declined/Not Authorised for chip & pin card with authCode and startDate missing on receipt" }

  //FlowType7 ReceiptType4
  final case object DeclinedInvalidCard extends Scenario { val value = "declined_invalid_card"; val description = "Declined/Not Authorised invalid card with terminalId missing on receipt" }
  //FlowType7 ReceiptType9
  final case object DeclinedInvalidCard2 extends Scenario { val value = "declined_invalid_card2"; val description = "Declined/Not Authorised invalid card with authCode, terminalId, seqNumber missing on receipt" }

  final case object DeclinedValidationFailed extends Scenario { val value = "declined_validation_failed"; val description = "Declined/No Validation with availableSpent and startDate missing on receipt" }
  final case object DeclinedBinCheckFailed extends Scenario { val value = "declined_bin_check_failed"; val description = "Declined/Bin check failed" }
  final case object DeclinedNoReceipt extends Scenario { val value = "declined_no_receipt"; val description = "Declined no receipt" }
  final case object DeclinedPedDisconnected extends Scenario { val value = "declined_ped_disconnected"; val description = "Declined PED disconnected" }

  final case object FallbackPosDecision extends Scenario { val value = "fallback_pos_decision"; val description = "Fallback after posDecision" }

  //  final case object CardDeclinedIcc extends StubPath { val value =  "card_declined_icc" ; val description = "Chip&Pin path with card declined by card provider"}
  final case object CancelledOnPedIcc extends Scenario { val value = "cancelled_ped_icc"; val description = "Chip&Pin path with transaction cancelled by user on ped" }
  final case object CancelledByBarclaycard extends Scenario { val value = "cancelled_by_barclaycard"; val description = "Chip&Pin path with transaction cancelled by Barclaycard" }
  //  final case object IncorrectPinIcc extends StubPath { val value =  "incorrect_pin_icc" ; val description = "Chip&Pin path with incorrect PIN and card removed from PED"}

  final case object SuccessChipAndPinMasterCard extends Scenario { val value = "success_chip_and_pin_mastercard"; val description = "Success chip & pin with different card" }
  final case object SuccessNoMerchantNumberInReceipt extends Scenario { val value = "success_no_merchant_number_in_receipt"; val description = "Success no merchant number in receipt" }

  override def values: immutable.IndexedSeq[Scenario] = findValues
}
