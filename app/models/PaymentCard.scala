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

import play.api.libs.json.{Json, OFormat}

import java.text.SimpleDateFormat

final case class PaymentCard(currency:       CurrencyNum,
                             country:        Country,
                             endDate:        String,
                             startDate:      String,
                             pan:            String,
                             cardSchema:     CardSchema,
                             seqNum:         String,
                             availableSpend: Option[AmountInPence]) {

  def receiptPan: String = pan.take(6) + pan.drop(6).replaceAll(".(?=.{4})", "*")
  def receiptPanMasked: String = "*** Data Removed for Security ***"
  def receiptStart: String = new SimpleDateFormat("MM/yy").format(new SimpleDateFormat("yyyy-MM-dd").parse(startDate).getTime)
  def receiptEnd: String = new SimpleDateFormat("MM/yy").format(new SimpleDateFormat("yyyy-MM-dd").parse(endDate).getTime)
  def receiptEndMasked: String = "****-**"

}
object PaymentCard {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[PaymentCard] = Json.format[PaymentCard]
}
