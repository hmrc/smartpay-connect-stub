/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

import scala.xml.Node

final case class ErrorNode(code: String, description: String) extends SpcXmlNode {

  def toXml: Node = {
    <ERROR code={ code }>{ description }</ERROR>
  }
}

