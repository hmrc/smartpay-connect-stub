package models.spc

import models.spc.parts.MessageNode
import play.api.Logger

import scala.xml.{Attribute, Elem, Node}

object SpcExtensionMethods {

  implicit class SpcXmlMessageExt(val spcXmlMessage: SpcXmlMessage) {
    def parseSpcResponseMessage: SpcResponseMessage = SpcParsingService.parseSpcResponseMessage(spcXmlMessage.elem)

    def parseSpcRequestMessage: SpcRequestMessage = SpcParsingService.parseSpcRequestMessage(spcXmlMessage.elem)
  }

  implicit class SpcRequestMessageExt(val n: SpcRequestMessage) {
    def toXml: Node = n match {
      case n: PedLogOn => ToXml.toXml(n)
      case n: SubmitPayment => ToXml.toXml(n)
      case n: ProcessTransaction => ToXml.toXml(n)
      case n: UpdatePaymentEnhancedResponse => ToXml.toXml(n)
      case n: PosPrintReceiptResponse => ToXml.toXml(n)
      case n: Finalise => ToXml.toXml(n)
      case n: CompleteTransaction => ToXml.toXml(n)
      case n: PedLogOff => ToXml.toXml(n)
      case n: CancelTransaction => ToXml.toXml(n)
      case n: GetTransactionDetails => ToXml.toXml(n)
    }

    def messageNode: MessageNode = n match {
      case n: PedLogOn => n.messageNode
      case n: SubmitPayment => n.messageNode
      case n: ProcessTransaction => n.messageNode
      case n: UpdatePaymentEnhancedResponse => n.messageNode
      case n: PosPrintReceiptResponse => n.messageNode
      case n: Finalise =>n.messageNode
      case n: CompleteTransaction => n.messageNode
      case n: PedLogOff => n.messageNode
      case n: CancelTransaction => n.messageNode
      case n: GetTransactionDetails => n.messageNode
    }
  }

    def addNode(to: Node, newNode: Node): Node = to match {
      case Elem(prefix, label, attributes, scope, child@_*) => Elem(prefix, label, attributes, scope, true, child ++ newNode: _*)
      case _ =>
        logger.warn("could not find node"); to
    }

    private val logger = Logger(this.getClass)

    def addAttribute(to: Elem, attribute: Attribute): Elem = to match {
      case elem: Elem => elem % attribute
      case _ => logger.warn("could not find node"); to
    }


  implicit class SuperNode(val to: Node) extends AnyVal {
    def maybeAddNode(maybeNewNode: Option[Node]): Node = maybeNewNode.fold(to)(newNode => addNode(to, newNode))
  }

//  implicit class SpcResponseMessageExt(val n: SpcResponseMessage) {
//
//  }
}
