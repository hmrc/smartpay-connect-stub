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

import akka.actor.{Actor, ActorIdentity, ActorRef, Identify, Props, Terminated}
import com.google.inject.Provides
import play.api.Logger
import play.api.libs.concurrent.ActorModule
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
    logger.info(s"Stopping Parent Actor!!!!! $self")
    super.postStop()
  }

  override def preStart(): Unit = {
    logger.info(s"Starting Parent Actor!!!!! $self")
    super.preStart()
  }

//  val identifyId = 1
//
//
//  def receive: Receive = waiting
//
//  def waiting:Receive = {
//    case SpcWSStringMessage(out,request) =>
//      logger.info(s"Parent Actor received first message $request")
//      val xmlMsg:Elem = XML.loadString(request)
//      val transNum = SpcXmlHelper.getSpcXmlMessageNode(xmlMsg).transNum
//      logger.info(s"Parent Actor will look for user actor ${transNum.value} ")
//      context.actorSelection(s"../${transNum.value}") ! Identify(identifyId)
//      context.become(userActorSearch(out, xmlMsg, transNum))
//  }
//
//  def userActorSearch(out:ActorRef, xmlMsg:Elem, transNum:TransactionId):Receive = {
//    case ActorIdentity(`identifyId`, Some(ref)) =>
//      logger.info(s"Parent Actor fount user actor ${transNum.value} with $ref")
//      context.watch(ref)
//      ref ! SpcWSXmlMessage(out,xmlMsg)
//      context.become(active(out, ref))
//    case ActorIdentity(`identifyId`, None) =>
//      logger.info(s"Parent Actor did NOT find user actor ${transNum.value} and need to create new")
//      val actorRef = context.actorOf(ScpUserActor.props(),s"${transNum.value}")
//      context.watch(actorRef)
//      actorRef ! xmlMsg
//      context.become(active(out, actorRef))
//  }
//
//  def active(out:ActorRef, userActor: ActorRef):Receive = {
//    case request: String =>
//      logger.info(s"Parent Actor received message $request")
//      val xmlMsg:Elem = XML.loadString(request)
//      userActor ! xmlMsg
//
//    case Terminated(ref) =>
//      logger.info(s"Parent Actor received user actor terminated $ref")
//      //self stop maybe but do not have to
//
//    case e => logger.error(s"Parent Actor received not handled message: $e" )
//  }

  def receive: Receive = handleScpMessages(Map.empty[TransactionId,ActorRef])

  def handleScpMessages(userActors:Map[TransactionId, ActorRef]) : Receive = {
    case SpcWSStringMessage(out,request) =>
      logger.info(s"Parent Actor got message $request")
      logger.info(s"Parent Actor userActors $userActors")
      val xmlMsg:Elem = XML.loadString(request)
      val transNum = SpcXmlHelper.getSpcXmlMessageNode(xmlMsg).transNum

      userActors.get(transNum).fold {
        logger.info(s"Parent actor is going to create user actor with name ${transNum.value}")
        val actorRef = context.actorOf(ScpUserActor.props(),s"${transNum.value}")
        logger.info(s"Parent actor created new user Actor $actorRef")
        logger.info(s"Parent Actor  Send XML message $request to:$actorRef for transactionId:${transNum}")
        context.watch(actorRef)
        val newUserActors = userActors + (transNum -> actorRef)
        logger.info(s"Parent Actor newUserActors $newUserActors")
        actorRef ! SpcWSXmlMessage(out,xmlMsg)
        context.become(handleScpMessages( newUserActors))
      }{ actorRef =>
        logger.info(s"Parent actor sending messages to existing Actor $actorRef")
        logger.info(s"Parent Actor  Send XML message $request to:$actorRef for transactionId:${transNum}")
        actorRef ! SpcWSXmlMessage(out,xmlMsg)

      }
    case Terminated(ref) =>
      logger.info(s"Actor user has been terminated $ref")
      userActors.filter(x => x._2 == ref).keys.headOption.map { stoppedActor =>
        context.become(handleScpMessages(userActors - stoppedActor))
      }

    case e => logger.error(s"Scp Parent Actor not handled message: $e" )
  }

//  def handleScpMessages(userActors:Map[TransactionId, ActorRef]) : Receive = {
//    case request: String =>
//      logger.info(s"Parent Actor got message $request")
//      logger.info(s"Parent Actor userActors $userActors")
//      val xmlMsg:Elem = XML.loadString(request)
//      val transNum = SpcXmlHelper.getSpcXmlMessageNode(xmlMsg).transNum
//
//      userActors.get(transNum).fold {
//        logger.info(s"Parent actor is going to create user actor with name ${transNum.value}")
//        val actorRef = context.actorOf(ScpUserActor.props(out, transNum),s"${transNum.value}")
//        logger.info(s"Parent actor created new user Actor $actorRef")
//        logger.info(s"Parent Actor  Send XML message $request to:$actorRef for transactionId:${transNum}")
//        context.watch(actorRef)
//        val newUserActors = userActors + (transNum -> actorRef)
//        logger.info(s"Parent Actor newUserActors $newUserActors")
//        actorRef ! xmlMsg
//        context.become(handleScpMessages( newUserActors))
//      }{ actorRef =>
//        logger.info(s"Parent actor sending messages to existing Actor $actorRef")
//        logger.info(s"Parent Actor  Send XML message $request to:$actorRef for transactionId:${transNum}")
//        actorRef ! xmlMsg
//
//      }
//    case Terminated(ref) =>
//      logger.info(s"Actor user has been terminated $ref")
//      userActors.filter(x => x._2 == ref).keys.headOption.map { stoppedActor =>
//        context.become(handleScpMessages(userActors - stoppedActor))
//      }
//
//    case e => logger.error(s"Scp Parent Actor not handled message: $e" )
//  }


  private lazy val logger = Logger(SpcParentActor.getClass)
}
