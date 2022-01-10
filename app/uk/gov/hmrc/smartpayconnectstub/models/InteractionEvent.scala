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

/**
 * SPC- Smart Pay Connect - Interaction Node events
 */
sealed trait InteractionEvent
final case object use_chip extends InteractionEvent
final case object in_progress extends InteractionEvent
final case object EventSuccess extends InteractionEvent { override def toString: String = "success"}

object InteractionEvent {
  def apply(category:String):InteractionEvent ={
    category match {
      case "use_chip" => use_chip
      case "in_progress" => in_progress
      case "success" => EventSuccess
      case x => throw new RuntimeException(s"Unknown InteractionEvent: $x")
    }
  }
}
