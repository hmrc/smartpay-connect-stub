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

import julienrf.json.derived
import play.api.libs.json.OFormat

/**
 * SPC- Smart Pay Connect - Interaction Node events
 */
sealed trait CardVerificationMethod

object CardVerificationMethod {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[CardVerificationMethod] = derived.oformat[CardVerificationMethod]()

  def apply(category: String): CardVerificationMethod = {
    category match {
      case "pin"               => pin
      case "signature"         => signature
      case "pin_and_signature" => pin_and_signature
      case "on_device"         => on_device
      case "not_performed"     => not_performed
      case "failed"            => failed
      case "unknown"           => unknown
      case x                   => throw new RuntimeException(s"Unknown CardVerificationMethod: $x")
    }
  }

  final case object pin extends CardVerificationMethod
  final case object signature extends CardVerificationMethod
  final case object pin_and_signature extends CardVerificationMethod
  final case object on_device extends CardVerificationMethod
  final case object not_performed extends CardVerificationMethod
  final case object failed extends CardVerificationMethod
  final case object unknown extends CardVerificationMethod
}

