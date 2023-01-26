#!/bin/bash

tcl_file="1805021.tcl"
trace_file="1805021.tr"
awk_file="1805021.awk"

output_area="area.txt"
output_nodes="nodes.txt"
output_flows="flows.txt"

declare -i area=(250 500 750 1000 1250)
declare -i nodes=(20 40 60 80 100)
declare -i flows=(10 20 30 40 50)

echo "Throughput(bits/s),Delay(s),Delivery Ratio, Drop Ratio" > $output_area
# traverse the area array
for i in "${area[@]}";do
    ns $tcl_file -x $i
    awk -f $awk_file $trace_file >> $output_area
done
