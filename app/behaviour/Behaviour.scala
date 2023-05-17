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

/**
 * Behaviour (B) Done. The last step, nothing more to do
 */
case object BDone extends Behaviour[Any, Nothing]

/**
 * Behaviour (B) Defined
 */
final case class BDefined[I, O](pf: PartialFunction[I, (O, Behaviour[I, O])]) extends Behaviour[I, O]
