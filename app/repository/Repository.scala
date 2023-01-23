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

package repository

import org.bson.codecs.Codec
import org.mongodb.scala.model.{Filters, IndexModel, ReplaceOptions}
import org.mongodb.scala.result
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

abstract class Repo[ID <: Id, A](
                                               collectionName: String,
                                               mongoComponent: MongoComponent,
                                               indexes:        Seq[IndexModel],
                                               extraCodecs:    Seq[Codec[_]],
                                               replaceIndexes: Boolean         = false
                                             )(implicit manifest: Manifest[A],
                                               domainFormat:     OFormat[A],
                                               executionContext: ExecutionContext
                                             )
  extends PlayMongoRepository[A](
    mongoComponent = mongoComponent,
    collectionName = collectionName,
    domainFormat   = domainFormat,
    indexes        = indexes,
    replaceIndexes = replaceIndexes,
    extraCodecs    = extraCodecs
  ) {

  /**
   * Update or Insert (UpSert) element `a` identified by `id`
   */
  def upsert(id: ID, a: A): Future[result.UpdateResult] = collection
    .replaceOne(
      filter      = Filters.eq("_id", id.value),
      replacement = a,
      options     = ReplaceOptions().upsert(true)
    )
    .toFuture()

  def findById(id: ID): Future[Option[A]] = collection
    .find(
      filter = Filters.eq("_id", id.value)
    )
    .headOption()

}

trait Id {
  def value: String
}

trait HasId[ID <: Id] {
  def _id: ID
  def id: ID = _id
}
