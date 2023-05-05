package models.spc.parts


import models.AmountInPence
import play.api.Logger
import play.api.libs.json.{Json, OFormat}

import scala.xml.{Elem, Node}

final case class AmountNode(
                             totalAmount:  AmountInPence,
                             currency:     CurrencyNum,
                             country:      Country,
                             finalAmountO: Option[AmountInPence]) extends SpcXmlNode {

  def toXml: Node = {
    val totalAmountNode =
      <AMOUNT currency={ currency.value } country={ country.value }>
        <TOTAL>{ totalAmount.value }</TOTAL>
      </AMOUNT>

    def addNode(to: Node, newNode: Node): Node = to match {
      case Elem(prefix, label, attributes, scope, child @ _*) => Elem(prefix, label, attributes, scope, true, child ++ newNode: _*)
      case _ => Logger("AmountNode").error("could not find node"); to
    }

    //TODO rewrite it without using mutable stuff
    finalAmountO.map { finalAmount =>
      addNode(totalAmountNode, { <FINAL>{ finalAmount.value }</FINAL> })
    }.getOrElse(totalAmountNode)
  }
}

object AmountNode {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[AmountNode] = Json.format[AmountNode]
}
