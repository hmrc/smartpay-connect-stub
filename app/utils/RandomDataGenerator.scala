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

package utils

import models.TransactionReference

import javax.inject.Singleton
import scala.util.Random.self

@Singleton
object RandomDataGenerator {

  //7befa217-5291-49a1-98f5-d9dcaa13c6a3
  def generateTransactionReference: TransactionReference =
    TransactionReference(s"${randomAlphaNumeric(8)}-${randomAlphaNumeric(4)}-${randomAlphaNumeric(4)}-${randomAlphaNumeric(8)}")

  private def randomAlphaNumeric(length: Int): String = alphaNumeric.take(length).mkString

  private def alphaNumeric: Stream[Char] = {
      def nextAlphaNumeric: Char = {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyz"
        chars charAt (self nextInt chars.length)
      }
    Stream continually nextAlphaNumeric
  }

}
