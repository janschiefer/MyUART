package MyUART

import spinal.core._
import spinal.lib._

// Hardware definition
case class MyTopLevel() extends Component {
  val io = new Bundle {
    val txData = out Bool() 
  }

  val writeStream = Stream(Bits(8 bits))

  writeStream.payload := B("10000010")
  writeStream.valid := True

  val send = MyUARTSend(234, 8, 2, UartParity.EVEN ) //234 for 115200 bps



  io.txData <> send.io.txData
  send.io.sendDataStream <> writeStream
}

object MyTopLevelVerilog extends App {
  Config.spinal.generateVerilog(MyTopLevel())
}

object MyTopLevelVhdl extends App {
  Config.spinal.generateVhdl(MyTopLevel())
}
