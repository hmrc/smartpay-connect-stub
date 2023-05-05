package models.spc

import models.{AmountInPence, TransactionNumber, TransactionReference, TransactionSources, TransactionTypes}
import models.spc.parts._
import models.SafeEquals._

import scala.xml.{Elem, Node, NodeSeq}

object SpcParsingService {

  /**
   * Parses received SpcResponseMessage from incoming Xml.
   */
  def parseSpcRequestMessage(node: Node): SpcRequestMessage = parsePoiMsg(node) match {
    case PoiMsg.Interaction => parseInteractionRequest(node)
    case PoiMsg.Submittal => parseSubmittalRequest(node)
    case PoiMsg.Transactional => parseTransactionalRequest(node)
    case PoiMsg.Error => throw new RuntimeException(s"Can't parse SpcRequestMessage: ${node.toString()}")
  }

  def parseSpcResponseMessage(node: Node): SpcResponseMessage = parsePoiMsg(node) match {
    //TODO: parse message results and errors, they are not always parsed
    case PoiMsg.Interaction => parseInteractionResponse(node)
    case PoiMsg.Submittal => parseSubmittalResponse(node)
    case PoiMsg.Transactional => parseTransactionalResponse(node)
    case PoiMsg.Error => parseErrorMessage(node)
  }

  private sealed trait PoiMsg

  private object PoiMsg {
    case object Interaction extends PoiMsg

    case object Submittal extends PoiMsg

    case object Transactional extends PoiMsg

    case object Error extends PoiMsg
  }

  private def parsePoiMsg(node: Node): PoiMsg = (node \\ "POI_MSG" \ "@type").text match {
    case "interaction" => PoiMsg.Interaction
    case "submittal" => PoiMsg.Submittal
    case "transactional" => PoiMsg.Transactional
    case "error" => PoiMsg.Error
    case _ => throw new RuntimeException(s"Can't parse SpcResponseMessage: ${node.toString()}")
  }

  private def parseTransactionalRequest(node: Node): SpcRequestMessage = (node \\ "TRANS" \ "@name").text match {
    case "processTransaction" => parsePt(node)
    case "getTransactionDetails" => parseGtd(node)
    case "updatePaymentEnhancedResponse" => parseUpdatePaymentEnhancedResponse(node)
    case "finalise" => parseFinalise(node)
    case _ => throw new RuntimeException(s"Can't parse SpcRequestMessage: ${node.toString()}")
  }

  private def parseTransactionalResponse(node: Node): SpcResponseMessage = (node \\ "TRANS" \ "@name").text match {
    case "processTransactionResponse" => parsePtr(node)
    case "getTransactionDetailsResponse" => parseGtdr(node)
    case "updatePaymentEnhanced" => parseUpdatePaymentEnhanced(node)
    case "posDecision" => parsePosDecisionMessage(node)
    case "finaliseResponse" => parseFinaliseResponse(node)
    case _ => throw new RuntimeException(s"Can't parse SpcResponseMessage: ${node.toString()}")
  }

  private def parseSubmittalRequest(node: Node): SpcRequestMessage = (node \\ "SUBMIT" \ "@name").text match {
    case "submitPayment" => parseSubmitPayment(node)
    case _ => throw new RuntimeException(s"Can't parse SpcRequestMessage: ${node.toString()}")
  }

  private def parseSubmittalResponse(node: Node): SpcResponseMessage = (node \\ "SUBMIT" \ "@name").text match {
    case "submitPaymentResponse" => parseSubmitPaymentResponse(node)
    case _ => throw new RuntimeException(s"Can't parse SpcResponseMessage: ${node.toString()}")
  }

  private def parseInteractionRequest(node: Node): SpcRequestMessage = (node \\ "INTERACTION" \ "@name").text match {
    case "pedLogOn" => parsePedLogOn(node)
    case "posPrintReceiptResponse" => parsePosPrintReceiptResponse(node)
    case "pedLogOff" => parsePedLogOff(node)
    case _ => throw new RuntimeException(s"Can't parse SpcRequestMessage: ${node.toString()}")
  }

