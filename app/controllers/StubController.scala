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

package controllers

import forms.{PathForDeviceForm, PathForDeviceFormData}
import langswitch.ErrorMessages
import models.{DeviceId, StubPaths}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repository.StubRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RequestSupport
import views.html.{PathForDeviceView, SuccessView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class StubController @Inject() (
                                 val controllerComponents: MessagesControllerComponents,
                                 repository: StubRepository,
                                 path_for_device_view: PathForDeviceView,
                                 success_view: SuccessView
                               )(implicit executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def getPathForDeviceId:Action[AnyContent] = Action.async { implicit request =>
    val formWithData = PathForDeviceForm.form(translatedFormErrors).fill(PathForDeviceFormData(StubPaths.SuccessKeyed))
    Future.successful(Ok(path_for_device_view(formWithData)))
  }


  def submitPathForDeviceId: Action[AnyContent] = Action.async { implicit request =>
    val deviceId = RequestSupport.hc(request.withBody()).deviceID.map(DeviceId(_)).getOrElse(throw new RuntimeException("storePathForDeviceId error: No Device Id provided"))

    PathForDeviceForm.form(translatedFormErrors).bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(path_for_device_view(formWithErrors))),
      (simpleFormData: PathForDeviceFormData) => {
        val stubPath = simpleFormData.path
        repository.upsert(deviceId, stubPath).map { _ =>
          Ok(success_view(stubPath))
        }
      }
    )
  }

  import utils.RequestSupport.language
  def translatedFormErrors(implicit request: Request[_]) = Map(
    "pathRequired" -> ErrorMessages.error_path_required.show
  )
}
