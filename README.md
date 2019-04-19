

#Team Network application for CPS706

This applications acts as a file sharing network. 
Composed of 2 programs: 
1. directoryServ to act as a directory server for peers.
2. client to act as a client in the network as well as a peer server to host image files.

User can run multiple directory server programs (from one device or multiple) and create a cycle between them with the given sockets and IPs.
Once directory servers are set up, client programs can be run.

Clients can inform directory servers of file in which peer will be hosting, updating the directory servers content/address records.
Clients can also query directory servers for known files to download from other peers.

File transfer occurs on a threaded function, allowing clients to be undisturbed if queried.
Currently transfer of .png files will work without issue, making small changes to include .jpeg however currently only transfers part of the buffered image.

All code is authored by Ryan Woodworth (Me).
