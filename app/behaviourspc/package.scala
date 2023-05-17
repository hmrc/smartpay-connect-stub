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

import behaviour.{BDefined, BDone, Behaviour}
import models.{SpcRequestMessage, SpcResponseMessage}

package object behaviourspc {

  type SpcBehaviour = Behaviour[SpcRequestMessage, Seq[SpcResponseMessage]]
  def behave(pf: PartialFunction[SpcRequestMessage, (Seq[SpcResponseMessage], SpcBehaviour)]): SpcBehaviour = BDefined(pf)
  val done: Behaviour[SpcRequestMessage, Nothing] = BDone
}