  private def parseInteractionResponse(node: Node): SpcResponseMessage = (node \\ "INTERACTION" \ "@name").text match {
    case "pedLogOnResponse" => parsePedLogOnResponse(node)
    case "posDisplayMessage" => parsePosDisplayMessage(node)
    case "posPrintReceipt" => parsePosPrintReceipt(node)
    case "pedLogOffResponse" => parsePedLogOffResponse(node)
    case _ => throw new RuntimeException(s"Can't parse SpcResponseMessage: ${node.toString()}")
  }

  private def parseErrorsNode(node: Node): ErrorsNode = ErrorsNode(
    errorNode = (node \\ "TRANS" \ "ERRORS").toList.map(parseErrorNode)
  )

  private def parseErrorNode(node: Node): ErrorNode = {
    val code = (node \\ "ERROR" \ "@code").text
    val description = (node \\ "ERROR").text
    ErrorNode(code, description)
  }

  private def parseMessageNode(node: Node): MessageNode = MessageNode(
    transactionNumber = TransactionNumber((node \\ "MESSAGE" \ "TRANS_NUM").text),
    deviceId = DeviceId((node \\ "MESSAGE" \ "DEVICE_ID").text),
    sourceId = SourceId((node \\ "MESSAGE" \ "SOURCE_ID").text)
  )

  private def parsePedLogOn(node: Node): PedLogOn = PedLogOn(
    messageNode = parseMessageNode(node)
  )

  private def parsePedLogOnResponse(node: Node): PedLogOnResponse = PedLogOnResponse(
    messageNode = parseMessageNode(node),
    result = parseMessageResult((
      node \\ "INTERACTION" \ "RESULT"
      ).text),
    errors = parseErrorsNode(node)
  )

  private def parseMessageResult(text: String): MessageResult = text match {
    case "success" => MessageResult.SuccessResult
    case "failure" => MessageResult.FailureResult
    case x => throw new RuntimeException(s"Unknown MessageResults:[$x]")
  }

  private def parseInteractionNode(node: Node): InteractionNode = InteractionNode(
    category = InteractionCategory((node \\ "INTERACTION" \ "STATUS" \ "@category").text),
    event = InteractionEvent((node \\ "INTERACTION" \ "STATUS" \ "@event").text),
    prompt = InteractionPrompt((node \\ "INTERACTION" \ "PROMPT").text)
  )

  private def parsePosDisplayMessage(node: Node): PosDisplayMessage = PosDisplayMessage(
    messageNode = parseMessageNode(node),
    interactionNode = parseInteractionNode(node)
  )

  private def parsePosPrintReceipt(node: Node): PosPrintReceipt = PosPrintReceipt(
    messageNode = parseMessageNode(node),
    receiptNode = parseReceiptNode(node)
  )

  private def parsePosPrintReceiptResponse(node: Node): PosPrintReceiptResponse = PosPrintReceiptResponse(
    messageNode = parseMessageNode(node),
    result = parseMessageResult(
      (node \\ "INTERACTION" \ "RESPONSE").text
    )
  )

  private def parsePedLogOff(node: Node): PedLogOff = PedLogOff(
    messageNode = parseMessageNode(node)
  )

  private def parsePedLogOffResponse(node: Node): PedLogOffResponse = PedLogOffResponse(
    messageNode = parseMessageNode(node),
    result = parseMessageResult(
      (node \\ "INTERACTION" \ "RESULT").text
    )
  )

  private def parseErrorMessage(node: Node): ErrorMessage = ErrorMessage(
    messageNode = parseMessageNode(node),
    errorsNode = parseErrorsNode(node)
  )

  private def parseSubmitPayment(node: Node): SubmitPayment = SubmitPayment(
    messageNode = parseMessageNode(node),
    transactionNode = parseTransactionNode(node)
  )

