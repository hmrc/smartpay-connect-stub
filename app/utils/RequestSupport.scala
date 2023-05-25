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

package utils

import cats.implicits.catsSyntaxEq
import deviceid.DeviceId
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

import javax.inject.Inject
/**
 * Repeating the pattern which was brought originally by play-framework
 * and putting some more data which can be derived from a request
 *
 * Use it to provide HeaderCarrier, Lang, or Messages
 */
class RequestSupport @Inject() (override val messagesApi: MessagesApi) extends I18nSupport {

  implicit def hc(implicit request: Request[_]): HeaderCarrier = RequestSupport.hc
  def lang(implicit messages: Messages): Lang = messages.lang

  //implicit def language(implicit messages: Messages): Language = Language(messages.lang)
}

object RequestSupport {
  def isLoggedIn(implicit request: Request[_]): Boolean = request.session.get(SessionKeys.authToken).isDefined

  implicit def hc(implicit request: Request[_]): HeaderCarrier = HcProvider.headerCarrier

  def deviceId(implicit request: Request[_]): DeviceId = request
    .cookies
    .find(_.name === DeviceId.cookieName).map(c => DeviceId(c.value))
    .getOrElse(DeviceId.couldNotFindDeviceId)

  /**
   * This is because we want to give responsibility of creation of [[HeaderCarrier]] to the platform code.
   * If they refactor how hc is created our code will pick it up automatically.
   */
  private object HcProvider extends FrontendHeaderCarrierProvider {
    def headerCarrier(implicit request: Request[_]): HeaderCarrier = hc(request)
  }

  //  implicit def language(implicit messages: Messages): Language = Language(messages.lang)
}
