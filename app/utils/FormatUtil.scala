/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.*
import scala.reflect.ClassTag

object FormatUtil:

  def apply[T](objectMappings: (String, T)*)(classMappings: MappedClass[T, _]*): OFormat[T] = new OFormat[T]:
    private val nameToObject: Map[String, T]                           = objectMappings.toMap
    private val objectToName: Map[T, String]                           = objectMappings.map(_.swap).toMap
    private val nameToClassReader: Map[String, JsValue => JsResult[T]] =
      classMappings.map(c => c.name -> c.read _).toMap

    override def reads(json: JsValue): JsResult[T] =
      json.as[JsObject].fields.headOption match
        // Case object
        case Some((name, _)) if nameToObject.contains(name)          =>
          JsSuccess(nameToObject(name), JsPath \ name)
        // Case class
        case Some((name, value)) if nameToClassReader.contains(name) =>
          nameToClassReader(name)(value).repath(JsPath \ name)
        case Some((other, _))                                        => JsError(s"Unknown discriminator key: $other")
        case None                                                    => JsError("Empty JSON object")

    override def writes(o: T): JsObject =
      // Case object
      if objectToName.contains(o) then Json.obj(objectToName(o) -> Json.obj())
      // Case class
      else
        classMappings
          .collectFirst {
            case handler if handler.matches(o) =>
              Json.obj(handler.name -> handler.write(o))
          }
          .getOrElse(throw new IllegalStateException(s"Missing mapping for class ${o.getClass.getName}"))

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  class MappedClass[P, S <: P](val name: String)(using format: OFormat[S], ct: ClassTag[S]):
    def matches(o: P): Boolean           = ct.runtimeClass.isInstance(o)
    def write(o:   P): JsValue           = format.writes(o.asInstanceOf[S])
    def read(json: JsValue): JsResult[P] = format.reads(json).map(_.asInstanceOf[P])

  // Usage: caseClass[ParentType].entry[SubType]("Name")
  def caseClass[P]: ClassMapper[P] = new ClassMapper[P]

  class ClassMapper[P]:
    def entry[S <: P](name: String)(using OFormat[S], ClassTag[S]): MappedClass[P, S] =
      new MappedClass[P, S](name)