  private def parseSubmitPaymentResponse(node: Node): SubmitPaymentResponse = SubmitPaymentResponse(
    messageNode = parseMessageNode(node),
    result = parseMessageResult((node \\ "SUBMIT" \ "RESULT").text)
  )

  private def parseAmountNode(node: Node): AmountNode = {
    def amountFromString(str: String): AmountInPence = str match {
      case s if s.isEmpty => AmountInPence(0)
      case s => AmountInPence(s.toLong)
    }

    AmountNode(
      totalAmount = amountFromString((node \\ "AMOUNT" \ "TOTAL").text),
      currency = CurrencyNum((node \\ "AMOUNT" \ "@currency").text),
      country = Country((node \\ "AMOUNT" \ "@country").text),
      finalAmountO = (node \\ "AMOUNT" \ "FINAL").headOption.map(x => amountFromString(x.text))
    )
  }

  private def parsePtrNode(node: Node): PtrTransactionNode = PtrTransactionNode(
    amountNode = parseAmountNode(node),
    transactionActionO = (node \\ "TRANSACTION" \ "@action").headOption.map(x => parseTransactionAction(x.text)),
    transactionTypeO = (node \\ "TRANSACTION" \ "@type").headOption.map(x => parseTransactionType(x.text)),
    transactionSourceO = (node \\ "TRANSACTION" \ "@source").headOption.map(x => parseTransactionSource(x.text)),
    transactionCustomerO = (node \\ "TRANSACTION" \ "@customer").headOption.map(x => parseCustomerPresence(x.text)),
    transactionReferenceO = (node \\ "TRANSACTION" \ "@reference").headOption.map(x => TransactionReference(x.text)),
    cardVerificationMethod = parseCardVerificationMethod((node \\ "TRANSACTION" \ "CARDHOLDER_RESULT" \ "@verification").text),
    transactionDate = (node \\ "TRANSACTION" \ "@date").headOption.map(_.text),
    transactionTime = (node \\ "TRANSACTION" \ "@time").headOption.map(_.text)
  )

  private def parseTransactionType(value: String): TransactionType = value match {
    case "purchase" => TransactionTypes.Purchase
    case x => throw new RuntimeException(s"Unknown TransactionType: $x")
  }

  private def parseTransactionSource(category: String): TransactionSource = category match {
    case "icc" => TransactionSources.Icc
    case "keyed" => TransactionSources.Keyed
    case x => throw new RuntimeException(s"Unknown TransactionSource: $x")
  }

  private def parseCustomerPresence(value: String): CustomerPresence = value match {
    case "present" => CustomerPresence.Present
    case x => throw new RuntimeException(s"Could not parse CustomerPresence: [$x]")
  }

  //this has extra applicationId and seqNum fields compared to parseCardNode which are used on the receipt
  private def parsePtrCardNode(node: Node): PtrCardNode = PtrCardNode(
    currency = CurrencyNum((node \\ "CARD" \ "@currency").text),
    country = Country((node \\ "CARD" \ "@country").text),
    endDate = (node \\ "CARD" \ "PAN" \ "@end").text,
    startDate = (node \\ "CARD" \ "PAN" \ "@start").headOption.map(_.text),
    pan = parseCardPan(node),
    cardType = CardType((node \\ "CARD" \ "APPLICATION").text),
    applicationId = (node \\ "CARD" \ "APPLICATION" \ "@id").text,
    seqNum = (node \\ "CARD" \ "PAN" \ "@seqNum").headOption.map(_.text)
  )

  private def parseCardPan(node: Node) = CardPan((node \\ "CARD" \ "PAN").text)

  private def parseReceiptNode(node: Node, receiptType: ReceiptType): Option[ReceiptNode] = {
    val receipts: NodeSeq = (node \\ "RECEIPT")
    val maybeReceipt: Option[Node] = receipts.find(node => (node \\ "RECEIPT" \ "@type").text === receiptType.getPrintedName)
    maybeReceipt.map(receipt => parseReceiptNode(receipt))
  }

