package MyUART

import spinal.core._
import spinal.core.formal._

// You need SymbiYosys to be installed.
// See https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Formal%20verification/index.html#installing-requirements
object MyTopLevelFormal extends App {

  // CHECK 1: Line should be always high when NOT busy aka idle
  FormalConfig
    .withProve(50)
    .doVerify(new Component {
      val dut = FormalDut(MyUARTSend(2, 8, 2, UartParity.ODD))

      assumeInitial(clockDomain.isResetActive)

      anyseq(dut.io.sendDataStream.payload)

      dut.io.sendDataStream.valid := False

      assert(dut.io.txData)

    })

  /*

  FormalConfig
    .withBMC(10)
    .doVerify(new Component {
      val dut = FormalDut(MyTopLevel())

      // Ensure the formal test start with a reset
      assumeInitial(clockDomain.isResetActive)

      // Provide some stimulus
      anyseq(dut.io.cond0)
      anyseq(dut.io.cond1)

      // Check the state initial value and increment
      assert(dut.io.state === past(dut.io.state + U(dut.io.cond0)).init(0))
    })

   */

}
