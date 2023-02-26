# Wireless 802.11 static node simulation with modified ns2 mechanism
# ======================================================================

# simulator
set ns [new Simulator]

# assign arguments to variables
if {[lindex $argv 0] == "-nodes"} {
    set val(nn) [lindex $argv 1]
} else {
    set val(nn) 40
}
if {[lindex $argv 0] == "-flows"} {
    set val(nf) [lindex $argv 1]
} else {
    set val(nf) 20
}
if {[lindex $argv 0] == "-packets"} {
    set val(pkt_rate) [lindex $argv 1]
} else {
    set val(pkt_rate) 200
}
# if {[lindex $argv 0] == "-tx"} {
#     set val(x) [lindex $argv 1]
#     set val(y) [lindex $argv 1]
# } else {
#     set val(x) 250
#     set val(y) 250
# }
if {[lindex $argv 0] == "-tx"} {
    set arg_pt [lindex $argv 1]
    
    if {$arg_pt == 2} {
        set val(Pt_) 0.115421
    } elseif {$arg_pt == 3} {
        set val(Pt_) 0.58432
    } elseif {$arg_pt == 4} {
        set val(Pt_) 0.3171
    } elseif {$arg_pt == 5} {
        set val(Pt_) 2.28289e-11
    }
} else {
    set val(Pt_) 7.214e-3
}

#         Pr * d^4 * L
# Pt = ---------------------------
#         Gt * Gr * (ht^2 * hr^2)

# T_x range    Pt_ 
# ------------------------
# 100m         0.00721383
# 150m         0.03652
# 200m         0.115421
# 250m         0.28179
# 300m         0.58432

# ======================================================================
# Define options

Phy/WirelessPhy set Pt_ $val(Pt_)              ;# transmission power

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DiffQueue          ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy          ;# network interface type; Phy/WirelessPhy/802_15_4
set val(mac)          Mac/802_11               ;# MAC type; Mac/802_15_4
set val(rp)           AODV                     ;# ad-hoc routing protocol
set val(x)            500                      ;# x dimension of the area
set val(y)            500                      ;# y dimension of the area
# set val(nn)           20                       ;# number of nodes
# set val(nf)           10                       ;# number of flows

# =======================================================================

# print the above args
puts "Nodes: $val(nn)"
puts "Flows: $val(nf)"
puts "Packet rate: $val(pkt_rate)"
puts "T_x range: $val(Pt_)"

# trace file
set trace_file [open 1805021_static_modified.tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open 1805021_static_modified.nam w]
$ns namtrace-all-wireless $nam_file $val(x) $val(y)

# topology: to keep track of node movements
set topo [new Topography]
$topo load_flatgrid $val(x) $val(y) ;# xm * ym area


# general operation director for mobilenodes
create-god $val(nn)


# node configs
# ======================================================================

# $ns node-config -addressingType flat or hierarchical or expanded
#                  -adhocRouting   DSDV or DSR or TORA
#                  -llType	   LL
#                  -macType	   Mac/802_11
#                  -propType	   "Propagation/TwoRayGround"
#                  -ifqType	   "Queue/DropTail/PriQueue"
#                  -ifqLen	   50
#                  -phyType	   "Phy/WirelessPhy"
#                  -antType	   "Antenna/OmniAntenna"
#                  -channelType    "Channel/WirelessChannel"
#                  -topoInstance   $topo
#                  -energyModel    "EnergyModel"
#                  -initialEnergy  (in Joules)
#                  -rxPower        (in W)
#                  -txPower        (in W)
#                  -agentTrace     ON or OFF
#                  -routerTrace    ON or OFF
#                  -macTrace       ON or OFF
#                  -movementTrace  ON or OFF

# ======================================================================

$ns node-config -adhocRouting $val(rp) \
                -llType $val(ll) \
                -macType $val(mac) \
                -ifqType $val(ifq) \
                -ifqLen $val(ifqlen) \
                -antType $val(ant) \
                -propType $val(prop) \
                -phyType $val(netif) \
                -energyModel "EnergyModel" \
                -initialEnergy 500 \
                -txPower 2.0 \
                -rxPower 1.0 \
                -idlePower 1.0 \
                -sleepPower 0.001 \
                -topoInstance $topo \
                -channelType $val(chan) \
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF

#if-else if-else block
if {$val(nn) == 20} {
    set val(row) 5
    set val(col) 4
} elseif {$val(nn) == 40} {
    set val(row) 5
    set val(col) 8
} elseif {$val(nn) == 60} {
    set val(row) 6
    set val(col) 10
} elseif {$val(nn) == 80} {
    set val(row) 8
    set val(col) 10
} else {
    set val(row) 10
    set val(col) 10

    # print row and col
    # puts "row: $val(row)"
    # puts "col: $val(col)"
}

