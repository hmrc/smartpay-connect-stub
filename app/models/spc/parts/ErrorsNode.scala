/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

import scala.xml.Node

//TODO - DO parse all errors
final case class ErrorsNode(errorNode: Seq[ErrorNode]) extends SpcXmlNode {
  def toXml: Node = {
    <ERRORS>
      { errorNode.foreach(_.toXml) }
    </ERRORS>
  }
}
