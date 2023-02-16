# Differential Backlog Congestion Control

The aim is to implement a new congestion control mechanism for TCP in NS2. In order to do that, some files were added inside the ns2
source code. The list of the files modified which are also provided in the repository are as follows:
 - *~ns-allinone-2.35/ns-2.35/queue/diff-queue.cc*
 - *~ns-allinone-2.35/ns-2.35/queue/diff-queue.h*
 - *~ns-allinone-2.35/ns-2.35/Makefile.in*
 - *~ns-allinone-2.35/ns-2.35/tcp/diffq.cc*
 - *~ns-allinone-2.35/ns-2.35/tcp/tcp.h*
 
 `NOTE`: All the new *.cc* files added should be included in the *Makefile.in* file inside **OBJ_CC**. The header files need not be added.
 
 ## Compilation of the modified NS2 source code
 
 In order to modify go to the directory *~ns-allinone-2.35/ns-2.35* in the linux/wsl shell. Then execute the commands `/configure` and then `make`.
 One might need to run `sudo make install` after if running a *tcl* script does not work.
