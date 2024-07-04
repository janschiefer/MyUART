package MyUART

import spinal.core._
import spinal.core.sim._

object MyTopLevelSim extends App {
  Config.sim.allOptimisation.compile(MyTopLevel()).doSim { dut =>
    // Fork a process to generate the reset and the clock on the dut
    dut.clockDomain.forkStimulus(period = 10)

    for (idx <- 0 to 9999) {

      dut.clockDomain.waitRisingEdge()

    }
  }
}
