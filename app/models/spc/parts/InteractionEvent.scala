/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

/**
 * SPC- Smart Pay Connect - Interaction Node events
 */
//final case class InteractionEvent(value: String) extends AnyVal


import enumeratum.{Enum, EnumEntry}
import play.api.libs.json.Format
import utils.EnumFormat

import scala.collection.immutable

/**
 * SPC- Smart Pay Connect - Interaction Node events
 */
sealed trait InteractionEvent extends EnumEntry

object InteractionEvent {
  import InteractionEvents._
  implicit val format: Format[InteractionEvent] = EnumFormat(InteractionEvents)

  def apply(category: String): InteractionEvent = {
    category match {
      case "use_chip"          => UseChip
      case "in_progress"       => InProgress
      case "success"           => EventSuccess
      case "processing"        => Processing
      case "use_chip_reinsert" => UseChipReinsert
      case "use_another_card"  => UseAnotherCard
      case "started"           => StartedEvent
      case "failed_retry"      => FailedRetry
      case "fallforward"       => Fallforward
      case x                   => throw new RuntimeException(s"Unknown InteractionEvent: $x")
    }
  }
}

object InteractionEvents extends Enum[InteractionEvent] {
  final case object UseChip extends InteractionEvent { override def toString: String = "use_chip" }
  final case object InProgress extends InteractionEvent { override def toString: String = "in_progress" }
  final case object EventSuccess extends InteractionEvent { override def toString: String = "success" }
  final case object Processing extends InteractionEvent { override def toString: String = "processing" }
  final case object UseChipReinsert extends InteractionEvent { override def toString: String = "use_chip_reinsert" }
  final case object UseAnotherCard extends InteractionEvent { override def toString: String = "use_another_card" }
  final case object StartedEvent extends InteractionEvent { override def toString: String = "started" }
  final case object FailedRetry extends InteractionEvent { override def toString: String = "failed_retry" }
  final case object Fallforward extends InteractionEvent { override def toString: String = "fallforward" }
  override def values: immutable.IndexedSeq[InteractionEvent] = findValues
}
