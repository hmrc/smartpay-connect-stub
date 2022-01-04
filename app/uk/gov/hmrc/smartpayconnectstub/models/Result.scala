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

sealed trait Result
final case object Success extends Result {override def toString: String = "success"}
final case object Failure extends Result {override def toString: String = "failure"}


object Result {
  def apply(result:String):Result ={
    result match {
      case "success" => Success
      case "failure" => Failure
      case x => throw new RuntimeException(s"Unknown Result: $x")
    }
  }
}