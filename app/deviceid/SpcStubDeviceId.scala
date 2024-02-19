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

package deviceid

import utils.RandomDataGenerator

/**
 * This is dedicated for the stub device id. The reason of not relying
 * on standard platform's device id is that stride-auth-stub overrides it.
 * We need an identifier which is persisted between sessions.
 * It's for testing purposes only and should not affect any production functionality.
 */
final case class SpcStubDeviceId(value: String)

object SpcStubDeviceId {
  def fresh(): SpcStubDeviceId = SpcStubDeviceId(RandomDataGenerator.randomAlphaNumeric(6))
  val couldNotFindDeviceId: SpcStubDeviceId = SpcStubDeviceId("couldNotFindDeviceId")
  val cookieName: String = "spcstubdi"
}
