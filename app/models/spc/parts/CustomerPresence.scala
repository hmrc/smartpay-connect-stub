package models.spc.parts

import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait CustomerPresence

object CustomerPresence {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[CustomerPresence] = derived.oformat[CustomerPresence]()

  case object Present extends CustomerPresence { override def toString = "present" }
}