# set seed
set seed 1805021

#track number of nodes created
set k 0
set x_limit [expr ($val(x)-10)/($val(col)+1)]
set y_limit [expr ($val(y)-10)/($val(row))]

for {set i 0} {$i < $val(row)} {incr i} {
    for {set j 0} {$j < $val(col)} {incr j} {
        set node($k) [$ns node]
        $node($k) random-motion 0       ;# disable random motion

        set cur_x [expr (10 + $x_limit * ($j + 1))]
        set cur_y [expr (10 + $y_limit * $i)]
        $node($k) set X_ $cur_x
        $node($k) set Y_ $cur_y
        $node($k) set Z_ 0

        $ns initial_node_pos $node($k) 20

        incr k
    }
}

# set val(nf)         20               ; # number of flows

for {set i 0} {$i < $val(nf)} {incr i} {
    # select random source and destination who are distinct
    set src [expr int(rand() * $val(nn))]
    set dest [expr int(rand() * $val(nn))]
    while {$src == $dest} {
        set dest [expr int(rand() * $val(nn))]
    }

    #$ns duplex-link $node($src) $node($dest) 2Mb 10ms DropTail
    #$ns queue-limit $node($src) $node($dest) 50

    # create ftp agents
    set tcp [new Agent/TCP]
    set tcp_sink [new Agent/TCPSink]
    
    # connect agents
    $ns attach-agent $node($src) $tcp
    $ns attach-agent $node($dest) $tcp_sink
    $ns connect $tcp $tcp_sink
    $tcp set fid_ $i

    # Traffic generator -> FTP traffic
    set ftp [new Application/FTP]
    # define packet size
    $ftp set packetSize_ 40
    # attach to agent
    $ftp attach-agent $tcp
    $ftp set rate_ $val(pkt_rate)
    
    # start traffic generation
    $ns at 1.0 "$ftp start"
}



# End Simulation

# Stop nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 50.0 "$node($i) reset"
}

# call final function
proc finish {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "Simulation ending"
    $ns halt
}

$ns at 50.0001 "finish"
$ns at 50.0002 "halt_simulation"




# Run simulation
puts "Simulation starting"
$ns run# Wireless 802.11 static node simulation with modified ns2 mechanism
# ======================================================================

# simulator
set ns [new Simulator]

# assign arguments to variables
if {[lindex $argv 0] == "-nodes"} {
    set val(nn) [lindex $argv 1]
} else {
    set val(nn) 40
}
if {[lindex $argv 0] == "-flows"} {
    set val(nf) [lindex $argv 1]
} else {
    set val(nf) 20
}
if {[lindex $argv 0] == "-packets"} {
    set val(pkt_rate) [lindex $argv 1]
} else {
    set val(pkt_rate) 200
}
# if {[lindex $argv 0] == "-tx"} {
#     set val(x) [lindex $argv 1]
#     set val(y) [lindex $argv 1]
# } else {
#     set val(x) 250
#     set val(y) 250
# }
if {[lindex $argv 0] == "-tx"} {
    set arg_pt [lindex $argv 1]
    
    if {$arg_pt == 2} {
        set val(Pt_) 0.115421
    } elseif {$arg_pt == 3} {
        set val(Pt_) 0.58432
    } elseif {$arg_pt == 4} {
        set val(Pt_) 0.3171
    } elseif {$arg_pt == 5} {
        set val(Pt_) 2.28289e-11
    }
} else {
    set val(Pt_) 7.214e-3
}

#         Pr * d^4 * L
# Pt = ---------------------------
#         Gt * Gr * (ht^2 * hr^2)

# T_x range    Pt_ 
# ------------------------
# 100m         0.00721383
# 150m         0.03652
# 200m         0.115421
# 250m         0.28179
# 300m         0.58432

# ======================================================================
# Define options

Phy/WirelessPhy set Pt_ $val(Pt_)              ;# transmission power

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DiffQueue          ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy          ;# network interface type; Phy/WirelessPhy/802_15_4
set val(mac)          Mac/802_11               ;# MAC type; Mac/802_15_4
set val(rp)           AODV                     ;# ad-hoc routing protocol
set val(x)            500                      ;# x dimension of the area
set val(y)            500                      ;# y dimension of the area
# set val(nn)           20                       ;# number of nodes
# set val(nf)           10                       ;# number of flows

# =======================================================================

# print the above args
puts "Nodes: $val(nn)"
puts "Flows: $val(nf)"
puts "Packet rate: $val(pkt_rate)"
puts "T_x range: $val(Pt_)"

# trace file
set trace_file [open 1805021_static_modified.tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open 1805021_static_modified.nam w]
$ns namtrace-all-wireless $nam_file $val(x) $val(y)

