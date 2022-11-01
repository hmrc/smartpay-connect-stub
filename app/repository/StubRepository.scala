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

package repository

import models.{DeviceId, StubPath}
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.mongo.MongoComponent

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext


object StubRepository {
  def indexes: Seq[IndexModel] = Seq(
    IndexModel(
      keys         = Indexes.ascending("created"),
      indexOptions = IndexOptions().expireAfter(600, TimeUnit.SECONDS).name("createdIdx")
    ),
    IndexModel(
      keys         = Indexes.ascending(DeviceId.headerName),
      indexOptions = IndexOptions().name(DeviceId.headerName)
    )
  )
}


@Singleton
final class StubRepository @Inject() (
                                       mongoComponent: MongoComponent
                                      )(implicit ec: ExecutionContext)
  extends Repo[DeviceId, StubPath](
    collectionName = "smartpay-connect-stub",
    mongoComponent = mongoComponent,
    indexes        = StubRepository.indexes,
    extraCodecs    = Seq.empty,
    replaceIndexes = true
  ) {
}

