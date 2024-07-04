#!/bin/bash
set -e
source ~/.sdkman/bin/sdkman-init.sh
sdk use java 17.0.11-tem
source ~/oss-cad-suite/environment
rm -f *.json *.fs *.log hw/gen/*.v hw/gen/*.vhd
sbt "runMain MyUART.MyTopLevelHWVerilog"
# -retime fails to work, -noalu sometimes needed
yosys -DSYNTHESIS -l yosys.log -p "read_verilog -sv hw/gen/*.v; synth_gowin -top MyTopLevelHW  -abc9 -json build.yosys.json" 
#nextpnr-gowin --enable-globals --enable-auto-longwires --parallel-refine --placer-heap-cell-placement-timeout 0  --placer-heap-timingweight 30 --json build.yosys.json --write build.nextpnr.json --device GW1NR-LV9QN88PC6/I5 --family GW1N-9C --cst tangnano9k.cst --freq 27 --timing-allow-fail -l nextpnr.log
nextpnr-himbaechel --detailed-timing-report --json build.yosys.json --write build.nextpnr.json --device GW1NR-LV9QN88PC6/I5 --vopt family=GW1N-9C --vopt cst=tangnano9k.cst
gowin_pack --done_as_gpio -d GW1N-9C -o bitstream.fs build.nextpnr.json
openFPGALoader --verbose --board tangnano9k --bitstream bitstream.fs