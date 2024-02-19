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
 * SPC- Smart Pay Connect - Interaction Node events
 */
sealed trait TransactionSource extends EnumEntry

object TransactionSource {
  import TransactionSources._
  implicit val format: Format[TransactionSource] = EnumFormat(TransactionSources)

  def apply(category: String): TransactionSource = {
    category match {
      case "icc"   => Icc
      case "keyed" => Keyed
      case x       => throw new RuntimeException(s"Unknown TransactionSource: $x")
    }
  }
}

object TransactionSources extends Enum[TransactionSource] {
  final case object Icc extends TransactionSource { override def toString: String = "icc" }
  final case object Keyed extends TransactionSource { override def toString: String = "keyed" }
  override def values: immutable.IndexedSeq[TransactionSource] = findValues
}

