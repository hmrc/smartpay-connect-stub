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

package behaviour

import models.TranResults.SuccessResult
import models.{ErrorMessage, ErrorNode, ErrorsNode, HeaderNode, SpcRequestMessage, SpcResponseMessage}

/**
 * Wrapper around partial function which returns both next behaviour and the value returned by the partial function.
 * It's strong type alternative to code previously written using akka classic actors and "receive: Any => Unit"
 */
sealed trait Behaviour[-I, +O] {
  def orElse[I1 <: I, O1 >: O](b: Behaviour[I1, O1]): Behaviour[I1, O1] = (this, b) match {
    case (BDone, BDone)                               => BDone
    case (BDone, bd: BDefined[I1, O1])                => bd
    case (bd: BDefined[I, O], BDone)                  => bd
    case (bd1: BDefined[I, O], bd2: BDefined[I1, O1]) => BDefined(pf = bd1.pf.orElse(bd2.pf))
  }

  //  /**
  //   * Maps over the returned type O
  //   */
  //  def map[O2](f: O => O2): Behaviour[I, O2] = this match {
  //    case BDone              => BDone
  //    case bd: BDefined[I, O] => BDefined[I, O2](bd.pf.andThen(t => (f(t._1), t._2.map(f))))
  //  }
}

case object BDone extends Behaviour[Any, Nothing]

final case class BDefined[I, O](pf: PartialFunction[I, (O, Behaviour[I, O])]) extends Behaviour[I, O]

object Behaviour {

  type B = Behaviour[SpcRequestMessage, Seq[SpcResponseMessage]]

  def behave(pf: PartialFunction[SpcRequestMessage, (Seq[SpcResponseMessage], B)]): B = BDefined(pf)
  //  type BSingle = Behaviour[SpcRequestMessage, SpcResponseMessage]
  //  def behaveSingle(pf: PartialFunction[SpcRequestMessage, (SpcResponseMessage, BSingle)]): B = BDefined[SpcRequestMessage, SpcResponseMessage](pf).map(List(_))

  def done: Behaviour[SpcRequestMessage, Nothing] = BDone

  val unexpectedMessage: B = behave {
    case unexpected: SpcRequestMessage =>
      val errorNode = ErrorNode("XXXXXX", s"Unexpected message [${unexpected.name}] for selected stub flow")
      val errorsNode = ErrorsNode(Seq(errorNode))
      val errorResponse: ErrorMessage = ErrorMessage(HeaderNode(), unexpected.messageNode, errorsNode, SuccessResult)
      (List(errorResponse), done)
  }

}
