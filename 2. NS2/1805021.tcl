# simulator
set ns [new Simulator]

# assign arguments]"

if {[lindex $argv 0] == "-x"} {
    set val(x) [lindex $argv 1]
    set val(y) [lindex $argv 1]
} else {
    set val(x) 500
    set val(y) 500
}
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


# set val(x) [lindex $argv 1]
# set val(y) [lindex $argv 1]
# set val(nn) [lindex $argv 3]
# set val(nf) [lindex $argv 5]

# ======================================================================
# Define options

set val(chan)         Channel/WirelessChannel  ;# channel type
set val(prop)         Propagation/TwoRayGround ;# radio-propagation model
set val(ant)          Antenna/OmniAntenna      ;# Antenna type
set val(ll)           LL                       ;# Link layer type
set val(ifq)          Queue/DropTail/PriQueue  ;# Interface queue type
set val(ifqlen)       50                       ;# max packet in ifq
set val(netif)        Phy/WirelessPhy/802_15_4          ;# network interface type; Phy/WirelessPhy/802_15_4
set val(mac)          Mac/802_15_4               ;# MAC type; Mac/802_15_4
set val(rp)           AODV                     ;# ad-hoc routing protocol
# set val(x)            250                      ;# x dimension of the area
# set val(y)            250                      ;# y dimension of the area
# set val(nn)           20                       ;# number of nodes
# set val(nf)           10                       ;# number of flows

# =======================================================================

# print the above args
puts "X: $val(x)"
puts "Y: $val(y)"
puts "Nodes: $val(nn)"
puts "Flows: $val(nf)"

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

#tracks number of nodes created
set k 0
set x_limit [expr ($val(x)-10)/($val(col)+1)]
set y_limit [expr ($val(y)-10)/($val(row))]
set half [expr int($val(x) / 2)]
set range [expr int(rand() * $half)]

for {set i 0} {$i < $val(row)} {incr i} {
    for {set j 0} {$j < $val(col)} {incr j} {
        set node($k) [$ns node]
        $node($k) random-motion 0       ;# disable random motion

        set cur_x [expr (10 + $x_limit * ($j + 1))]
        set cur_y [expr (10 + $y_limit * $i)]
        $node($k) set X_ $cur_x
        $node($k) set Y_ $cur_y
        $node($k) set Z_ 0

        if {$cur_x <= $half} {
            set destX [expr $cur_x + $range]
        } else {
            set destX [expr $cur_x - $range]
        }

        if {$cur_y <= $half} {
            set destY [expr $cur_y + $range]
        } else {
            set destY [expr $cur_y - $range]
        }

        set velocity [expr int(rand() * 4) + 1]

        $ns at 1.0 "$node($k) setdest $destX $destY $velocity"

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

    # create udp agents
    set udp [new Agent/UDP]
    set null [new Agent/Null]
    
    # connect agents
    $ns attach-agent $node($src) $udp
    $ns attach-agent $node($dest) $null
    $ns connect $udp $null

    # Traffic generator -> Exponential traffic
    set exp [new Application/Traffic/Exponential]
    # define packet size
    $exp set packetSize_ 40
    # attach to agent
    $exp attach-agent $udp
    
    # start traffic generation
    $ns at 1.0 "$exp start"
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
