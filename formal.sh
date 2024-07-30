#!/bin/bash
source ~/.sdkman/bin/sdkman-init.sh
sdk use java 17.0.12-tem
source ~/oss-cad-suite/environment
sbt "runMain MyUART.MyTopLevelFormal"
gtkwave simWorkspace/unamed_1/unamed_prove/engine_0/trace.vcd