  def parseReceiptNode(node: Node): ReceiptNode = {
    val xmlReceiptNode: Elem = scala.xml.XML.loadString((node \\ "RECEIPT").text.trim)
    ReceiptNode(
      receiptType = parseReceiptType((node \\ "RECEIPT" \ "@type").text),
      applicationId = (xmlReceiptNode \\ "APPLICATION_ID").headOption.map(_.text),
      authCode = (xmlReceiptNode \\ "AUTH_CODE").headOption.map(_.text),
      cardSchema = (xmlReceiptNode \\ "CARD_SCHEME").headOption.map(x => CardType(x.text)),
      currencyCode = (xmlReceiptNode \\ "CURRENCY_CODE").headOption.map(x => CurrencyCode(x.text)),
      customerPresence = (xmlReceiptNode \\ "CUSTOMER_PRESENCE").headOption.map(x => parseCustomerPresence(x.text)),
      finalAmount = (xmlReceiptNode \\ "FINAL_AMOUNT").headOption.map(x => AmountInPence(x.text)),
      merchantNumber = (xmlReceiptNode \\ "MERCHANT_NUMBER").headOption.map(x => MerchantNumber(x.text)),
      cardPan = (xmlReceiptNode \\ "PAN_NUMBER").headOption.map(x => CardPan(x.text)),
      panSequence = (xmlReceiptNode \\ "PAN_SEQUENCE").headOption.map(_.text),
      terminalId = (xmlReceiptNode \\ "TERMINAL_ID").headOption.map(x => TerminalId(x.text)),
      transactionSource = (xmlReceiptNode \\ "TRANSACTION_DATA_SOURCE").headOption.map(x => parseTransactionSource(x.text)),
      totalAmount = (xmlReceiptNode \\ "TOTAL_AMOUNT").headOption.map(x => AmountInPence(x.text)),
      transactionDate = (xmlReceiptNode \\ "TRANSACTION_DATE").headOption.map(_.text),
      transactionTime = (xmlReceiptNode \\ "TRANSACTION_TIME").headOption.map(_.text),
      transactionType = (xmlReceiptNode \\ "TRANSACTION_TYPE").headOption.map(x => parseTransactionType(x.text)),
      cardVerificationMethod = (xmlReceiptNode \\ "VERIFICATION_METHOD").headOption.map(x => parseCardVerificationMethod(x.text)),
      transactionResponse = (xmlReceiptNode \\ "TRANSACTION_RESPONSE").headOption.map(_.text)
    )
  }

  private def parseReceiptType(value: String): ReceiptType = value match {
    case "merchant" => ReceiptType.Merchant
    case "merchant_signature" => ReceiptType.MerchantSignature
    case "customer" => ReceiptType.Customer
    case "pos" => ReceiptType.Pos
    case x => throw new RuntimeException(s"Unknown ReceiptType:[$x]")
  }

  private def parseCardVerificationMethod(category: String): CardVerificationMethod = category match {
    case "pin" => CardVerificationMethod.Pin
    case "signature" => CardVerificationMethod.Signature
    case "pin_and_signature" => CardVerificationMethod.PinAndSignature
    case "on_device" => CardVerificationMethod.OnDevice
    case "not_performed" => CardVerificationMethod.NotPerformed
    case "failed" => CardVerificationMethod.Failed
    case "unknown" => CardVerificationMethod.Unknown
    case x => throw new RuntimeException(s"Could not parse CardVerificationMethod:[$x]")
  }

  private def parsePt(node: Node): ProcessTransaction = ProcessTransaction(
    messageNode = parseMessageNode(node)
  )

  private def parsePtr(node: Node): ProcessTransactionResponse = ProcessTransactionResponse(
    messageNode = parseMessageNode(node),
    ptrTransactionNode = parsePtrNode(node),
    ptrCardNode = parsePtrCardNode(node),
    result = parseMessageResult((node \\ "TRANS" \ "RESULT").text),
    paymentResult = parsePaymentResult(node),
    merchantNumber = MerchantNumber((node \\ "PAYMENT" \ "MERCHANT" \ "@number").text),
    receiptNodeCustomer = parseReceiptNode(node, ReceiptType.Customer),
    errorsNode = parseErrorsNode(node)
  )

