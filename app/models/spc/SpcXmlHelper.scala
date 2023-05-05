//package models.spc
//
//import play.api.Logger
//
//import scala.xml.{Attribute, Elem, Node}
//
///**
// * SPC- Smart Pay Connect helper functions
// */
//object SpcXmlHelper {
//  def getSpcXmlMessage(node: Node): Option[SpcMessage] = {
//    (node \\ "POI_MSG" \ "@type").text match {
//      case "interaction" =>
//        (node \\ "INTERACTION" \ "@name").text match {
//          case PedLogOn.name => Some(PedLogOn.fromXml(node))
//          case PosPrintReceiptResponse.name => Some(PosPrintReceiptResponse.fromXml(node))
//          case PedLogOff.name => Some(PedLogOff.fromXml(node))
//          case _ => None
//        }
//      case "submittal" =>
//        (node \\ "SUBMIT" \ "@name").text match {
//          case SubmitPayment.name => Some(SubmitPayment.fromXml(node))
//          case _ => None
//        }
//      case "transactional" =>
//        (node \\ "TRANS" \ "@name").text match {
//          case ProcessTransaction.name => Some(ProcessTransaction.fromXml(node))
//          case UpdatePaymentEnhancedResponse.name => Some(UpdatePaymentEnhancedResponse.fromXml(node))
//          case Finalise.name => Some(Finalise.fromXml(node))
//          case CancelTransaction.name => Some(CancelTransaction.fromXml(node))
//          case CompleteTransaction.name => Some(CompleteTransaction.fromXml(node))
//          case _ => None
//        }
//      case "error" => Some(ErrorMessage.fromXml(node))
//      case _ => None
//    }
//  }
//
//  def getSpcXmlMessageNode(node: Node): MessageNode = {
//    MessageNode.fromXml(node)
//  }
//
//  def addNode(to: Node, newNode: Node): Node = to match {
//    case Elem(prefix, label, attributes, scope, child@_*) => Elem(prefix, label, attributes, scope, true, child ++ newNode: _*)
//    case _ =>
//      logger.warn("could not find node"); to
//  }
//
//  private val logger = Logger(this.getClass)
//
//  def addAttribute(to: Elem, attribute: Attribute): Elem = to match {
//    case elem: Elem => elem % attribute
//    case _ => logger.warn("could not find node"); to
//  }
//
//  implicit class SuperNode(val to: Node) extends AnyVal {
//    def maybeAddNode(maybeNewNode: Option[Node]): Node = maybeNewNode.fold(to)(newNode => addNode(to, newNode))
//  }
//}
