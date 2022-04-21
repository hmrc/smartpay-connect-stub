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

import akka.actor.{Actor, ActorRef, Props, Terminated}
import play.api.Logger
import uk.gov.hmrc.smartpayconnectstub.models.{SpcXmlHelper, TransactionId}

import scala.xml.{Elem, XML}

object SpcParentActor {
  def props():Props = Props(new SpcParentActor())

  case class SpcWSStringMessage(out: ActorRef, msg:String)
}

class SpcParentActor extends Actor {
  import ScpUserActor._
  import SpcParentActor._

  override def postStop(): Unit = {
    logger.debug(s"Stopping Parent Actor $self")
    super.postStop()
  }

  override def preStart(): Unit = {
    logger.debug(s"Starting Parent Actor $self")
    super.preStart()
  }



  def receive: Receive = handleScpMessages(Map.empty[TransactionId,ActorRef])

  def handleScpMessages(userActors:Map[TransactionId, ActorRef]) : Receive = {
    case SpcWSStringMessage(out,request) =>
      val session = context.sender()
      logger.debug(s"Parent Actor got message $request")
      logger.debug(s"Parent Actor userActors $userActors")
      val xmlMsg:Elem = XML.loadString(request)
      val transNum = SpcXmlHelper.getSpcXmlMessageNode(xmlMsg).transNum

      userActors.get(transNum).fold {
        logger.debug(s"Parent actor is going to create user actor with name ${transNum.value}")
        val actorRef = context.actorOf(ScpUserActor.props(),s"${transNum.value}")
        logger.debug(s"Parent actor created new user Actor $actorRef")
        logger.debug(s"Parent Actor  Send XML message $request to:$actorRef for transactionId:${transNum}")
        context.watch(actorRef)
        val newUserActors = userActors + (transNum -> actorRef)
        logger.debug(s"Parent Actor newUserActors $newUserActors")
        actorRef ! SpcWSXmlMessage(out, session, xmlMsg)
        context.become(handleScpMessages( newUserActors))
      }{ actorRef =>
        logger.debug(s"Parent actor sending messages to existing Actor $actorRef")
        logger.debug(s"Parent Actor  Send XML message $request to:$actorRef for transactionId:${transNum}")
        actorRef ! SpcWSXmlMessage(out, session, xmlMsg)

      }
    case Terminated(ref) =>
      logger.error(s"Actor user has been terminated $ref")
      userActors.filter(x => x._2 == ref).keys.headOption.map { stoppedActor =>
        context.become(handleScpMessages(userActors - stoppedActor))
      }

    case e => logger.error(s"Scp Parent Actor not handled message: $e" )
  }



  private lazy val logger = Logger(SpcParentActor.getClass)
}