  private def parseGtd(node: Node): GetTransactionDetails = GetTransactionDetails(
    messageNode = parseMessageNode(node)
  )

  private def parseGtdr(node: Node): GetTransactionDetailsResponse = GetTransactionDetailsResponse(
    messageNode = parseMessageNode(node),
    ptrTransactionNode = parsePtrNode(node),
    ptrCardNode = parsePtrCardNode(node),
    result = parseMessageResult((node \\ "TRANS" \ "RESULT").text),
    paymentResult = parsePaymentResult(node),
    merchantNumber = MerchantNumber((node \\ "PAYMENT" \ "MERCHANT" \ "@number").text),
    receiptNodeCustomer = parseReceiptNode(node, ReceiptType.Customer),
    errorsNode = parseErrorsNode(node)
  )

  private def parsePaymentResult(node: Node): PaymentResult = (node \\ "PAYMENT" \ "PAYMENT_RESULT").text match {
    case "on-line" => PaymentResult.OnLine
    case "declined" => PaymentResult.Declined
    case "cancelled" => PaymentResult.Cancelled
    case "not_authorised" => PaymentResult.NotAuthorised
    case x => throw new RuntimeException(s"Unknown PaymentResult: $x")
  }

  private def parseCardNode(node: Node): CardNode = {
    CardNode(
      currency = CurrencyNum((node \\ "CARD" \ "@currency").text),
      country = Country((node \\ "CARD" \ "@country").text),
      endDate = (node \\ "CARD" \ "PAN" \ "@end").text,
      startDate = (node \\ "CARD" \ "PAN" \ "@start").text,
      pan = parseCardPan(node),
      cardType = CardType((node \\ "CARD" \ "APPLICATION").text)
    )
  }

  private def parseTransactionNode(node: Node): TransactionNode = TransactionNode(
    amountNode = parseAmountNode(node),
    purchaseDescription = PurchaseDescription((node \\ "DESCRIPTION").text),
    transactionActionO = (node \\ "TRANSACTION" \ "@action").headOption.map(x => parseTransactionAction(x.text)),
    transactionTypeO = (node \\ "TRANSACTION" \ "@type").headOption.map(x => parseTransactionType(x.text)),
    transactionSourceO = (node \\ "TRANSACTION" \ "@source").headOption.map(x => parseTransactionSource(x.text)),
    transactionCustomerO = (node \\ "TRANSACTION" \ "@customer").headOption.map(x => parseCustomerPresence(x.text))
  )

  private def parseTransactionAction(value: String): TransactionAction = value match {
    case "auth_n_settle" => TransactionAction.AuthorizeAndSettle
    case x => throw new RuntimeException(s"Unknown TransactionAction: $x")
  }

  private def parseUpdatePaymentEnhanced(node: Node): UpdatePaymentEnhanced = UpdatePaymentEnhanced(
    messageNode = parseMessageNode(node),
    transactionNode = parseTransactionNode(node),
    cardNode = parseCardNode(node),
    result = MessageResult.SuccessResult, //TODO parse that
    errors = ErrorsNode(Seq.empty[ErrorNode]) //TODO parse that
  )

  private def parseUpdatePaymentEnhancedResponse(node: Node): UpdatePaymentEnhancedResponse = UpdatePaymentEnhancedResponse(
    messageNode = parseMessageNode(node),
    amountNode = parseAmountNode(node)
  )

  private def parsePosDecisionMessage(node: Node): PosDecisionMessage = PosDecisionMessage(
    messageNode = parseMessageNode(node)
  )

  private def parseFinalise(node: Node): Finalise = Finalise(
    messageNode = parseMessageNode(node)
  )

  private def parseFinaliseResponse(node: Node): FinaliseResponse = FinaliseResponse(
    messageNode = parseMessageNode(node),
    result = parseMessageResult((node \\ "TRANS" \ "RESULT").text)
  )
}
