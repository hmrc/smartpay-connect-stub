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

package scenario

import cats.implicits.catsSyntaxEq
import deviceid.SpcStubDeviceId
import forms.ScenarioForm
import play.api.mvc.{Action, AnyContent, Cookie, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RequestSupport.deviceId
import views.html.ScenariosView

import javax.inject.Inject

class ScenarioController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    scenariosView:            ScenariosView)
  extends FrontendBaseController {

  def showScenarios: Action[AnyContent] = Action { implicit request =>
    val scenario = ScenarioService.getScenario(deviceId)
    val result = Ok(scenariosView(ScenarioForm.form.fill(scenario)))
    request
      .cookies
      .find(_.name === SpcStubDeviceId.cookieName)
      .fold(result.withCookies(makeDeviceIdCookie()))(_ => result)
  }

  private def makeDeviceIdCookie(): Cookie = Cookie(
    name   = SpcStubDeviceId.cookieName,
    value  = SpcStubDeviceId.fresh().value,
    maxAge = Some(315360000) //10 years
  )

  def submitScenario: Action[AnyContent] = Action { implicit request =>
    ScenarioForm
      .form
      .bindFromRequest()
      .fold(
        formWithErrors => Ok(scenariosView(formWithErrors)),
        (scenario: Scenario) => {
          ScenarioService.setScenario(deviceId, scenario)
          Redirect(routes.ScenarioController.showScenarios)
        }
      )
  }
}
