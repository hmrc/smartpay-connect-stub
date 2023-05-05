/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import models.spc.ErrorNode
import models.spc.parts.ErrorNode

import java.text.SimpleDateFormat
import java.util.Date

object StubUtil {
  //  val customerReceipts =
  //    """<![CDATA[AID: A0000000031010
  //      |Visa Credit
  //      |Card: ************0011
  //      |PAN Seq Nr: 01
  //      |
  //      |ICC
  //      |SALE
  //      |TOTAL: GBP10.10
  //      |
  //      |SIGNATURE Verified
  //      |
  //      |Auth: D12345
  //      |Merchant: **76543
  //      |TID: ****2074
  //      |Trans no: 000624b1dbb9c0000e5a9174e3e
  //      |Date: 04/04/22 Time: 17:36:03
  //      |
  //      |Please retain for your records
  //      |
  //      |CUSTOMER COPY]]>""".stripMargin
  //
  //  val customerDuplicateReceipt =
  //    """<|![CDATA[* * DUPLICATE * *
  //      |
  //      |AID: A0000000031010
  //      |Visa Credit
  //      |Card: ************0011
  //      |PAN Seq Nr: 01
  //      |
  //      |ICC
  //      |SALE
  //      |TOTAL: GBP10.10
  //      |
  //      |SIGNATURE Verified
  //      |
  //      |Auth: D12345
  //      |Merchant: **76543
  //      |TID: ****2074
  //      |Trans no: 000624b1dbb9c0000e5a9174e3e
  //      |Date: 04/04/22 Time: 17:36:03
  //      |
  //      |Please retain for your records
  //      |
  //      |CUSTOMER COPY]]>""".stripMargin

  //  val securityReceipt = "*** Data Removed for Security ***"

  val TRANSACTION_REFERENCE = TransactionReference("8c1d4648-a57a-4dbd-a272-DUMMY")

  val APPLICATION_ID = "A0000000031010"
  val AUTH_CODE = "150348"
  val VERSION = "1.34.0"
  val MERCHANT_NUMBER = "6571327"
  val TERMINAL_ID = "90012002"

  val incorrectMessageFlowErrorNode = ErrorNode("100003", "Incorrect Message Flow")

  //That matching our test card
  val MasterDebit = PaymentCard(CurrencyNum.Eur, Country.Bel, "2025-12-01", "2004-01-01", "5457210089020012", CardSchema.MasterDebit, "01", Some(AmountInPence("99,999,999.99")))
  val VisaCredit = PaymentCard(CurrencyNum.Usd, Country.Uk, "2025-12-01", "2004-01-01", "4761730000000011", CardSchema.VisaCredit, "01", Some(AmountInPence("99,999,999.99")))
  val VisaCredit_BinCheckFail = PaymentCard(CurrencyNum.Usd, Country.Uk, "2025-12-01", "2004-01-01", "9999930000000011", CardSchema.VisaCredit, "01", Some(AmountInPence("99,999,999.99")))

  def getCurrentDateTime: Long = new Date().getTime
  def formatReceiptDate(datetime: Long) = new SimpleDateFormat("dd/MM/yyyy").format(datetime)
  def formatReceiptTime(datetime: Long) = new SimpleDateFormat("HH:mm:ss").format(datetime)
  def formatTransactionDate(datetime: Long) = new SimpleDateFormat("yyyy-MM-dd").format(datetime)
  def formatTransactionTime(datetime: Long) = new SimpleDateFormat("HH:mm:ss").format(datetime)

  //  val merchantSignatureReceipt = ReceiptNode(
  //    receiptType            = ReceiptTypes.MerchantSignatureReceipt,
  //    applicationId          = "A0000000031010",
  //    authCode               = "D12345",
  //    cardSchema             = CardType.VisaCredit,
  //    currencyCode           = Currency.Gbp,
  //    customerPresence       = CustomerPresence.present,
  //    finalAmount            = AmountInPence(1000),
  //    merchantNumber         = MerchantNumber("9876543"),
  //    cardPan                = CardPan("476173******0011"),
  //    panSequence            = "01",
  //    terminalId             = TerminalId("23212075"),
  //    transactionSource      = TransactionSources.Icc,
  //    totalAmount            = AmountInPence(1000),
  //    transactionDate        = "2022-04-04",
  //    transactionTime        = "17:36:03",
  //    transactionType        = TransactionTypes.Purchase,
  //    cardVerificationMethod = CardVerificationMethod.signature
  //  )

  //  val customerReceipt = ReceiptNode(
  //    receiptType            = ReceiptTypes.CustomerReceipt,
  //    applicationId          = "A0000000031010",
  //    authCode               = "D12345",
  //    cardSchema             = CardType.VisaCredit,
  //    currencyCode           = Currency.Gbp,
  //    customerPresence       = CustomerPresence.present,
  //    finalAmount            = AmountInPence(1000),
  //    merchantNumber         = MerchantNumber("9876543"),
  //    cardPan                = CardPan("476173******0011"),
  //    panSequence            = "01",
  //    terminalId             = TerminalId("23212075"),
  //    transactionSource      = TransactionSources.Icc,
  //    totalAmount            = AmountInPence(1000),
  //    transactionDate        = "2022-04-04",
  //    transactionTime        = "17:36:03",
  //    transactionType        = TransactionTypes.Purchase,
  //    cardVerificationMethod = CardVerificationMethod.signature
  //  )

}
