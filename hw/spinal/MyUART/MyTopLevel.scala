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

  val push_counter = RegInit(U(0, 8 bits))

  val pushStream = Stream(Bits(8 bits))

  pushStream.valid := True
  pushStream.payload := (message  |>> (message.getWidth - 8 - push_counter))( 7 downto 0)
  when(pushStream.ready) {
   when(push_counter < message.getWidth - 8 ) {
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
