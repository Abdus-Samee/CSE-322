#include <bits/stdc++.h>

#include "queue.h"
#include "address.h"
#include "config.h"
#include "tcp.h"

#define QUEUE_THRESH 25

class DiffQueue : public Queue {
 public:
  DiffQueue() { 
    q_ = new PacketQueue;
  }

 protected:
   void enque(Packet*);
   Packet* deque();
   int calcDiff(nsaddr_t dest, Packet* p);
   int node_queue_length(nsaddr_t dest);
   int next_hop_queue_length(Packet* p);

   PacketQueue *q_;
   map<nsaddr_t, vector<Packet*>> qmap_; // map of priority queues
   map<nsaddr_t, int> diffSizes; // map of priority queue sizes
};
