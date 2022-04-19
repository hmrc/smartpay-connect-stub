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
import play.api.libs.json.Format
import utils.EnumFormat

import scala.collection.immutable

sealed trait TransactionDecision extends EnumEntry {
  val decisionType: String
  val decisionDesc: String
}

object TransactionDecision {
  import TransactionDecisions._
  implicit val format: Format[TransactionDecision] = EnumFormat(TransactionDecisions)

  def apply(decisionType: String): TransactionDecision = {
    decisionType match {
      case "get_sig_auth"     => SignatureRequired
      case "get_man_auth"     => AuthorizationRequired
      case "cv2_avs_decision" => LiabilityRequired
      case x                  => throw new RuntimeException(s"Unknown TransactionDecision: $x")
    }
  }
}

object TransactionDecisions extends Enum[TransactionDecision] {

  case object SignatureRequired extends TransactionDecision {
    override val decisionType: String = "get_sig_auth"
    override val decisionDesc: String = "Signature verification required"
  }

  case object AuthorizationRequired extends TransactionDecision {
    override val decisionType: String = "get_man_auth"
    override val decisionDesc: String = "Manual authorisation is required."
  }

  case object LiabilityRequired extends TransactionDecision {
    override val decisionType: String = "cv2_avs_decision"
    override val decisionDesc: String = "A CV2/AVS liability decision is required."
  }
  override def values: immutable.IndexedSeq[TransactionDecision] = findValues
}



