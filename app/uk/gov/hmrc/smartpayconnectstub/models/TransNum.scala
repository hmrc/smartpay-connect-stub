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

final case class TransNum(value:String) {
  def isEqual(transNum:TransNum):Boolean = { value.equalsIgnoreCase(transNum.value) }
  def isValid:Boolean = value != TransNum.zero.value
}

object TransNum {
  val zero: TransNum = TransNum("000000")

  def apply(value: String): TransNum = {
    value match {
      case s if s.isEmpty => zero
      case _              => TransNum(value)
    }
  }


}
