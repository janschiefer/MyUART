package MyUART

import spinal.core._
import spinal.lib._
import spinal.lib.formal._
import spinal.core.formal._

// You need SymbiYosys to be installed.
// See https://spinalhdl.github.io/SpinalDoc-RTD/master/SpinalHDL/Formal%20verification/index.html#installing-requirements
object MyTopLevelFormal extends App {

  val bitsPerTransfer = 8
  val stopBits = 2
  // CHECK 1: Line should be always high when NOT busy aka idle
  FormalConfig
    .withBMC(50)
    .withProve(50)
    .withCover(50)
    .doVerify(new Component {
      val dut = FormalDut(MyUARTSend(1, bitsPerTransfer, stopBits, UartParity.ODD))

      val reset = ClockDomain.current.isResetActive

      assumeInitial(clockDomain.isResetActive)

      when(reset || past(reset)) {
        assume(dut.io.uartDataStream.valid === False)
      }

      anyseq(dut.io.uartDataStream.payload)

      dut.io.uartDataStream.valid := False

      assert(dut.io.txData)

    })

  FormalConfig
    .withBMC(50)
    .withProve(50)
    .withCover(50)
    .doVerify(new Component {
      val dut = FormalDut(MyUARTSend(1, bitsPerTransfer, stopBits, UartParity.ODD))

      val reset = ClockDomain.current.isResetActive

      assumeInitial(clockDomain.isResetActive)

      anyseq(dut.io.uartDataStream.payload)

      dut.io.uartDataStream.valid := True

      when(!dut.io.uartDataStream.ready) {
        assume(stable(dut.io.uartDataStream.payload))
      }

      when(pastValid() && dut.io.uartDataStream.ready.rise) {

        for (i <- 1 to stopBits) {
          assert(past(dut.io.txData, i))
        }

        for (i <- 0 to (bitsPerTransfer - 1)) {
          assert(past(dut.io.txData, 4 + i) === past(dut.io.uartDataStream.payload(bitsPerTransfer - 1 - i), 1))
        }
      }

    })
}
