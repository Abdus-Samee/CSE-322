# Installation of ns2 in Ubuntu 20.04/Ubuntu 18.04 for WSL

 - Run the command: `sudo apt install build-essential autoconf automake libxmu-dev`
 - Open the following file: `sudo nano /etc/apt/sources.list`
 - Include the following line: `deb http://in.archive.ubuntu.com/ubuntu bionic main universe` in the above file
 - Now do the following:
   ```
    $sudo apt update
    $sudo apt install gcc-4.8 g++-4.8
    $sudo apt install gedit(optional)
   ```
 - Download the latest allinone ns2 package from [here](https://sourceforge.net/projects/nsnam/files/allinone/ns-allinone-2.35/) and run the following:
   ```
   $tar zxvf ns-allinone-2.35.tar.gz
   $cd ns-allinone-2.35/ns-2.35
   ```
   If the first command gives permission error, then add `sudo` in front of the first command
 - Change every occurence of `@CC` into `gcc-4.8` and `@CPP@/@CXX@` into `g++-4.8` in the following files:
   ```
   $./ns-2.35/Makefile.in
   $./nam-1.15/Makefile.in
   $./xgraph-12.2/Makefile.in
   $./otcl-1.14/Makefile.in
   ```
 - In the file `$./ns-2.35/linkstate/ls.h`, change the line no 137 from this
   ```
   void eraseAll() { erase(baseMap::begin(), baseMap::end()); }
   ```
   to this
   ```
   void eraseAll() { this->erase(baseMap::begin(), baseMap::end()); }
   ```
 - Now open a new terminal and `cd` into *ns-allinone-2.35* and run: `$./install` or `$sudo ./install`
 - In order to set the path, open the `.bashrc` file at the location */home/<username>* using either **nano** or **gedit** or anything else
   ```
   $gedit /home/<username>/.bashrc
   ```
 - Now we can paste the *PATH* and *LD_LIBRARY_PATH* variables, which if we carefully observe, can see them posted at the end of the installation in the terminal, in the 
   form of
   ```
   export PATH=$PATH:<location>/ns-allinone-2.35/bin:<location>/ns-allinone-2.35/tcl8.5.10/unix:<location>/ns-allinone-2.35/tk8.5.10/unix
   export LD_LIBRARY_PATH=<location>/ns-allinone-2.35/otcl-1.14:<location>/ns-allinone-2.35/lib
   ```
   As I installed in the **D** drive, my *folder location* was: `/mnt/d`
 - Now execute the following: `$source .bashrc` or `$source /home/<username>/.bashrc` depending on the current working directory
  
We can see that our ns2 is properly installed by running `$ns` in the cmd and seeing a **%** prompt pop off out of nowhere. We can get out of it using `Ctrl+C`. But
running the `$nam` command might not work if **XLaunch** is not installed. In simple words, this app serves as the GUI for our WSL.
