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

package models

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format
import utils.EnumFormat

import scala.collection.immutable

/**
 * SPC- Smart Pay Connect - Interaction Node categories
 */
sealed trait InteractionCategory extends EnumEntry

object InteractionCategory {
  import InteractionCategories._
  implicit val format: Format[InteractionCategory] = EnumFormat(InteractionCategories)

  def apply(category: String): InteractionCategory = {
    category match {
      case "online"      => OnlineCategory
      case "card_reader" => CardReader
      case "pin_entry"   => PinEntry
      case "signature"   => Signature
      case "transaction" => Transaction
      case x             => throw new RuntimeException(s"Unknown InteractionCategory: $x")
    }
  }
}

object InteractionCategories extends Enum[InteractionCategory] {
  final case object OnlineCategory extends InteractionCategory { override def toString: String = "online" }
  final case object CardReader extends InteractionCategory { override def toString: String = "card_reader" }
  final case object PinEntry extends InteractionCategory { override def toString: String = "pin_entry" }
  final case object Signature extends InteractionCategory { override def toString: String = "signature" }
  final case object Transaction extends InteractionCategory { override def toString: String = "transaction" }
  override def values: immutable.IndexedSeq[InteractionCategory] = findValues
}



