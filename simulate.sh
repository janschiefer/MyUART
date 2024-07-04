#!/bin/bash
source ~/.sdkman/bin/sdkman-init.sh
sdk use java 17.0.11-tem
source ~/oss-cad-suite/environment
unset VERILATOR_ROOT
sbt "runMain MyUART.MyTopLevelSim"
gtkwave simulation.gtkw&
