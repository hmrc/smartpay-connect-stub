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

package utils

import play.api.libs.json.{Format, OFormat, JsObject, JsValue, JsResult}

object JsonUtil {

  def oFormat[T](format: Format[T]): OFormat[T] = {
    val oFormat: OFormat[T] = new OFormat[T]() {
      override def writes(o: T): JsObject = format.writes(o).as[JsObject]

      override def reads(json: JsValue): JsResult[T] = format.reads(json)
    }
    oFormat
  }
}