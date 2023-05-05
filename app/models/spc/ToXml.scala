/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package models.spc

import scala.xml.Node

/**
 * Provides routines to render SpcRequestMessage as Xml.
 */
object ToXml {

  def toXml(n: PedLogOn): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }<POI_MSG type="interaction">
                               <INTERACTION name="pedLogOn"/>
                             </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: SubmitPayment): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }<POI_MSG type="submittal">
                               <SUBMIT name="submitPayment">
                                 { n.transactionNode.toXml }
                               </SUBMIT>
                             </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: ProcessTransaction): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }<POI_MSG type="transactional">
                               <TRANS name="processTransaction"/>
                             </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: UpdatePaymentEnhancedResponse): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }<POI_MSG type="transactional">
                               <TRANS name="updatePaymentEnhancedResponse">
                                 <PAYMENT>
                                   <TRANSACTION>
                                     { n.amountNode.toXml }
                                   </TRANSACTION>
                                 </PAYMENT>
                               </TRANS>
                             </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: PosPrintReceiptResponse): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }<POI_MSG type="interaction">
                               <INTERACTION name="posPrintReceiptResponse">
                                 <RESPONSE>{ n.result.toString }</RESPONSE>
                               </INTERACTION>
                             </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: Finalise): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }<POI_MSG type="transactional">
                               <TRANS name="finalise"/>
                             </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: CompleteTransaction): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }<POI_MSG type="transactional">
                               <TRANS name="completeTransaction"/>
                             </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: PedLogOff): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }<POI_MSG type="interaction">
                               <INTERACTION name="pedLogOff"/>
                             </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: CancelTransaction): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }
      <POI_MSG type="transactional">
        <TRANS name="cancelTransaction"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }

  def toXml(n: GetTransactionDetails): Node = {
    <RLSOLVE_MSG version="5.0">
      { n.messageNode.toXml }
      <POI_MSG type="transactional">
        <TRANS name="getTransactionDetails"/>
      </POI_MSG>
    </RLSOLVE_MSG>
  }

}
