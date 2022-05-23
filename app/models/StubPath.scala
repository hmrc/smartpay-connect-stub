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

package models

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json._
import utils.{EnumFormat, JsonUtil}

import scala.collection.immutable


sealed trait StubPath extends EnumEntry {
  def value: String
  def description: String
}

object StubPath {
  import StubPaths._
  implicit val format: Format[StubPath] = EnumFormat(StubPaths)
  implicit val oformat: OFormat[StubPath] = JsonUtil.oFormat(format)

  def apply(value: String): StubPath = {
    value match {
      case "success_icc" => SuccessIcc
      case "card_declined_icc"   => CardDeclinedIcc
      case "cancelled_ped_icc" => CancelledOnPedIcc
      case "incorrect_pin_icc" => IncorrectPinIcc
      case x             => throw new RuntimeException(s"Unknown StubPath: $x")
    }
  }
}

object StubPaths extends Enum[StubPath] {
  final case object SuccessIcc extends StubPath { val value = "success_icc"; val description = "Chip&Pin success path with surcharge accepted" }
  final case object CardDeclinedIcc extends StubPath { val value =  "card_declined_icc" ; val description = "Chip&Pin path with card declined by card provider"}
  final case object CancelledOnPedIcc extends StubPath { val value =  "cancelled_ped_icc" ; val description = "Chip&Pin path with transaction cancelled by user on ped"}
  final case object IncorrectPinIcc extends StubPath { val value =  "incorrect_pin_icc" ; val description = "Chip&Pin path with incorrect PIN and card removed from PED"}
  override def values: immutable.IndexedSeq[StubPath] = findValues
}