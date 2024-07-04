package MyUART

import spinal.core._
import spinal.lib._

// Hardware definition
case class MyTopLevelHW() extends Component {
  val io = new Bundle {
    val txData = out Bool() 
  }

    io.txData.setName("txData")

    val toplevel = new MyTopLevel()

    io <> toplevel.io

}

object MyTopLevelHWVerilog extends App {
  Config.spinal.generateVerilog(MyTopLevelHW())
}

object MyTopLevelHWVhdl extends App {
  Config.spinal.generateVhdl(MyTopLevelHW())
}
