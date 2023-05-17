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

object BehaviourService {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var behaviours: Map[TransactionId, SpcBehaviour] = Map()

  def getBehaviour(transactionId: TransactionId, deviceId: DeviceId): SpcBehaviour = behaviours.getOrElse(
    transactionId,
    SpcFlows.getFlow(ScenarioService.getScenario(deviceId)).initialBehaviour
  )

  def removeBehaviour(transactionId: TransactionId): Unit = {
    behaviours = behaviours.removed(transactionId)
  }

  def updateBehaviour(transactionId: TransactionId, behaviour: SpcBehaviour): Unit = {
    behaviours = behaviours.updated(transactionId, behaviour)
  }

}
