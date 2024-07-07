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

  val input_fifo = new StreamFifo(
        dataType = Bits(8 bit),
        depth = 32
      )

  val pushStream, popStream = Stream(Bits(8 bits))

  input_fifo.io.push << pushStream
  input_fifo.io.pop >> popStream

  pushStream.valid := True
  pushStream.payload := (message  |>> (48 - push_counter))( 7 downto 0)

  when(pushStream.ready.fall()) {

   push_counter := push_counter + U(8).resized
  
   }

  val send = MyUARTSend(234, 8, 2, UartParity.ODD) // 234 for 115200 bps

  io.txData <> send.io.txData
  popStream >> send.io.sendDataStream
}

object MyTopLevelVerilog extends App {
  Config.spinal.generateVerilog(MyTopLevel())
}

object MyTopLevelVhdl extends App {
  Config.spinal.generateVhdl(MyTopLevel())
}
