#!/bin/bash
source ~/.sdkman/bin/sdkman-init.sh
sdk use java 17.0.11-tem
source ~/oss-cad-suite/environment
sbt "runMain MyUART.MyTopLevelFormal"
gtkwave simWorkspace/unamed/unamed_prove/engine_0/trace_induct.vcd
gtkwave gtkwave simWorkspace/unamed/unamed_bmc/engine_0/trace.vcd
