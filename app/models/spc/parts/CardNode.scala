/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

import scala.xml.Node

final case class CardNode(
    currency:  CurrencyNum,
    country:   Country,
    endDate:   String,
    startDate: String,
    pan:       CardPan,
    cardType:  CardType
) extends SpcXmlNode {

  def toXml: Node = {
    <CARD range="0" currency={ currency.value } country={ country.value }>
      <PAN end={ endDate } start={ startDate } seqNum="01">{ pan.value }</PAN>
      <APPLICATION id="A0000000031010">{ cardType.value }</APPLICATION>
      <TOKENS>
        <TOKEN origin="central">DB89CDDF-4A25-4C46-E053-11221FACA840</TOKEN>
      </TOKENS>
    </CARD>
  }
}

