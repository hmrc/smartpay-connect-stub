/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.smartpayconnectstub.controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartpayconnectstub.models.{PedLogOn, SpcMessageHelper, SubmitPayment}

import javax.inject.{Inject, Singleton}
import scala.xml.{Elem, Node, XML}

@Singleton()
class WebsocketController @Inject()(cc: ControllerComponents)(implicit mat: Materializer, actorSystem: ActorSystem)
    extends BackendController(cc) {

  def ws(): WebSocket = WebSocket.accept[String,String] { implicit request =>
    ActorFlow.actorRef { out =>
      WebSocketActor.props(out)
    }
  }
}

import akka.actor._

object WebSocketActor {
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

class WebSocketActor(out: ActorRef) extends Actor {
val logger = Logger(WebSocketActor.getClass)



  def receive = {
    case request: String =>
      logger.info(s"Got message $request")
      val xmlMsg:Elem = XML.loadString(request)
      val response:Node = SpcMessageHelper.getSpcXMLMessage(xmlMsg) match {
        case msg:PedLogOn => SpcMessageHelper.createPedLogOnResponse(msg,"success").toXML
        case msg:SubmitPayment => SpcMessageHelper.createSubmitPaymentResponse(msg,"success").toXML
        case x =>  throw new RuntimeException(s"Unknown SmartPay Connect message: $x")
      }
      logger.info(s"Reply $response")
      out ! response.toString
  }
}
