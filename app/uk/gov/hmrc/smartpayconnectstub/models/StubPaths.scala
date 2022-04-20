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

package uk.gov.hmrc.smartpayconnectstub.models

import enumeratum._
import uk.gov.hmrc.smartpayconnectstub.utils.ValueClassBinder.{bindableA, valueClassBinder}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{PathBindable, QueryStringBindable}
import scala.collection.immutable

sealed abstract class StubPath extends EnumEntry

object StubPath {
  implicit val format: OFormat[StubPath] = Json.format[StubPath]
  implicit val pathBinder: QueryStringBindable[StubPath] = bindableA(_.toString)
  implicit val taxTypeBinder: PathBindable[StubPath] = valueClassBinder(_.toString)
}

object StubPaths extends Enum[StubPath] {

  case object Happy extends StubPath

  override def values: immutable.IndexedSeq[StubPath] = findValues
}

