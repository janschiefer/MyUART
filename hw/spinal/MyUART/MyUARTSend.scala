package MyUART

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

object UartParity extends SpinalEnum(binarySequential) {
  val NONE, EVEN, ODD = newElement()
}

// Hardware definition
case class MyUARTSend( bitrateClockDivider : Int, bitsPerTransfer : Int, stopBits: Int, parity : UartParity.E ) extends Component {
  val io = new Bundle {
        val txData  = out Bool()
        val sendDataStream = slave Stream (Bits(bitsPerTransfer bits))


  }

  val slowUARTClock = new SlowArea(bitrateClockDivider) { 

    val txReg = RegInit(False)
    val parityReg = Reg(Bool())

    val bitCounter = RegInit(U(0, log2Up(bitsPerTransfer) bit))

    io.sendDataStream.ready := False

    parityReg := parityReg ^ io.sendDataStream.payload(bitCounter)

     val fsm = new StateMachine {

        val UART_IDLE : State = new State with EntryPoint {
          whenIsActive {
             txReg := False 
             when(io.sendDataStream.valid) {
                goto(UART_SEND_START_BIT)
             }
          }
        }

        val UART_SEND_START_BIT : State = new State {
            whenIsActive {
              txReg := True 
              bitCounter := U(0).resized

              parityReg := parity === UartParity.ODD

              goto(UART_SEND_DATA_BITS)
            }
          }

          val UART_SEND_DATA_BITS : State = new State {

            whenIsActive {
             txReg := !io.sendDataStream.payload(bitCounter)
              when(bitCounter < U(bitsPerTransfer - 1).resized ) {
                bitCounter := bitCounter + U(1).resized
              }
              .otherwise {
                io.sendDataStream.ready := True
                when(parity === UartParity.NONE) {
                  goto(UART_SEND_STOP_BIT) 
                }
                .otherwise {
                  goto(UART_SEND_PARITY_BIT)
                }
              }
            }
          
          }

          val UART_SEND_PARITY_BIT : State = new State {
            whenIsActive {
              txReg := !parityReg
              goto(UART_SEND_STOP_BIT) 
            }
          }

          val UART_SEND_STOP_BIT : State = new State {
            onEntry {
              bitCounter := U(0).resized
            }
            whenIsActive {

              txReg := False

               when(bitCounter < U(stopBits - 1).resized ) {
                bitCounter := bitCounter + U(1).resized
              }
              .otherwise {
                goto(UART_IDLE) //TODO: Parity
              }

            }
          }
    }

  }

  io.txData := !slowUARTClock.txReg //Invert because we can only init registers to false on FPGA startup

}
