package models.spc.parts
import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait ReceiptType {
  def getPrintedName: String
}

object ReceiptType {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[ReceiptType] = derived.oformat[ReceiptType]()

  final case object Merchant extends ReceiptType {
    override def getPrintedName: String = "merchant"
  }
  final case object MerchantSignature extends ReceiptType {
    override def getPrintedName: String = "merchant_signature"
  }
  final case object Customer extends ReceiptType {
    override def getPrintedName: String = "customer"
  }
  final case object Pos extends ReceiptType {
    override def getPrintedName: String = "pos"
  }

}
