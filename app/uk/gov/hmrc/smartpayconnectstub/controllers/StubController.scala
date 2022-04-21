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

package uk.gov.hmrc.smartpayconnectstub.controllers

import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartpayconnectstub.models.{DeviceId, StubPath}
import uk.gov.hmrc.smartpayconnectstub.repository.StubRepository
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class StubController @Inject() (repository: StubRepository, cc: ControllerComponents)(implicit executionContext: ExecutionContext) extends BackendController(cc){

  def setPathForDeviceId(path: String): Action[AnyContent] = Action.async { implicit request =>
    val deviceId: DeviceId = DeviceId(request.headers.get(DeviceId.headerName).getOrElse(throw new RuntimeException("storePathForDeviceId error: No Device Id provided")))
    val chosenPath: StubPath = StubPath(path)

    repository.upsert(deviceId, chosenPath).map { _ =>
      Ok(toJson("Great Success"))
    }
  }
}
