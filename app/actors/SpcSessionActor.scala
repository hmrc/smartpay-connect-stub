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

package actors

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.pattern.pipe
import play.api.Logger
import scenario.{Scenario, ScenarioRepo, ScenarioService}

import scala.concurrent.Future

/**
 * Session actor is only exists for duration of websocket connection.
 * When websocket close actor is terminated
 */
object SpcSessionActor {
  def props(out: ActorRef, spcParentActor: ActorRef, scenarioService: ScenarioService): Props = Props(new SpcSessionActor(out, spcParentActor, scenarioService))
}

class SpcSessionActor(out: ActorRef, spcParentActor: ActorRef, scenarioService: ScenarioService)
  extends Actor with Stash {
  import SpcParentActor._

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

  private def waitForFirstMessage: Receive = {
    case firstRequest: String =>
      logger.debug(s"SpcSessionActor $self received first message $firstRequest")
      stash()
      val scenario: Future[Scenario] = scenarioService.getScenario()
      scenario.pipeTo(self)
      context.become(waitForScenario())
    case x => logger.error(s"SpcSessionActor in state waitForStubPath received unhadled message: $x")
  }

  private def waitForScenario(): Receive = {
    case scenario: Scenario =>
      logger.debug(s"SpcSessionActor $self received StubPath $scenario")
      unstashAll()
      context.become(withScenario(scenario))

    case x => logger.error(s"SpcSessionActor in state waitForStubPath received unhadled message: $x")
  }

  private def withScenario(scenario: Scenario): Receive = {
    case request: String =>
      logger.debug(s"SpcSessionActor $self processing with StubPath $scenario")
      spcParentActor ! SpcWSStringMessage(out, request, scenario)
    case x => logger.error(s"SpcSessionActor in state withStubPath received unhadled message: $x")
  }

  private lazy val logger = Logger(SpcSessionActor.getClass)
}
