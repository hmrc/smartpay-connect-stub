package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import uk.gov.hmrc.smartpayconnectstub.models.AmountInPence


class SurchargeSpec extends AnyFreeSpec with Matchers {


  "calculate" - {
    "correct surcharge " in {
      val amount1 = AmountInPence(1000)
      val amount2 = AmountInPence(1000)

      amount1 shouldBe amount2


    }
  }

}

