# Basic File Transfer Protocol

We are dealing with Unix sockets. The code must compile on Pluto. 

## **Assignment Objective**

The goal of this assignment is to gain experience with **UDP socket** programming in a client-server environment by implementing a file transfer protocol. You will use C++ or Java (your choice)  to  design  and  implement  a  client  program  (client)  and  server  program  (server)  to communicate between themselves. 

##  **Assignment Specifications** 
### 1. **Summary** 

In this assignment, the client will transfer a file <filename> (specified in the command line) to the server over the network using internet sockets. The **file formatting must be preserved** (line breaks, white spaces, punctuation, etc.). The “network” will actually be non-existent since you be running your code locally on Pluto, but everything you create here is applicable to a real network. 

This assignment uses a two-stage communication process. In the *negotiation stage*, the client and the server agree on a random port <r\_port> for later use through a fixed negotiation port <n\_port> of 

the server. Each port is allowed to be between 1024 and 65535 (inclusive). Later, in the *transaction stage*, the client connects to the server through the selected random port for actual data transfer. 

### 2. **Client-Server Communication**  

Communication between client and server is done in two main stages:

####  Stage  1: Handshake.  
  In  this  stage,  the  client  creates  a  UDP  socket  to  the  server  using <server\_address> as the server address and <n\_port> as the handshake port on the server (where the server is known *a priori* to be listening). The client sends a request to get the random port number on the server where the client will send the actual data. For simplicity, the client will send the characters 1234 (as a single message) to initiate a handshake with the server;** this does not need to be written to the screen. 

Once the server receives this request in the handshake port, the server will reply back with a random port  number  <r\_port>  *between  1024  and  65535*  (inclusive)  where  it  will  be  listening  for  the expected data.  

The server will then write to screen **“The random port chosen is <r\_port>”**. It must **write exactly that**, where <r\_port> is replaced by a port number, no brackets; or you will lose points.  

Both the client and server must close their sockets once the handshake stage has completed.  

####  Stage 2: File Transfer.
  In this stage, the client creates a UDP socket to the server using <r\_port> as the port and sends the data. This data is assumed to be 8-bit ASCII (assuming standard 8-bit encoding for each character, so 1 byte per character) and may be of arbitrary finite length of at least 1 byte. The file is sent over the channel in chunks of 4 characters of the file per UDP packet (a character includes white space, exclamation points, commas, etc.).  An exception to this rule occurs when the end of the file is reached and, in that case, less than 4 characters of the file may be sent. We call each such chunk of the file a *payload*. 

**The packet may contain other information in addition to the payload, if you deem it useful; for example,  to  indicate  the  end  of  the  file.**  After  each  packet  is  sent,  the  client  waits  for  an acknowledgement from the server that comes in the form of the most recent transmitted payload in **capital letters**. These acknowledgements are output to the screen on the client side as one line per packet. The client does not need to write these acknowledgements to any file. 

Once the file has been sent and the last acknowledgement received, the client closes its ports and terminates automatically; that is, **it must determine the end of the file**. 

On the other side, the server receives the data and writes it to file using the filename “blah.txt” and you must use this name or you will lose points.  **By the end of execution, the entire file should be written on the server side. Note that if a file named “**blah.txt**” existed prior to executing your program, this file must be overwritten with these new contents.**  The server does **not** write the received data to screen. 

After each received packet, the server uses the UDP socket to send back an acknowledgement to the client that is the most recent received payload in capital letters. Recall that the client displays this data to the screen. 

Once the last acknowledgement has been sent, the server closes all ports and terminates automatically; that is, it must determine that end of the transmission has occurred from the client.  

###  3. **Client Program (**client**)** 

You should implement a client program, named client. It will take the command line inputs in this order: <server\_address>, <n\_port>, and <filename> 

###  4. **Server Program (server)** 

You must also implement a server program, named server.  It will take the command line input <n\_port>.   

###  5. **Example Execution Commands in Java** Assume that host1 is the server and host2 is the client.** 

On host1: java server <n\_port>

On host2: java client <host1/server address> <n\_port> <filename>

So, you will execute (assuming the use of negotiation port 6003): 

java server 6003

java client localhost 6003 file.txt
