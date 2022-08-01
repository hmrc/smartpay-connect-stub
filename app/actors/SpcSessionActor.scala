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

import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import akka.pattern.pipe
import akka.actor.Stash
import models.{StubPath, StubPaths}
import repository.StubRepository

import scala.concurrent.Future

/**
 * Session actor is only exists for duration of websocket connection.
 * When websocket close actor is terminated
 */
object SpcSessionActor {
  def props(out: ActorRef, spcParentActor: ActorRef, mayBeDeviceId: Option[String],repository: StubRepository):Props = Props(new SpcSessionActor(out,spcParentActor, mayBeDeviceId, repository))
}

class SpcSessionActor(out: ActorRef, spcParentActor: ActorRef, mayBeDeviceId: Option[String], repository: StubRepository)
  extends Actor with Stash {
  import SpcParentActor._

  val defaultPath = StubPaths.SuccessNoVerification
  implicit val ec = context.dispatcher

  override def preStart(): Unit = {
    logger.debug(s"Starting Session Actor $self")
    super.preStart()
  }

  override def postStop(): Unit = {
    logger.debug(s"Stopping Session Actor $self")
    super.postStop()
  }

  override def receive: Receive = waitForFirstMessage
  def waitForFirstMessage: Receive = {
    case firstRequest: String =>
      logger.debug(s"SpcSessionActor $self received first message $firstRequest")
      logger.debug(s"mayBeDeviceId  $mayBeDeviceId")
      stash()
      val stubPathF = mayBeDeviceId match {
          case Some(deviceId) => repository.find("_id" -> deviceId).map(_.headOption).map{x =>
            x.getOrElse(defaultPath) }
          case None => Future.successful(defaultPath)
        }
      stubPathF.pipeTo(self)
      context.become(waitForStubPath())
    case x => logger.error(s"SpcSessionActor in state waitForStubPath received unhadled message: $x")
  }

  def waitForStubPath(): Receive = {
    case stubPath: StubPath =>
      logger.debug(s"SpcSessionActor $self received StubPath $stubPath")
      unstashAll()
      context.become(withStubPath(stubPath))

    case x => logger.error(s"SpcSessionActor in state waitForStubPath received unhadled message: $x")
  }

  def withStubPath(stubPath: StubPath): Receive ={
    case request: String =>
      logger.debug(s"SpcSessionActor $self processing with StubPath $stubPath")
      spcParentActor ! SpcWSStringMessage(out, request, stubPath)
    case x => logger.error(s"SpcSessionActor in state withStubPath received unhadled message: $x")
  }

  private lazy val logger = Logger(SpcSessionActor.getClass)
}
