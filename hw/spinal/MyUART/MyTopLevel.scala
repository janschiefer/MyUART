package MyUART

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

// Hardware definition
case class MyTopLevel() extends Component {
  val io = new Bundle {
    val txData = out Bool ()
  }

  val writeStream = Stream(Bits(8 bits))

  writeStream.payload := B("10000101")

  writeStream.valid := False

  val fsm = new StateMachine {

    val WAIT: State = new StateDelay(1000) with EntryPoint {
      whenCompleted {
        goto(SEND)
      }
    }

    val SEND: State = new State {
      onEntry { writeStream.valid := True }
      whenIsActive {
        writeStream.valid := True
        when(!writeStream.ready) { goto(WAIT_FINISH_SEND) }
      }
    }

    val WAIT_FINISH_SEND: State = new State {
      whenIsActive {
        when(writeStream.ready) { goto(WAIT) }
      }
    }

  }

  val send = MyUARTSend(234, 8, 2, UartParity.ODD) // 234 for 115200 bps

  io.txData <> send.io.txData
  send.io.sendDataStream <> writeStream
}

object MyTopLevelVerilog extends App {
  Config.spinal.generateVerilog(MyTopLevel())
}

object MyTopLevelVhdl extends App {
  Config.spinal.generateVhdl(MyTopLevel())
}
