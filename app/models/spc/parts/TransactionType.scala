package models.spc.parts


import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait TransactionType {
  def receiptValue: String
}

object TransactionType {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[TransactionType] = derived.oformat[TransactionType]()

  case object Purchase extends TransactionType {
    override def toString: String = "purchase"
    override def receiptValue: String = "Payment"
  }
}
