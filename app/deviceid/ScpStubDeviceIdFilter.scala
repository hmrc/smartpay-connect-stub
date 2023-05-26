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

package deviceid

import akka.stream.Materializer
import cats.implicits.catsSyntaxEq
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ScpStubDeviceIdFilter @Inject() ()(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    rh
      .cookies
      .find(_.name === SpcStubDeviceId.cookieName)
      //add SpcStubDeviceId if not there
      .fold(f(rh).map(_.withCookies(makeDeviceIdCookie())))(_ => f(rh))
  }

  private def makeDeviceIdCookie(): Cookie = Cookie(
    name   = SpcStubDeviceId.cookieName,
    value  = SpcStubDeviceId.fresh().value,
    maxAge = Some(315360000) //10 years
  )

}

