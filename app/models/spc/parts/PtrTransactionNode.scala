/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc.parts

import models.TransactionReference

//TODO - provide current date time in message
final case class PtrTransactionNode(
    amountNode:             AmountNode,
    transactionActionO:     Option[TransactionAction],
    transactionTypeO:       Option[TransactionType],
    transactionSourceO:     Option[TransactionSource],
    transactionCustomerO:   Option[CustomerPresence],
    transactionReferenceO:  Option[TransactionReference],
    cardVerificationMethod: CardVerificationMethod,
    transactionDate:        Option[String],
    transactionTime:        Option[String]
) extends SpcXmlNode
