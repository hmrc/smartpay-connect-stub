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

package models

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json._
import utils.{EnumFormat, JsonUtil}

import scala.collection.immutable


sealed trait StubPath extends EnumEntry {
  def value: String
  def description: String
}

object StubPath {
  import StubPaths._
  val format: Format[StubPath] = EnumFormat(StubPaths)
  implicit val oformat: OFormat[StubPath] = JsonUtil.oFormat(format)

  def apply(value: String): StubPath = {
    value match {
      case "success_chip_and_pin" => SuccessChipAndPin
      case "success_chip_and_pin_multi_card" => SuccessChipAndPinMulti


      case "success_no_verification" => SuccessNoVerification
      case "success_empty_receipt" => SuccessEmptyReceipt
      case "success_broken_receipt" => SuccessBrokenReceipt
      case "success_no_receipt" => SuccessNoReceipt
//      case "success_contactless_EMV" => SuccessContactlessEMV
//      case "success_no_verification_pre_auth2" => SuccessNoVerificationPreAuth2
//      case "success_contactless_EMV3" => SuccessContactlessEMV3

      case "declined_no_verification_no_authorisation" => DeclinedNotAuthorisedNotVerified
      case "declined_no_verification_no_authorisation2" => DeclinedNotAuthorisedNotVerified2
      case "declined_invalid_card" => DeclinedInvalidCard
      case "declined_invalid_card2" => DeclinedInvalidCard2

      case "declined_validation_failed" => DeclinedValidationFailed
      case "declined_bin_check_failed" => DeclinedBinCheckFailed
      case "declined_no_receipt" => DeclinedNoReceipt


//      case "card_declined_icc"   => CardDeclinedIcc
//      case "cancelled_ped_icc" => CancelledOnPedIcc
//      case "incorrect_pin_icc" => IncorrectPinIcc
      case x             => throw new RuntimeException(s"Unknown StubPath: $x")
    }
  }
}

object StubPaths extends Enum[StubPath] {
  final case object SuccessChipAndPin extends StubPath { val value = "success_chip_and_pin"; val description = "Success chip & pin with all data on receipt" }
  final case object SuccessChipAndPinMulti extends StubPath { val value = "success_chip_and_pin_multi_card"; val description = "Success chip & pin with no sequenceNumber on receipt" }

  //FlowType1
  final case object SuccessNoVerification extends StubPath { val value = "success_no_verification"; val description = "Success no verification card with all data on receipt" }
  final case object SuccessEmptyReceipt extends StubPath { val value = "success_empty_receipt"; val description = "Success chip & pin with empty receipt. (Not real example but F2F should handle that)" }
  final case object SuccessBrokenReceipt extends StubPath { val value = "success_broken_receipt"; val description = "Success chip & pin with broken receipt. (Not real example but F2F should handle that)" }
  final case object SuccessNoReceipt extends StubPath { val value = "success_no_receipt"; val description = "Success chip & pin with no receipt." }
  //FlowType8, ReceiptType 5
//  final case object SuccessContactlessEMV extends StubPath { val value = "success_contactless_EMV"; val description = "Success contactlessEMV card with no startDate on receipt" }
//  //FlowType8, ReceiptType 5
//  final case object SuccessNoVerificationPreAuth2 extends StubPath { val value = "success_no_verification_pre_auth2"; val description = "Success no chip & pin card with no authCode, startDate on receipt" }
//  //FlowType4, ReceiptType 8
//  final case object SuccessContactlessEMV3 extends StubPath { val value = "success_contactless_EMV3"; val description = "Success contactlessEMV card with availableSpent additionally on receipt" }

  //FlowType5 ReceiptType3
  final case object DeclinedNotAuthorisedNotVerified extends StubPath { val value =  "declined_no_verification_no_authorisation" ; val description = "Declined/Not Authorised for chip & pin card with authCode missing on receipt"}
  final case object DeclinedNotAuthorisedNotVerified2 extends StubPath { val value =  "declined_no_verification_no_authorisation2" ; val description = "Declined/Not Authorised for chip & pin card with authCode and startDate missing on receipt"}


  //FlowType7 ReceiptType4
  final case object DeclinedInvalidCard extends StubPath { val value =  "declined_invalid_card" ; val description = "Declined/Not Authorised invalid card with terminalId missing on receipt"}
  //FlowType7 ReceiptType9
  final case object DeclinedInvalidCard2 extends StubPath { val value =  "declined_invalid_card2" ; val description = "Declined/Not Authorised invalid card with authCode, terminalId, seqNumber missing on receipt"}


  final case object DeclinedValidationFailed extends StubPath { val value =  "declined_validation_failed" ; val description = "*Declined/No Validation with availableSpent and startDate missing on receipt"}
  final case object DeclinedBinCheckFailed extends StubPath { val value =  "declined_bin_check_failed" ; val description = "Declined/Bin check failed"}
  final case object DeclinedNoReceipt extends StubPath { val value = "declined_no_receipt"; val description = "Declined no receipt" }


//  final case object CardDeclinedIcc extends StubPath { val value =  "card_declined_icc" ; val description = "Chip&Pin path with card declined by card provider"}
//  final case object CancelledOnPedIcc extends StubPath { val value =  "cancelled_ped_icc" ; val description = "Chip&Pin path with transaction cancelled by user on ped"}
//  final case object IncorrectPinIcc extends StubPath { val value =  "incorrect_pin_icc" ; val description = "Chip&Pin path with incorrect PIN and card removed from PED"}
  override def values: immutable.IndexedSeq[StubPath] = findValues
}