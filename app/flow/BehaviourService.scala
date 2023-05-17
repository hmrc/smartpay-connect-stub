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

package flow

import models.TransactionId
import scenario.ScenarioService
import utils.DeviceId

import scala.collection.concurrent.TrieMap

object BehaviourService {

  private val behaviours: TrieMap[TransactionId, SpcBehaviour] = TrieMap()

  def getBehaviour(transactionId: TransactionId, deviceId: DeviceId): SpcBehaviour = {
    val scenario = ScenarioService.getScenario(deviceId)
    val maybeBehaviour: Option[SpcBehaviour] = behaviours.get(
      transactionId
    )

    val behaviour: SpcBehaviour = maybeBehaviour match {
      case Some(b) => b
      case None =>
        val b: SpcBehaviour = SpcFlows.getFlow(scenario).initialBehaviour
        updateBehaviour(transactionId, b)
        b
    }

    behaviour

  }

  def removeBehaviour(transactionId: TransactionId): Unit = {
    behaviours.remove(transactionId)
    ()
  }

  def updateBehaviour(transactionId: TransactionId, behaviour: SpcBehaviour): Unit = {
    behaviours.update(transactionId, behaviour)
  }

}
