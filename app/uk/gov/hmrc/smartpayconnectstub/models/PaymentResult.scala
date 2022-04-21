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

package uk.gov.hmrc.smartpayconnectstub.models

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.{Format}
import utils.EnumFormat

import scala.collection.immutable

sealed trait PaymentResult extends EnumEntry

object PaymentResult {
  import PaymentResults._
  implicit val format: Format[PaymentResult] = EnumFormat(PaymentResults)

  def apply(value: String): PaymentResult = {
    value match {
      case "on-line"        => OnlineResult
      case "declined"       => declined
      case "cancelled"      => cancelled
      case "not_authorised" => not_authorised
      case x                => throw new RuntimeException(s"Unknown PaymentResult: $x")
    }
  }
}

object PaymentResults extends Enum[PaymentResult] {
  case object OnlineResult extends PaymentResult { override def toString: String = "on-line" }
  case object declined extends PaymentResult
  case object cancelled extends PaymentResult
  case object not_authorised extends PaymentResult

  override def values: immutable.IndexedSeq[PaymentResult] = findValues
}

