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

package scenario

import deviceid.DeviceId

import scala.collection.concurrent.TrieMap

object ScenarioService {

  private val scenarios: TrieMap[DeviceId, Scenario] = TrieMap()

  def setScenario(deviceId: DeviceId, scenario: Scenario): Unit = {
    scenarios.update(deviceId, scenario)
  }

  def getScenario(deviceId: DeviceId): Scenario = scenarios.getOrElse(deviceId, Scenario.default)

}
