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

import enumeratum.EnumEntry
import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait CustomerPresence extends EnumEntry

object CustomerPresence {
  implicit val format: OFormat[CustomerPresence] = derived.oformat[CustomerPresence]()

  def apply(value: String): CustomerPresence = {
    value match {
      case "present" => present
      case x         => throw new RuntimeException(s"Unknown TransactionCustomer: $x")
    }
  }

  case object present extends CustomerPresence
}
