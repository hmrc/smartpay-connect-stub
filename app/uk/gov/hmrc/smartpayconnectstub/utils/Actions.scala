package uk.gov.hmrc.smartpayconnectstub.utils

import play.api.mvc.{ActionBuilder, DefaultActionBuilder}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Actions @Inject() (
                          actionBuilder:      DefaultActionBuilder
                        )(implicit ec: ExecutionContext) {

}
