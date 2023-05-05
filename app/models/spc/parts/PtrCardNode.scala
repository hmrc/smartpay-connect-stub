/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

final case class PtrCardNode(
    currency:      CurrencyNum,
    country:       Country,
    endDate:       String,
    startDate:     Option[String],
    pan:           CardPan,
    cardType:      CardType,
    applicationId: String,
    seqNum:        Option[String]
) extends SpcXmlNode
