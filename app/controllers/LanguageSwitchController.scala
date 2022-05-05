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

import langswitch.Language
import play.api.mvc._
import play.mvc.Http.HeaderNames
import utils.RequestSupport

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class LanguageSwitchController @Inject() (
                                           requestSupport: RequestSupport,
                                           cc:             ControllerComponents
                                         )(
                                           implicit
                                           ec: ExecutionContext
                                         )
  extends AbstractController(cc) {

  import requestSupport._

  def switchToLanguage(language: Language): Action[AnyContent] = cc.actionBuilder { implicit request =>
    val referer: String =
      request
        .headers
        .get(HeaderNames.REFERER)
        .getOrElse(throw new RuntimeException("Missing Referer"))

    val result = Redirect(referer)
    result.withLang(language.toPlayLang)
  }

}


