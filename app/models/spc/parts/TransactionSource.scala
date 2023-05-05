package models.spc.parts

import julienrf.json.derived
import play.api.libs.json.OFormat

/**
 * SPC- Smart Pay Connect - Interaction Node events
 */
sealed trait TransactionSource {
  def receiptValue: String = this.toString
}

object TransactionSource {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[TransactionSource] = derived.oformat[TransactionSource]()

  /**
   * Standard method of transaction when user insert a card into PED
   */
  final case object Icc extends TransactionSource {
    override def toString: String = "icc"
    override def receiptValue: String = "Chip and PIN"
  }
  final case object Keyed extends TransactionSource { override def toString: String = "keyed" }
  final case object Swiped extends TransactionSource { override def toString: String = "swiped" }
  final case object ContactlessEMV extends TransactionSource { override def toString: String = "contactlessEMV" }
  final case object ContactlessMSD extends TransactionSource { override def toString: String = "contactlessMSD" }
}
