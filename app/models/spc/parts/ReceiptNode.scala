/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

import models.AmountInPence

final case class ReceiptNode(
    receiptType:            ReceiptType,
    applicationId:          Option[String],
    authCode:               Option[String],
    cardSchema:             Option[CardType],
    currencyCode:           Option[CurrencyCode],
    customerPresence:       Option[CustomerPresence],
    finalAmount:            Option[AmountInPence],
    merchantNumber:         Option[MerchantNumber],
    cardPan:                Option[CardPan],
    panSequence:            Option[String],
    terminalId:             Option[TerminalId],
    transactionSource:      Option[TransactionSource],
    totalAmount:            Option[AmountInPence],
    transactionDate:        Option[String],
    transactionTime:        Option[String],
    transactionType:        Option[TransactionType],
    cardVerificationMethod: Option[CardVerificationMethod],
    transactionResponse:    Option[String]
) extends SpcXmlNode
