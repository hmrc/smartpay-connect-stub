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
import play.api.libs.json.Format
import utils.EnumFormat

import scala.collection.immutable

/**
 * SPC- Smart Pay Connect - Interaction Node prompts
 */
sealed trait InteractionPrompt extends EnumEntry

object InteractionPrompt {
  import InteractionPrompts._
  implicit val format: Format[InteractionPrompt] = EnumFormat(InteractionPrompts)

  def apply(value: String): InteractionPrompt = {
    value match {
      case "Customer To Insert Or Swipe Card" => InsertCard
      case "Connecting to Acquirer"           => ConnectingToAcquirer
      case "Processing Transaction"           => ProcessingTransaction
      case "Customer To Reinsert Card"        => CustomerReinsertCard
      case x                                  => throw new RuntimeException(s"Unknown InteractionPrompt: $x")
    }
  }
}

object InteractionPrompts extends Enum[InteractionPrompt] {
  final case object InsertCard extends InteractionPrompt { override def toString: String = "Customer To Insert Or Swipe Card" }
  final case object ConnectingToAcquirer extends InteractionPrompt { override def toString: String = "Connecting to Acquirer" }
  final case object ProcessingTransaction extends InteractionPrompt { override def toString: String = "Processing Transaction" }
  final case object CustomerReinsertCard extends InteractionPrompt { override def toString: String = "Customer To Reinsert Card" }

  override def values: immutable.IndexedSeq[InteractionPrompt] = findValues
}
