package MyUART

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

object UartParity extends SpinalEnum(binarySequential) {
  val NONE, EVEN, ODD = newElement()
}

// Hardware definition
case class MyUARTSend(bitrateClockDivider: Int, bitsPerTransfer: Int, stopBits: Int, parity: UartParity.E)
    extends Component {
  val io = new Bundle {
    val txData = out Bool ()
    val sendDataStream = slave Stream (Bits(bitsPerTransfer bits))

  }

  val popStream = Stream(Bits(8 bits))

  val slowUARTClock = new SlowArea(bitrateClockDivider) {

    val parityReg = Reg(Bool())
    val busyBit = RegInit(False)

    io.txData := True

    val bitCounter = RegInit(U(0, log2Up(bitsPerTransfer) bit))

    val fsm = new StateMachine {

      val UART_IDLE: State = new State with EntryPoint {
        onEntry {
          busyBit := False
        }
        whenIsActive {

          when(popStream.valid) {
            goto(UART_SEND_START_BIT)
          }
        }
      }

      val UART_SEND_START_BIT: State = new State {
        onEntry {
          busyBit := True
        }

        whenIsActive {
          io.txData := False
          goto(UART_SEND_DATA_BITS)
        }
      }

      val UART_SEND_DATA_BITS: State = new State {
        onEntry {
          bitCounter := U(0).resized
          parityReg := parity === UartParity.ODD
        }

        whenIsActive {
          io.txData := popStream.payload(bitCounter)
          parityReg := parityReg ^ io.txData

          when(bitCounter < U(bitsPerTransfer - 1).resized) {
            bitCounter := bitCounter + U(1).resized
          }
            .otherwise {
              when(parity === UartParity.NONE) { goto(UART_SEND_STOP_BIT) }
                .otherwise { goto(UART_SEND_PARITY_BIT) }
            }
        }

      }

      val UART_SEND_PARITY_BIT: State = new State {

        whenIsActive {
          io.txData := parityReg
          goto(UART_SEND_STOP_BIT)
        }
      }

      val UART_SEND_STOP_BIT: State = new State {
        onEntry {
          bitCounter := U(0).resized
        }
        whenIsActive {
          io.txData := True
          when(bitCounter < U(stopBits - 1).resized) {
            bitCounter := bitCounter + U(1).resized
          }
            .otherwise {
              goto(UART_IDLE)
            }

        }
      }
    }

  }

  popStream.ready := slowUARTClock.busyBit.fall()

  val input_fifo = StreamFifo(
    dataType = Bits(8 bits),
    depth = 32
  )

  input_fifo.io.push << io.sendDataStream
  input_fifo.io.pop >> popStream

}
