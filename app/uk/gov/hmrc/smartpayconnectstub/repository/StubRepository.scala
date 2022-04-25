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

import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes._
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.smartpayconnectstub.models.{DeviceId, StubPath}
import scala.concurrent.ExecutionContext
import StubPath._

@Singleton
final class StubRepository @Inject() (reactiveMongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends Repository[StubPath, DeviceId]("smartpay-connect-stub", reactiveMongoComponent){

  override def indexes: Seq[Index] = Seq(
    Index(
      key     = Seq("created" -> IndexType.Ascending),
      name    = Some("createdIdx"),
      options = BSONDocument("expireAfterSeconds" -> 600)
    ),
    Index(
      key  = Seq(DeviceId.headerName -> IndexType.Ascending),
      name = Some(DeviceId.headerName)
    )
  )
}
