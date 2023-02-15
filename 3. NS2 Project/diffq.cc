#ifndef lint
static const char rcsid[] =
"@(#) $Header: /cvsroot/nsnam/ns-2/tcp/tcp-diffq.cc,v 1.37 2023/02/15 18:58:12 samee Exp $ (NCSU/IBM)";
#endif

#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>

#include "ip.h"
#include "tcp.h"
#include "flags.h"

#define MIN(x, y) ((x)<(y) ? (x) : (y))


static class DiffQTcpClass : public TclClass {
public:
	DiffQTcpClass() : TclClass("Agent/TCP/DiffQ") {}
	TclObject* create(int, const char*const*) {
		return (new DiffQAgent());
	}
} class_diffq;


DiffQAgent::DiffQAgent() : TcpAgent()
{
	v_sendtime_ = NULL;
	v_transmits_ = NULL;
}

DiffQAgent::~DiffQAgent()
{
	if (v_sendtime_)
		delete []v_sendtime_;
	if (v_transmits_)
		delete []v_transmits_;
}

void
DiffQAgent::delay_bind_init_all()
{
	TcpAgent::delay_bind_init_all();
    reset();
}

int
DiffQAgent::delay_bind_dispatch(const char *varName, const char *localName, 
				   TclObject *tracer)
{
	/* init diffq var */
    return TcpAgent::delay_bind_dispatch(varName, localName, tracer);
}

void DiffQAgent::recv(Packet *pkt, Handler*)
{
	hdr_tcp *tcph = hdr_tcp::access(pkt);
	int valid_ack = 0;
	if (qs_approved_ == 1 && tcph->seqno() > last_ack_) 
		endQuickStart();
	if (qs_requested_ == 1)
		processQuickStart(pkt);
#ifdef notdef
	if (pkt->type_ != PT_ACK) {
		Tcl::instance().evalf("%s error \"received non-ack\"",
				      name());
		Packet::free(pkt);
		return;
	}
#endif
	/* W.N.: check if this is from a previous incarnation */
	if (tcph->ts() < lastreset_) {
		// Remove packet and do nothing
		Packet::free(pkt);
		return;
	}
	++nackpack_;
	ts_peer_ = tcph->ts();
	int ecnecho = hdr_flags::access(pkt)->ecnecho();
	if (ecnecho && ecn_)
		ecn(tcph->seqno());
	recv_helper(pkt);
	recv_frto_helper(pkt);
	/* grow cwnd and check if the connection is done */ 
	if (tcph->seqno() > last_ack_) {
		recv_newack_helper(pkt);
		if (last_ack_ == 0 && delay_growth_) { 
			cwnd_ = initial_window();
		}
	} else if (tcph->seqno() == last_ack_) {
                if (hdr_flags::access(pkt)->eln_ && eln_) {
                        tcp_eln(pkt);
                        return;
                }
		if (++dupacks_ == numdupacks_ && !noFastRetrans_) {
			dupack_action();
		} else if (dupacks_ < numdupacks_ && singledup_ ) {
			send_one();
		}
	}

	if (QOption_ && EnblRTTCtr_)
		process_qoption_after_ack (tcph->seqno());

	if (tcph->seqno() >= last_ack_)  
		// Check if ACK is valid.  Suggestion by Mark Allman. 
		valid_ack = 1;
	Packet::free(pkt);
	/*
	 * Try to send more data.
	 */
	if (valid_ack || aggressive_maxburst_)
		send_much(0, 0, maxburst_);
}

	
void DiffQAgent::timeout(int tno)
{
	/* retransmit timer */
	if (tno == TCP_TIMER_RTX) {

		// There has been a timeout - will trace this event
		trace_event("TIMEOUT");

		frto_ = 0;
		// Set pipe_prev as per Eifel Response
		pipe_prev_ = (window() > ssthresh_) ?
			window() : (int)ssthresh_;

	        if (cwnd_ < 1) cwnd_ = 1;
		if (qs_approved_ == 1) qs_approved_ = 0;
		if (highest_ack_ == maxseq_ && !slow_start_restart_) {
			/*
			 * TCP option:
			 * If no outstanding data, then don't do anything.  
			 */
			 // Should this return be here?
			 // What if CWND_ACTION_ECN and cwnd < 1?
			 // return;
		} else {
			recover_ = maxseq_;
			if (highest_ack_ == -1 && wnd_init_option_ == 2)
				/* 
				 * First packet dropped, so don't use larger
				 * initial windows. 
				 */
				wnd_init_option_ = 1;
                        else if ((highest_ack_ == -1) &&
                                (wnd_init_option_ == 1) && (wnd_init_ > 1)
				&& bugfix_ss_)
                                /*
                                 * First packet dropped, so don't use larger
                                 * initial windows.  Bugfix from Mark Allman.
                                 */
                                wnd_init_ = 1;
			if (highest_ack_ == maxseq_ && restart_bugfix_)
			       /* 
				* if there is no outstanding data, don't cut 
				* down ssthresh_.
				*/
				slowdown(CLOSE_CWND_ONE|NO_OUTSTANDING_DATA);
			else if (highest_ack_ < recover_ &&
			  last_cwnd_action_ == CWND_ACTION_ECN) {
			       /*
				* if we are in recovery from a recent ECN,
				* don't cut down ssthresh_.
				*/
				slowdown(CLOSE_CWND_ONE);
				if (frto_enabled_ || sfrto_enabled_) {
					frto_ = 1;
				}
			}
			else {
				++nrexmit_;
				last_cwnd_action_ = CWND_ACTION_TIMEOUT;
				slowdown(CLOSE_SSTHRESH_HALF|CLOSE_CWND_RESTART);
				if (frto_enabled_ || sfrto_enabled_) {
					frto_ = 1;
				}
			}
		}
		/* if there is no outstanding data, don't back off rtx timer */
		if (highest_ack_ == maxseq_ && restart_bugfix_) {
			reset_rtx_timer(0,0);
		}
		else {
			reset_rtx_timer(0,1);
		}
		last_cwnd_action_ = CWND_ACTION_TIMEOUT;
		send_much(0, TCP_REASON_TIMEOUT, maxburst_);
	} 
	else {
		timeout_nonrtx(tno);
	}
}


void DiffQAgent::output(int seqno, int reason)
{
	TcpAgent::output(seqno, reason);
}

void DiffQAgent::slowdown_(int how)
{
	cout << "slowdown_ called ... ...\n";
	TcpAgent::slowdown(how);
}

void DiffQAgent::opencwnd_()
{
	cout << "opencwnd_ called ... ...\n";
	TcpAgent::opencwnd();
}
