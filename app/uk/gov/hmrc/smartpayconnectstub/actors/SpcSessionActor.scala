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

package uk.gov.hmrc.smartpayconnectstub.actors

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger

object SpcSessionActor {
  def props(out: ActorRef, spcParentActor: ActorRef):Props = Props(new SpcSessionActor(out,spcParentActor))
}

class SpcSessionActor(out: ActorRef, spcParentActor: ActorRef) extends Actor {
  import SpcParentActor._

  override def preStart(): Unit = {
    logger.info(s"Starting Session Actor!!!!! $self")
    super.preStart()
  }

  override def postStop(): Unit = {
    logger.info(s"Stopping Session Actor!!!!! $self")
    super.postStop()
  }


  override def receive: Receive = {
    case request: String =>
      spcParentActor ! SpcWSStringMessage(out, request)
  }

  private lazy val logger = Logger(SpcSessionActor.getClass)
}
