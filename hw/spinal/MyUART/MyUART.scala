package MyUART

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

object UartParity extends SpinalEnum(binarySequential) {
  val NONE, EVEN, ODD = newElement()
}

// Hardware definition
case class MyUART(bitrateClockDivider: Int, bitsPerTransfer: Int, stopBits: Int, parity: UartParity.E)
    extends Component {
  val io = new Bundle {
    val txData = out Bool ()
    val sendDataStream = slave Stream (Bits(bitsPerTransfer bits))
  }

  val uart_send = new MyUARTSend(bitrateClockDivider, bitsPerTransfer, stopBits, parity)

  val input_fifo = StreamFifo(
    dataType = Bits(8 bits),
    depth = 32
  )

  input_fifo.io.push <> io.sendDataStream
  input_fifo.io.pop <> uart_send.io.uartDataStream
  io.txData := uart_send.io.txData

}
