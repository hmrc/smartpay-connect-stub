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

import akka.actor.Terminated
import akka.actor.{Actor, ActorRef, Props}
import play.api.Logger
import uk.gov.hmrc.smartpayconnectstub.models.{SpcXmlHelper, TransNum}

import scala.xml.{Elem, XML}

object ScpParentActor {
  def props(out: ActorRef):Props = Props(new ScpParentActor(out))
}

class ScpParentActor(out: ActorRef) extends Actor {


  def receive: Receive = handleScpMessages(Map.empty[TransNum, ActorRef])

  def handleScpMessages(userActors:Map[TransNum, ActorRef]) : Receive = {
    case request: String =>
      logger.info(s"Got message $request")
      val xmlMsg:Elem = XML.loadString(request)
      val transNum = SpcXmlHelper.getSpcXmlMessageNode(xmlMsg).transNum
      userActors.get(transNum).fold {
        val actorRef = context.actorOf(ScpUserActor.props(out, transNum),s"$transNum")
        actorRef ! xmlMsg
        logger.info(s"Send XML message $request to:$actorRef for transactionId:$transNum")
        context.watch(actorRef)
        context.become(handleScpMessages( userActors + (transNum -> actorRef)))
      }{ actorRef =>
        actorRef ! xmlMsg
        logger.info(s"Send XML message $request to:$actorRef for transactionId:$transNum")
      }
    case Terminated(ref) =>
      logger.info(s"Actor user has been terminated $ref")
      userActors.filter(x => x._2 == ref).keys.headOption.map { stoppedActor =>
        context.become(handleScpMessages(userActors - stoppedActor))
      }

    case e => logger.error(s"Scp Parent Actor not handled message: $e" )


  }
  private lazy val logger = Logger(ScpParentActor.getClass)
}
