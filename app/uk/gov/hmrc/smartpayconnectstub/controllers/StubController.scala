package uk.gov.hmrc.smartpayconnectstub.controllers

import play.api.libs.json.Json.toJson
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.smartpayconnectstub.models.{SetPathRequest, StubPath}
import uk.gov.hmrc.smartpayconnectstub.models.StubPaths.Happy
import uk.gov.hmrc.smartpayconnectstub.repository.StubRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class StubController @Inject() (repository: StubRepository, cc: ControllerComponents)(implicit executionContext: ExecutionContext) extends BackendController(cc){

  def storePathForDeviceId(): Action[SetPathRequest] = Action.async(parse.json[SetPathRequest]) { implicit request =>
    val chosenPath: StubPath = request.body.stubPath match {
      case "Happy" => Happy
      case _ => Happy
    }

    repository.upsert("deviceId",  chosenPath).map { _ =>
      Ok(toJson("Great Success"))
    }
  }
}