# topology: to keep track of node movements
set topo [new Topography]
$topo load_flatgrid $val(x) $val(y) ;# xm * ym area


# general operation director for mobilenodes
create-god $val(nn)


# node configs
# ======================================================================

# $ns node-config -addressingType flat or hierarchical or expanded
#                  -adhocRouting   DSDV or DSR or TORA
#                  -llType	   LL
#                  -macType	   Mac/802_11
#                  -propType	   "Propagation/TwoRayGround"
#                  -ifqType	   "Queue/DropTail/PriQueue"
#                  -ifqLen	   50
#                  -phyType	   "Phy/WirelessPhy"
#                  -antType	   "Antenna/OmniAntenna"
#                  -channelType    "Channel/WirelessChannel"
#                  -topoInstance   $topo
#                  -energyModel    "EnergyModel"
#                  -initialEnergy  (in Joules)
#                  -rxPower        (in W)
#                  -txPower        (in W)
#                  -agentTrace     ON or OFF
#                  -routerTrace    ON or OFF
#                  -macTrace       ON or OFF
#                  -movementTrace  ON or OFF

# ======================================================================

$ns node-config -adhocRouting $val(rp) \
                -llType $val(ll) \
                -macType $val(mac) \
                -ifqType $val(ifq) \
                -ifqLen $val(ifqlen) \
                -antType $val(ant) \
                -propType $val(prop) \
                -phyType $val(netif) \
                -energyModel "EnergyModel" \
                -initialEnergy 500 \
                -txPower 2.0 \
                -rxPower 1.0 \
                -idlePower 1.0 \
                -sleepPower 0.001 \
                -topoInstance $topo \
                -channelType $val(chan) \
                -agentTrace ON \
                -routerTrace ON \
                -macTrace OFF \
                -movementTrace OFF

#if-else if-else block
if {$val(nn) == 20} {
    set val(row) 5
    set val(col) 4
} elseif {$val(nn) == 40} {
    set val(row) 5
    set val(col) 8
} elseif {$val(nn) == 60} {
    set val(row) 6
    set val(col) 10
} elseif {$val(nn) == 80} {
    set val(row) 8
    set val(col) 10
} else {
    set val(row) 10
    set val(col) 10

    # print row and col
    # puts "row: $val(row)"
    # puts "col: $val(col)"
}

# set seed
set seed 1805021

#track number of nodes created
set k 0
set x_limit [expr ($val(x)-10)/($val(col)+1)]
set y_limit [expr ($val(y)-10)/($val(row))]

for {set i 0} {$i < $val(row)} {incr i} {
    for {set j 0} {$j < $val(col)} {incr j} {
        set node($k) [$ns node]
        $node($k) random-motion 0       ;# disable random motion

        set cur_x [expr (10 + $x_limit * ($j + 1))]
        set cur_y [expr (10 + $y_limit * $i)]
        $node($k) set X_ $cur_x
        $node($k) set Y_ $cur_y
        $node($k) set Z_ 0

        $ns initial_node_pos $node($k) 20

        incr k
    }
}

# set val(nf)         20               ; # number of flows

for {set i 0} {$i < $val(nf)} {incr i} {
    # select random source and destination who are distinct
    set src [expr int(rand() * $val(nn))]
    set dest [expr int(rand() * $val(nn))]
    while {$src == $dest} {
        set dest [expr int(rand() * $val(nn))]
    }

    #$ns duplex-link $node($src) $node($dest) 2Mb 10ms DropTail
    #$ns queue-limit $node($src) $node($dest) 50

    # create ftp agents
    set tcp [new Agent/TCP]
    set tcp_sink [new Agent/TCPSink]
    
    # connect agents
    $ns attach-agent $node($src) $tcp
    $ns attach-agent $node($dest) $tcp_sink
    $ns connect $tcp $tcp_sink
    $tcp set fid_ $i

    # Traffic generator -> FTP traffic
    set ftp [new Application/FTP]
    # define packet size
    $ftp set packetSize_ 40
    # attach to agent
    $ftp attach-agent $tcp
    $ftp set rate_ $val(pkt_rate)
    
    # start traffic generation
    $ns at 1.0 "$ftp start"
}



# End Simulation

# Stop nodes
for {set i 0} {$i < $val(nn)} {incr i} {
    $ns at 50.0 "$node($i) reset"
}

# call final function
proc finish {} {
    global ns trace_file nam_file
    $ns flush-trace
    close $trace_file
    close $nam_file
}

proc halt_simulation {} {
    global ns
    puts "Simulation ending"
    $ns halt
}

$ns at 50.0001 "finish"
$ns at 50.0002 "halt_simulation"




# Run simulation
puts "Simulation starting"
$ns run
