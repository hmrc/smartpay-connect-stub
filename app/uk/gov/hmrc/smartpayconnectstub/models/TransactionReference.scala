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

import play.api.libs.json.Format
import play.api.libs.functional.syntax._

final case class TransactionReference(value: String)

object TransactionReference {
  implicit val format: Format[TransactionReference] = implicitly[Format[String]].inmap(TransactionReference(_), _.value)
  val Uk = Country("826")
  val US = Country("840")
}