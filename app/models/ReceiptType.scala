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

package models

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format
import utils.EnumFormat

import scala.collection.immutable

/**
 * SPC- Smart Pay Connect - Interaction Node categories
 */
sealed trait ReceiptType extends EnumEntry {
  val receiptType: String
  val description: String
}

object ReceiptType {
  import ReceiptTypes._
  implicit val format: Format[ReceiptType] = EnumFormat(ReceiptTypes)

  def apply(receiptType: String): ReceiptType = {
    receiptType match {
      case "merchant"           => MerchantReceipt
      case "merchant_signature" => MerchantSignatureReceipt
      case "customer"           => CustomerReceipt
      case "pos"                => PosReceipt
      case x                    => throw new RuntimeException(s"Unknown TransactionDecision: $x")
    }
  }
}

object ReceiptTypes extends Enum[ReceiptType] {
  final case object MerchantReceipt extends ReceiptType {
    override val receiptType: String = "merchant"
    override val description: String = "Transaction receipt for the merchant."
  }

  final case object MerchantSignatureReceipt extends ReceiptType {
    override val receiptType: String = "merchant_signature"
    override val description: String = "Manual authorisation is required."
  }

  final case object CustomerReceipt extends ReceiptType {
    override val receiptType: String = "customer"
    override val description: String = "Transaction receipt for the customer."
  }

  final case object PosReceipt extends ReceiptType {
    override val receiptType: String = "pos"
    override val description: String = "The PoS has constructed the receipt/invoice and wishes to print it on the PED."
  }

  override def values: immutable.IndexedSeq[ReceiptType] = findValues
}

