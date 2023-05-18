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

package controllers

import behaviour.{BDefined, BDone, Behaviour}
import flow.{BehaviourService, SpcBehaviour}
import models.TranResults.SuccessResult
import models._
import play.api.libs.json.{Json, OFormat}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.RequestSupport.deviceId

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}
import scala.xml.Elem

@Singleton()
class StubController @Inject() (
    val controllerComponents: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends FrontendBaseController {

  def ping(): Action[AnyContent] = Action(Ok)

  def pingSpc(): Action[AnyContent] = Action(Ok)

  def sendMessage(): Action[SpcRequestMessage] = Action(sendMessageRequestParser) { implicit request =>
    val spcRequestMessage: SpcRequestMessage = request.body
    val transactionId: TransactionId = spcRequestMessage.messageNode.transNum
    val behaviour: SpcBehaviour = BehaviourService.getBehaviour(transactionId, deviceId)
    val unexpected: (List[SpcResponseMessage], BDone.type) = (List(Help.unexpectedRequestResponse(spcRequestMessage)), BDone)

    val (spcResponses: Seq[SpcResponseMessage], nextBehaviour: Behaviour[SpcRequestMessage, Seq[SpcResponseMessage]]) = behaviour match {
      case BDone        => unexpected
      case BDefined(pf) => if (pf.isDefinedAt(spcRequestMessage)) pf(spcRequestMessage) else unexpected
    }

    nextBehaviour match {
      case BDone             => BehaviourService.removeBehaviour(transactionId)
      case b: BDefined[_, _] => BehaviourService.updateBehaviour(transactionId, b)
    }

    Ok(Json.toJson(SendMessageResponse(spcResponses.map(_.toXmlString))))
  }

  private val sendMessageRequestParser: BodyParser[SpcRequestMessage] = {
    val missingBody: Either[Result, String] = Left(BadRequest("Missing spc request in body"))

      def invalidXml(err: String): Either[Result, Elem] = Left(BadRequest(s"could not parse XML: $err"))
    val invalidSpcMessage: Either[Result, SpcMessage] = Left(BadRequest("could not parse SpcMessage"))
      def invalidSpcRequestMessage(spcMessage: SpcMessage): Either[Result, SpcRequestMessage] = Left(BadRequest(s"could not parse SpcRequestMessage: ${spcMessage.name} is not SpcRequestMessage"))

    val parseMaybeString: BodyParser[Option[String]] = parse
      .anyContent
      .map(_.asText)

    val parseString: BodyParser[String] = parseMaybeString
      .validate[String](_
        .filterNot(_.isEmpty)
        .fold[Either[Result, String]](missingBody)(Right(_))
      )

    val parseXml: BodyParser[Elem] = parseString
      .validate[Elem]((t: String) =>
        Try(scala.xml.XML.loadString(t)) match {
          case Success(e: Elem) => Right(e)
          case Failure(e)       => invalidXml(e.toString)
        }
      )

    val parseSpcMessage: BodyParser[SpcMessage] = parseXml
      .validate[SpcMessage](xml => SpcXmlHelper
        .getSpcXmlMessage(xml)
        .fold(invalidSpcMessage)(Right(_))
      )

    val parseSpcRequestMessage: BodyParser[SpcRequestMessage] = parseSpcMessage
      .validate[SpcRequestMessage] {
        case r: SpcRequestMessage => Right(r)
        case r                    => invalidSpcRequestMessage(r)
      }
    parseSpcRequestMessage
  }

}

final case class SendMessageRequest(
    spcMessage: String
)

object Help {
  def unexpectedRequestResponse(unexpected: SpcRequestMessage, hint: String = ""): SpcResponseMessage = {
    val errorNode = ErrorNode("XXXXXX", s"Unexpected message [${unexpected.name}] for selected stub flow: $hint")
    val errorsNode = ErrorsNode(Seq(errorNode))
    val errorResponse = ErrorMessage(HeaderNode(), unexpected.messageNode, errorsNode, SuccessResult)
    errorResponse
  }
}

final case class SendMessageResponse(
    spcResponses: Seq[String]
)

object SendMessageResponse {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[SendMessageResponse] = Json.format[SendMessageResponse]
}
