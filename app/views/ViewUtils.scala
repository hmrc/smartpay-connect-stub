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

package views

import langswitch.LangMessages
import play.api.data.Form
import play.api.i18n.Messages
import utils.RequestSupport
import utils.RequestSupport.language


object ViewUtils {

  def title(form: Form[_], title: String, section: Option[String] = None)(implicit request: RequestSupport, messages: Messages): String =
    titleNoForm(
      title   = s"${errorPrefix(form)} ${LangMessages.stub_frontend.show}",
      section = section
    )

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    s"${messages(title)} - ${section.fold("")(messages(_) + " - ")}${LangMessages.`stub_frontend`.show} - ${LangMessages.`GOV.UK`.show}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) LangMessages.`Error`.show else ""
  }
}