/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format
import utils.EnumFormat

import scala.collection.immutable

//final case class InteractionCategory(value: String) extends AnyVal

sealed trait InteractionCategory extends EnumEntry

object InteractionCategory {
  import InteractionCategories._
  implicit val format: Format[InteractionCategory] = EnumFormat(InteractionCategories)

  def apply(category: String): InteractionCategory = {
    category match {
      case "online"      => OnlineCategory
      case "card_reader" => CardReader
      case "pin_entry"   => PinEntry
      case "signature"   => Signature
      case "transaction" => Transaction
      case x             => throw new RuntimeException(s"Unknown InteractionCategory: $x")
    }
  }
}

object InteractionCategories extends Enum[InteractionCategory] {
  final case object OnlineCategory extends InteractionCategory { override def toString: String = "online" }
  final case object CardReader extends InteractionCategory { override def toString: String = "card_reader" }
  final case object PinEntry extends InteractionCategory { override def toString: String = "pin_entry" }
  final case object Signature extends InteractionCategory { override def toString: String = "signature" }
  final case object Transaction extends InteractionCategory { override def toString: String = "transaction" }
  override def values: immutable.IndexedSeq[InteractionCategory] = findValues
}
