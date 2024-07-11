package MyUART

import spinal.core._
import spinal.lib._
import spinal.lib.formal._
import spinal.core.formal._

// You need SymbiYosys to be installed.
// See https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Formal%20verification/index.html#installing-requirements
object MyTopLevelFormal extends App {

  // CHECK 1: Line should be always high when NOT busy aka idle
  FormalConfig
    .withBMC(50)
    .withProve(50)
    .withCover(50)
    .doVerify(new Component {
      val dut = FormalDut(MyUARTSend(1, 8, 2, UartParity.ODD))

      val reset = ClockDomain.current.isResetActive

      assumeInitial(clockDomain.isResetActive)

      when(reset || past(reset)) {
        assume(dut.io.uartDataStream.valid === False)
      }

      anyseq(dut.io.uartDataStream.payload)

      dut.io.uartDataStream.valid := False

      assert(dut.io.txData)

    })

  // Test STOP bits
  FormalConfig
    .withProve(50)
    .doVerify(new Component {
      val dut = FormalDut(MyUARTSend(1, 8, 2, UartParity.ODD))

      val reset = ClockDomain.current.isResetActive

      assumeInitial(clockDomain.isResetActive)

      anyseq(dut.io.uartDataStream.payload)

      dut.io.uartDataStream.valid := True

      when(pastValid() && dut.io.uartDataStream.ready.rise) {
        assert(past(dut.io.txData, 1)) // STOP bit 2
        assert(past(dut.io.txData, 2)) // STOP bit 1
      }

    })
}
