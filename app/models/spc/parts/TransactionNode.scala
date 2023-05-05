package models.spc.parts


import play.api.libs.json.{Json, OFormat}

import scala.xml.{Node, Null, UnprefixedAttribute}

final case class TransactionNode(
                                  amountNode:           AmountNode,
                                  purchaseDescription:  PurchaseDescription,
                                  transactionActionO:   Option[TransactionAction],
                                  transactionTypeO:     Option[TransactionType],
                                  transactionSourceO:   Option[TransactionSource],
                                  transactionCustomerO: Option[CustomerPresence]
                                ) extends SpcXmlNode {

  def toXml: Node = {
    val transaction =
    //    type="purchase" action="auth_n_settle" source={transactionSource} customer="present"
      <TRANSACTION>
        { amountNode.toXml }
        <DESCRIPTION>{ purchaseDescription.value }</DESCRIPTION>
      </TRANSACTION>
    val transactionWithAction = transactionActionO.fold(transaction)(transactionAction => transaction % new UnprefixedAttribute("action", transactionAction.toString, Null))
    val transactionWithType = transactionTypeO.fold(transactionWithAction)(transactionType => transactionWithAction % new UnprefixedAttribute("type", transactionType.toString, Null))
    val transactionWithSource = transactionSourceO.fold(transactionWithType)(transactionSource => transactionWithType % new UnprefixedAttribute("source", transactionSource.toString, Null))
    val transactionWithCustomer = transactionCustomerO.fold(transactionWithSource)(transactionCustomer => transactionWithSource % new UnprefixedAttribute("customer", transactionCustomer.toString, Null))
    transactionWithCustomer
  }
}

object TransactionNode {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[TransactionNode] = Json.format[TransactionNode]
}
