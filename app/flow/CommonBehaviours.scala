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

package flow
import models.TranResults.SuccessResult
import models._

object CommonBehaviours {

  lazy val handleFinalise: SpcBehaviour = behave {
    case finalise: Finalise =>
      val finaliseResponse: FinaliseResponse = FinaliseResponse(HeaderNode(), finalise.messageNode, SuccessResult)
      (List(finaliseResponse),
        handlePedLogOff
      )
  }

  lazy val handlePedLogOff: SpcBehaviour = behave {
    case pedLogOff: PedLogOff =>
      val pedLogOffResponse = PedLogOffResponse(HeaderNode(), pedLogOff.messageNode, SuccessResult)
      (
        List(pedLogOffResponse),
        done
      )
  }

}
