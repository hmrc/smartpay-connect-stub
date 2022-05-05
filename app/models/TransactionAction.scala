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
import play.api.libs.json.{Format}
import utils.EnumFormat

import scala.collection.immutable

sealed trait TransactionAction extends EnumEntry

object TransactionAction {
  import TransactionActions._
  implicit val format: Format[TransactionAction] = EnumFormat(TransactionActions)

  def apply(value: String): TransactionAction = {
    value match {
      case "auth_n_settle"        => AuthorizeAndSettle
      case x                => throw new RuntimeException(s"Unknown TransactionAction: $x")
    }
  }
}

object TransactionActions extends Enum[TransactionAction] {
  case object AuthorizeAndSettle extends TransactionAction { override def toString: String = "auth_n_settle" }

  override def values: immutable.IndexedSeq[TransactionAction] = findValues
}