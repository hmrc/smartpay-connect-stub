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

sealed trait TranResult extends EnumEntry

object TranResult {
  import TranResults._
  implicit val format: Format[TranResult] = EnumFormat(TranResults)

  def apply(result: String): TranResult = {
    result match {
      case "success" => SuccessResult
      case "failure" => FailureResult
      case x         => throw new RuntimeException(s"Unknown scp message Result received: $x")
    }
  }
}

object TranResults extends Enum[TranResult] {
  final case object SuccessResult extends TranResult { override def toString: String = "success" }
  final case object FailureResult extends TranResult { override def toString: String = "failure" }

  override def values: immutable.IndexedSeq[TranResult] = findValues
}
