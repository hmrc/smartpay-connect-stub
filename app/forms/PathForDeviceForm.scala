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

package forms

import models.StubPath
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import play.api.libs.json.{Format, Json}

final case class PathForDeviceFormData(path: StubPath)

object PathForDeviceFormData {
  implicit val format: Format[PathForDeviceFormData] = Json.format[PathForDeviceFormData]
}

object PathForDeviceForm {
  def pathConstraint(errors: Map[String, String]): Formatter[StubPath] = new Formatter[StubPath] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], StubPath] = data.get(key) match {
      case Some(ref) => Right(StubPath(ref))
      case None => Left(Seq(FormError(key, errors("pathRequired"))))
    }

    override def unbind(key: String, path: StubPath): Map[String, String] = Map(key -> path.value)
  }

  def form(errors: Map[String, String]): Form[PathForDeviceFormData] =
    Form(
      mapping = mapping(
        "path" -> Forms.of(pathConstraint(errors))
      )(PathForDeviceFormData.apply)(PathForDeviceFormData.unapply)
    )
}
