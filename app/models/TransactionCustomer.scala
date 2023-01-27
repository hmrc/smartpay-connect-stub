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
import play.api.libs.json.{Format}
import utils.EnumFormat

import scala.collection.immutable

sealed trait TransactionCustomer extends EnumEntry

object TransactionCustomer {
  import TransactionCustomers._
  implicit val format: Format[TransactionCustomer] = EnumFormat(TransactionCustomers)

  def apply(value: String): TransactionCustomer = {
    value match {
      case "present" => Present
      case x         => throw new RuntimeException(s"Unknown TransactionCustomer: $x")
    }
  }
}

object TransactionCustomers extends Enum[TransactionCustomer] {
  case object Present extends TransactionCustomer { override def toString: String = "present" }

  override def values: immutable.IndexedSeq[TransactionCustomer] = findValues
}
