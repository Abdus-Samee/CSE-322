#!/bin/bash

tcl_file="1805021.tcl"
trace_file="1805021.tr"
awk_file="1805021.awk"

output_area="areas.txt"
output_nodes="nodes.txt"
output_flows="flows.txt"

declare -i area=(250 500 750 1000 1250)
declare -i nodes=(20 40 60 80 100)
declare -i flows=(10 20 30 40 50)

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio" > $output_area
for i in "${area[@]}";do
    ns $tcl_file -x $i
    awk -f $awk_file $trace_file >> $output_area
done

echo "Area file generation done!"

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio" > $output_nodes
for i in "${nodes[@]}";do
    ns $tcl_file -nodes $i
    awk -f $awk_file $trace_file >> $output_nodes
done

echo "Node file generation done!"

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio" > $output_flows
for i in "${flows[@]}";do
    ns $tcl_file -flows $i
    awk -f $awk_file $trace_file >> $output_flows
done

echo "Flow file generation done!"
