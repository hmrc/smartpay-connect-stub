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

package uk.gov.hmrc.smartpayconnectstub.repository

import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.UpdateWriteResult
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

abstract class Repository[A, ID](
                            collectionName:         String,
                            reactiveMongoComponent: ReactiveMongoComponent)
                          (implicit domainFormat: OFormat[A],
                           idFormat:         Format[ID],
                           executionContext: ExecutionContext)
  extends ReactiveRepository[A, ID](
    collectionName,
    reactiveMongoComponent.mongoConnector.db,
    domainFormat,
    idFormat) {

  implicit val f: OWrites[JsObject] = new OWrites[JsObject] {
    override def writes(o: JsObject): JsObject = o
  }

  /**
   * Update or Insert (UpSert)
   */
  def upsert(id: ID, a: A): Future[UpdateWriteResult] = collection.update(ordered = false).one(
    _id(id),
    a,
    upsert = true
  )
}

