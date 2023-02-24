#!/bin/bash

tcl_file="1805021_mobile.tcl"
mod_tcl_file="1805021_mobile_modified.tcl"
trace_file="1805021_mobile.tr"
mod_trace_file="1805021_mobile_modified.tr"
awk_file="1805021.awk"

output_nodes="nodes.txt"
output_flows="flows.txt"
output_rate="rate.txt"
output_speed="speed.txt"

mod_output_nodes="mod_nodes.txt"
mod_output_flows="mod_flows.txt"
mod_output_rate="mod_rate.txt"
mod_output_speed="mod_speed.txt"

declare -i nodes=(20 40 60 80 100)
declare -i flows=(10 20 30 40 50)
declare -i rate=(100 200 300 400 500)
declare -i speed=(5 10 15 20 25)

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio, Energy(J/packet), Energy(J/byte)" > $output_nodes
for i in "${nodes[@]}";do
    ns $tcl_file -nodes $i
    awk -f $awk_file $trace_file >> $output_nodes
done

echo "Node file generation for usual mobile config done!"

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio, Energy(J/packet), Energy(J/byte)" > $output_flows
for i in "${flows[@]}";do
    ns $tcl_file -flows $i
    awk -f $awk_file $trace_file >> $output_flows
done

echo "Flow file generation for usual mobile config  done!"

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio, Energy(J/packet), Energy(J/byte)" > $output_rate
for i in "${rate[@]}";do
    ns $tcl_file -packets $i
    awk -f $awk_file $trace_file >> $output_rate
done

echo "Rate file generation for usual mobile config  done!"

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio, Energy(J/packet), Energy(J/byte)" > $output_speed
for i in "${speed[@]}";do
    ns $tcl_file -speed $i
    awk -f $awk_file $trace_file >> $output_speed
done

echo "Spped file generation for usual mobile config  done!"

echo "Calculation for usual mobile config done!"
echo "########################################################"
# ------------------------------------------------------------------------------------------------------------------------------


echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio, Energy(J/packet), Energy(J/byte)" > $mod_output_nodes
for i in "${nodes[@]}";do
    ns $mod_tcl_file -nodes $i
    awk -f $awk_file $mod_trace_file >> $mod_output_nodes
done

echo "Node file generation for modified mobile config done!"

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio, Energy(J/packet), Energy(J/byte)" > $mod_output_flows
for i in "${flows[@]}";do
    ns $mod_tcl_file -flows $i
    awk -f $awk_file $mod_trace_file >> $mod_output_flows
done

echo "Flow file generation for modified mobile config  done!"

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio, Energy(J/packet), Energy(J/byte)" > $mod_output_rate
for i in "${rate[@]}";do
    ns $mod_tcl_file -packets $i
    awk -f $awk_file $mod_trace_file >> $mod_output_rate
done

echo "Rate file generation for modified mobile config  done!"

echo "Throughput(bits/s), Delay(s), Delivery Ratio, Drop Ratio, Energy(J/packet), Energy(J/byte)" > $mod_output_speed
for i in "${speed[@]}";do
    ns $mod_tcl_file -speed $i
    awk -f $awk_file $mod_trace_file >> $mod_output_speed
done

echo "Speed file generation for modified mobile config  done!"

echo "Calculation for modified mobile config done!"
