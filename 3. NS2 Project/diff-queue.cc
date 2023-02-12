#include <bits/stdc++.h>

#include "diff-queue.h"
#include "ip.h"
#include "limits.h"

static class DiffQueueClass : public TclClass {
public:
        DiffQueueClass() : TclClass("Queue/DiffQueue") {}
        TclObject* create(int, const char*const*) {
	         return (new DiffQueue);
	}
} class_differential_queue;


void DiffQueue::enque(Packet* p)
{
    hdr_ip* iph = hdr_ip::access(p);
    //get packet destination
    nsaddr_t dest = iph->daddr();

    // if the destination is not in the map, create a new queue and set the size to 1
    if (qmap_.count(dest) == 0) {
        cout << "Count 0 found...\n";
        vector<Packet*> newQueue;
        newQueue.push_back(p);
        qmap_[dest] = newQueue;
        diffSizes[dest] = 1;
        cout << "Added to map for new dest...\n";
    }
    // if the destination is in the map, push the packet into the queue and increase the size
    else {
        cout << "Count is not 0...\n";
        qmap_[dest].push_back(p);
        diffSizes[dest]++;

        if(diffSizes[dest] > qlim_){
            qmap_[dest].pop_back();
            drop(p);
            diffSizes[dest]--;
            cout << "Packet dropped in enqueue...\n";
        }
    }
    
    cout << "Enqued ... ... ...\n";
    //q_->enque(p);
}


Packet* DiffQueue::deque()
{
    Packet *p = NULL;
  
    int diff_priority = -21;
    cout << "qmap_ traversal started... ...\n";

    for (auto& kv : qmap_) {
        nsaddr_t dest = kv.first;
        Packet* temp = kv.second[0];
        int diff = calcDiff(dest, temp);
        //temp = NULL;

        if (diff > diff_priority) {
            p = kv.second[0];
            diff_priority = diff;
            cout << "Diff priority changed...\n";
        }
    }

    cout << "qmap_ traversal ended... ...\n";

    if (p != NULL) {
        cout << "Packet is not null...\n";
        nsaddr_t dest = hdr_ip::access(p)->daddr(); //get packet destination
        qmap_[dest].erase(qmap_[dest].begin()); //remove packet from queue
        diffSizes[dest]--;  //decrease the size of the queue
        
        if (qmap_[dest].empty()) {
            cout << "Dest queue is empty & henced erased...\n";
            qmap_.erase(dest);
        }
    }

    cout << "Dequed ... ... ...\n";
  
    return (p);
}

//calculates QD_i(d)
nsaddr_t DiffQueue::calcDiff(nsaddr_t dest, Packet* p)
{
    int diff = node_queue_length(dest) - next_hop_queue_length(p);
    if(diff < 0) diff = -diff;

    cout << "Diff calculated...\n";

    return diff;
}

//calculates Q_i(d)
nsaddr_t DiffQueue::node_queue_length(nsaddr_t dest)
{
    cout << "Node queue length calculated...\n";

    return qmap_[dest].size();
}

//calculates Q_j(d)
nsaddr_t DiffQueue::next_hop_queue_length(Packet* p)
{
    // nsaddr_t next_hop = hdr_cmn::access(p)->next_hop();

    // return qmap_[next_hop].size();
    cout << "Next hop queue length calculated...\n";

    return 21;
}
