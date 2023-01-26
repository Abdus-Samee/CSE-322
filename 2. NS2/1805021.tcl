# simulator
set ns [new Simulator]


# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy          ;# network interface type; Phy/WirelessPhy/802_15_4
set val(mac)          Mac/802_11               ;# MAC type; Mac/802_15_4
set val(rp)           AODV                     ;# ad-hoc routing protocol 
set val(nn)           20                       ;# number of mobilenodes
set val(x)            500                      ;# x dimension of area
set val(y)            500                      ;# y dimension of area
# =======================================================================

# trace file
set trace_file [open 1805021.tr w]
$ns trace-all $trace_file

# nam file
set nam_file [open 1805021.nam w]
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
} elseif {$val(rp) == 40} {
    set val(row) 5
    set val(col) 8
} elseif {$val(rp) == 60} {
    set val(row) 6
    set val(col) 10
} elseif {$val(rp) == 80} {
    set val(row) 8
    set val(col) 10
} else {
    set val(row) 10
    set val(col) 10

    # print row and col
    # puts "row: $val(row)"
    # puts "col: $val(col)"
}

#tracks number of nodes created
set k 0

for {set i 0} {$i < $val(row)} {incr i} {
    for {set j 0} {$j < $val(col)} {incr j} {
        set node($k) [$ns node]
        $node($k) random-motion 0       ;# disable random motion

        $node($k) set X_ [expr (10 + 60 * ($j + 1))]
        $node($k) set Y_ [expr (500 - 60 * $i)]
        $node($k) set Z_ 0

        set destX [expr int(rand() * $val(x))]
        set destY [expr int(rand() * $val(y))]
        set velocity [expr int(rand() * 4) + 1]

        $ns at 1.0 "$node($k) setdest $destX $destY $velocity"

        $ns initial_node_pos $node($k) 20

        incr k
    }
}




set val(nf)         20               ; # number of flows

for {set i 0} {$i < $val(nf)} {incr i} {
    # select random source and destination who are distinct
    set src [expr int(rand() * $val(nn))]
    set dest [expr int(rand() * $val(nn))]
    while {$src == $dest} {
        set dest [expr int(rand() * $val(nn))]
    }

    # Traffic config
    # create agent
    set tcp [new Agent/TCP]
    set tcp_sink [new Agent/TCPSink]

    # attach to nodes
    $ns attach-agent $node($src) $tcp
    $ns attach-agent $node($dest) $tcp_sink

    # create udp agents
    set udp [new Agent/UDP]
    set null [new Agent/Null]
    
    # connect agents
    $ns attach-agent $node($src) $udp
    $ns attach-agent $node($dest) $null
    $ns connect $udp $null

    # Traffic generator -> Exponential traffic
    set cbr [new Application/Traffic/CBR]
    # attach to agent
    $cbr attach-agent $udp
    
    # start traffic generation
    $ns at 1.0 "$cbr start"
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
