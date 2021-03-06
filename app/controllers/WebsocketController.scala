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

package controllers

import actors.{SpcParentActor, SpcSessionActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import repository.StubRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RequestSupport

import javax.inject.{Inject, Singleton}


@Singleton()
class WebsocketController @Inject()(
                                     val controllerComponents: MessagesControllerComponents,
                                     system: ActorSystem,
                                     repository: StubRepository)(implicit mat: Materializer, actorSystem: ActorSystem)
    extends FrontendBaseController {

  val scpParentActor = system.actorOf(SpcParentActor.props())

  def ws(): WebSocket = WebSocket.accept[String,String] { implicit request =>
    val mayBeDeviceId = RequestSupport.hc(request.withBody()).deviceID

    ActorFlow.actorRef { out =>
        SpcSessionActor.props(out, scpParentActor, mayBeDeviceId, repository)
      }
  }
}
