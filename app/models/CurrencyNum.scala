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

import play.api.libs.json.{Format, Json}

final case class CurrencyNum(value: String) {
  def toCurrencyCode = value match {
    case "826" => "GBP"
    case "840" => "USD"
    case "978" => "EUR"
    case x     => x
  }
}

object CurrencyNum {
  implicit val format: Format[CurrencyNum] = Json.valueFormat
  val Gbp = CurrencyNum("826")
  val Usd = CurrencyNum("840")
  val Eur = CurrencyNum("978")
}
