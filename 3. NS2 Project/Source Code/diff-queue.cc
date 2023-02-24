#include <bits/stdc++.h>

#include "diff-queue.h"
#include "ip.h"
#include "limits.h"
#include "packet.h"
#include "config.h"

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
    nsaddr_t dest = iph->daddr();

    // if the destination is not in the map, create a new queue and set the size to 1
    if (diffSizes[dest] == 0) {
        //cout << "Count 0 found...\n";
        q_->enque(p);
        diffSizes[dest] = 1;
        // vector<Packet*> newQueue;
        // newQueue.push_back(p);
        // qmap_[dest] = newQueue;
        // diffSizes[dest] = 1;
        //cout << "Added to map for new dest...\n";
    }
    // if the destination is in the map, push the packet into the queue and increase the size
    else {
        //cout << "Count is not 0...\n";
        q_->enque(p);
        diffSizes[dest]++;

        if(diffSizes[dest] > qlim_){
            q_->deque();
            drop(p);
            diffSizes[dest]--;
            //cout << "Packet dropped in enqueue...\n";
        }
        // qmap_[dest].push_back(p);
        // diffSizes[dest]++;

        // if(diffSizes[dest] > qlim_){
        //     qmap_[dest].pop_back();
        //     drop(p);
        //     diffSizes[dest]--;
        //     //cout << "Packet dropped in enqueue...\n";
        // }
    }
}


Packet* DiffQueue::deque()
{
    Packet *p = 0;
  
    int diff_priority = -21;
    //cout << "qmap_ traversal started... ...\n";

    Packet* packetHead = q_->head();
    for (Packet* pkt = packetHead; pkt != 0; pkt = pkt->next_) {
        hdr_ip* pkt_iph = hdr_ip::access(pkt);
        nsaddr_t dst = pkt_iph->daddr();
        int diff = calcDiff(dst, pkt);

        if(diff > diff_priority){
            p = pkt;
            diff_priority = diff;
            //cout << "Diff priority changed...\n";
        }
	}

    if(p != 0){
        //cout << "Packet is not null...\n";
        q_->remove(p);
        diffSizes[hdr_ip::access(p)->daddr()]--;
    }

    // for (auto& kv : qmap_) {
    //     nsaddr_t dest = kv.first;
    //     Packet* temp = kv.second[0];
    //     int diff = calcDiff(dest, temp);
    //     //temp = NULL;

    //     if (diff > diff_priority) {
    //         p = kv.second[0];
    //         diff_priority = diff;
    //         //cout << "Diff priority changed...\n";
    //     }
    // }

    //cout << "qmap_ traversal ended... ...\n";

    // if (p != NULL) {
    //     //cout << "Packet is not null...\n";
    //     nsaddr_t dest = hdr_ip::access(p)->daddr(); //get packet destination
    //     qmap_[dest].erase(qmap_[dest].begin()); //remove packet from queue
    //     diffSizes[dest]--;  //decrease the size of the queue
        
    //     if (qmap_[dest].empty()) {
    //         //cout << "Dest queue is empty & henced erased...\n";
    //         qmap_.erase(dest);
    //     }
    // }
  
    return (p);
}

//calculates QD_i(d)
nsaddr_t DiffQueue::calcDiff(nsaddr_t dest, Packet* p)
{
    int diff = node_queue_length(dest) - next_hop_queue_length(p);
    if(diff < 0) diff = -diff;

    //cout << "Diff calculated..." << diff << "\n";

    return diff;
}

//calculates Q_i(d)
nsaddr_t DiffQueue::node_queue_length(nsaddr_t dest)
{
    //cout << "Node queue length calculated...\n";

    // return qmap_[dest].size();
    return diffSizes[dest];
}

//calculates Q_j(d)
nsaddr_t DiffQueue::next_hop_queue_length(Packet* p)
{
    nsaddr_t next_hop = hdr_cmn::access(p)->next_hop();

    //cout << "Next hop: " << next_hop << "\n";

    if(next_hop < 0) return 0;

    return diffSizes[next_hop];
    // return qmap_[next_hop].size();
}
