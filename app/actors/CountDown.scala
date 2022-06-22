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

package actors

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import models.InteractionCategories.{CardReader, OnlineCategory}
import models.InteractionEvents.{EventSuccess, InProgress, Processing, UseChip}
import models.InteractionPrompts.{ConnectingToAcquirer, InsertCard, ProcessingTransaction}
import models.Results.SuccessResult
import models._
import play.api.Logger

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object CountDown {

  val value = 10.minutes
}
