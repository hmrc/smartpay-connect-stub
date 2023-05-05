/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

import scala.xml.Node

object HeaderNode extends SpcXmlNode {

  def toXml: Node = {
    <HEADER>
      <BUILD>
        <VERSION>1.34.0</VERSION>
      </BUILD>
    </HEADER>
  }
}
