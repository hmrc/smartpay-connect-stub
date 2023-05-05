package controllers

import models.spc.SpcXmlMessage
import play.api.{Configuration, Logging}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SpcRestController @Inject()(
                                    val controllerComponents:  MessagesControllerComponents,
                                    configuration:             Configuration
                                  )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport with Logging {

  def sendMessage(): Action[AnyContent] = Action { implicit request =>
    val spcRequestMessageXml: SpcXmlMessage = {
      val bodyText = request.body.asText.getOrElse(sys.error(s"Expected XML but received empty body"))
      SpcXmlMessage(bodyText)
    }
    val c = spcRequestMessageXml.parseSpcRequestMessage
      Ok
  }

  def getRecent(): Action[AnyContent] = Action { implicit request =>
    Ok
  }


}
