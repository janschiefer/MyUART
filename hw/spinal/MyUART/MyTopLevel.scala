package MyUART

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

// Hardware definition
case class MyTopLevel() extends Component {
  val io = new Bundle {
    val txData = out Bool ()
  }

  val message = B"h48454C4C4F0D0A" // HELLO\CR\LF
  val messageCurr = Bits((message.getWidth + 8) bits)

  val push_counter = RegInit(U(0, 8 bits))

  val pushStream = Stream(Bits(8 bits))

  val hello_counter = RegInit(U(0, 4 bits))

  messageCurr(15 downto 0) := message(15 downto 0)
  messageCurr(23 downto 16) := (U(48, 8 bits) + hello_counter.resized).asBits
  messageCurr(63 downto 24) := message(55 downto 16)

  val timeout = Timeout(100 ms)
  when(timeout) {
    when(hello_counter < 9) {
      hello_counter := hello_counter + U(1).resized
    }
      .otherwise {
        hello_counter := U(0).resized
      }
    // Check if the timeout has tick
    timeout.clear() // Ask the timeout to clear its flag
  }

  pushStream.valid := True
  pushStream.payload := (messageCurr |>> (messageCurr.getWidth - 8 - push_counter)) (7 downto 0)
  when(pushStream.ready) {
    when(push_counter < messageCurr.getWidth - 8) {
      push_counter := push_counter + U(8).resized
    }
      .otherwise {
        push_counter := U(0).resized
      }

  }

  val send = MyUARTSend(234, 8, 2, UartParity.ODD) // 234 for 115200 bps

  io.txData <> send.io.txData
  pushStream >> send.io.sendDataStream
}

object MyTopLevelVerilog extends App {
  Config.spinal.generateVerilog(MyTopLevel())
}

object MyTopLevelVhdl extends App {
  Config.spinal.generateVhdl(MyTopLevel())
}
