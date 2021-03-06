/*
 * Copyright 2022 HM Revenue & Customs
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

object StubTestData {
  val customerReceipts =
    """<![CDATA[AID: A0000000031010
      |Visa Credit
      |Card: ************0011
      |PAN Seq Nr: 01
      |
      |ICC
      |SALE
      |TOTAL: GBP10.10
      |
      |SIGNATURE Verified
      |
      |Auth: D12345
      |Merchant: **76543
      |TID: ****2074
      |Trans no: 000624b1dbb9c0000e5a9174e3e
      |Date: 04/04/22 Time: 17:36:03
      |
      |Please retain for your records
      |
      |CUSTOMER COPY]]>""".stripMargin

  val customerDuplicateReceipt =
    """<|![CDATA[* * DUPLICATE * *
      |
      |AID: A0000000031010
      |Visa Credit
      |Card: ************0011
      |PAN Seq Nr: 01
      |
      |ICC
      |SALE
      |TOTAL: GBP10.10
      |
      |SIGNATURE Verified
      |
      |Auth: D12345
      |Merchant: **76543
      |TID: ****2074
      |Trans no: 000624b1dbb9c0000e5a9174e3e
      |Date: 04/04/22 Time: 17:36:03
      |
      |Please retain for your records
      |
      |CUSTOMER COPY]]>""".stripMargin

  val securityReceipt = "*** Data Removed for Security ***"

  val transactionReference = TransactionReference("8c1d4648-a57a-4dbd-a272-d4451d70474b")

  val incorrectMessageFlowErrorNode = ErrorNode("100003", "Incorrect Message Flow")

  val VisaCredit = PaymentCard(Currency.Usd, Country.US, "****-**", "2009-07-01", "476173******0011", CardType.VisaCredit)

  val merchantSignatureReceipt = ReceiptNode(
    receiptType            = ReceiptTypes.MerchantSignatureReceipt,
    applicationId          = "A0000000031010",
    authCode               = "D12345",
    cardSchema             = CardType.VisaCredit,
    currencyCode           = Currency.Gbp,
    customerPresence       = CustomerPresence.present,
    finalAmount            = AmountInPence(1000),
    merchantNumber         = MerchantNumber("9876543"),
    cardPan                = CardPan("476173******0011"),
    panSequence            = "01",
    terminalId             = TerminalId("23212075"),
    transactionSource      = TransactionSources.Icc,
    totalAmount            = AmountInPence(1000),
    transactionDate        = "2022-04-04",
    transactionTime        = "17:36:03",
    transactionType        = TransactionTypes.Purchase,
    cardVerificationMethod = CardVerificationMethod.signature
  )

  val customerReceipt = ReceiptNode(
    receiptType            = ReceiptTypes.CustomerReceipt,
    applicationId          = "A0000000031010",
    authCode               = "D12345",
    cardSchema             = CardType.VisaCredit,
    currencyCode           = Currency.Gbp,
    customerPresence       = CustomerPresence.present,
    finalAmount            = AmountInPence(1000),
    merchantNumber         = MerchantNumber("9876543"),
    cardPan                = CardPan("476173******0011"),
    panSequence            = "01",
    terminalId             = TerminalId("23212075"),
    transactionSource      = TransactionSources.Icc,
    totalAmount            = AmountInPence(1000),
    transactionDate        = "2022-04-04",
    transactionTime        = "17:36:03",
    transactionType        = TransactionTypes.Purchase,
    cardVerificationMethod = CardVerificationMethod.signature
  )


}
