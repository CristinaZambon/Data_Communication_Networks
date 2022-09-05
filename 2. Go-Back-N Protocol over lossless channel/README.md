# Go-Back-N Protocol over lossless channel
  This project implements a version of the Go-Back-N (GBN) protocol which facilitates the transfer of a text file from one host to another across an unreliable network. 
It takes into consideration a lossless channel. The protocol is unidirectional, i.e., data will flow in one direction (from the client to the server) and the acknowledgements (ACKs) in the opposite direction.
To implement this protocol, you will write two programs: a client and a server, with the specifications given below. 
All communication will be done over datagram (UDP) UNIX sockets. 


## 1. Client Program (client)
You should implement a client program, named client. Its command line input includes the
following:

<serverName: host name of the server>, 

<serverPort: UDP port number used by the server to receive data from the client>,

<fileName: name of the file to be transferred>
in the given order.

Upon execution, the client program should be able to read data from the specified file and send it using the GBN protocol to the server. 
The window size should be set to N=7. Recall that this means that, even though we have 8 sequence numbers, we are only using 7 of these sequence numbers at any given time.
After all contents of the file have been transmitted successfully to the server and corresponding ACKs have been received, the client should send an EOT packet to the server. The EOT packet is in the same format (and it has a sequence number) as a regular data packet, except that its type field is set to 3, its length is set to zero, and the data is set to NULL.
The client can close its connection and exit only after it has received ACKs for all data packets it has sent and received an EOT from the server. To keep the project simple, you can assume that EOT packets are never lost in the network.
 
To ensure reliable transmission, your program should implement the GBN protocol as follows:
- If the client has a packet to send, it first checks to see if the window is full — that is, whether there are N outstanding, unacknowledged packets. If the window is not full, the packet is sent and the appropriate variables are updated. If the window is full, the client will try sending the packet later.
- A timer of 2 seconds is started if it was not done before. The client will use only a single timer that will be set for the oldest transmitted-but-not-yet-acknowledged packet.
- When the client receives an acknowledgement packet with sequence number n, the ACK will be taken to be a cumulative acknowledgement, indicating that all packets with a sequence number up to and including n have been correctly received at the server.
- If a timeout occurs, the client resends all packets that have been previously sent but that have not yet been acknowledged. If an ACK is received corresponding to an un-acked packet within the window, but there are still additional transmitted-but-yet-to-be-acknowledged packets, the timer is restarted with 2 seconds. If there are no outstanding packets, the timer is stopped.
-  There are two generic ways to structure the client when the window is filled. The first way is the client fills the window and then waits to receive all acks before checking the window again to see whether it can send. The second way is that your client can fill the window and then obtain an ack, check to see the window has space and send again, then check for acks again, etc. -- this always keeps the window full. Implement the second way.

### 1.1. Interrupting the Timer
The recvfrom() call will cause the client to block until a packet is received. This may cause problems for the correctness of your program unless you interrupt the recvfrom() call.
For example, imagine the case where the last packet from the client is sent and the client then calls recvfrom() to obtain the acknowledgement that should be returned from the server. However, assume that this last packet from the client is lost in transit. No acknowledgement will be transmitted back from the server. The client, who is now waiting to receive an ack, will block forever.
You must interrupt the blocking call to check whether the timer has expired and, if it has, resend all outstanding packets as specified by GBN. It is up to you to decide how to do this.

### 1.2 Output from Client
The client program will generate two log files, named as clientseqnum.log and clientack.log.
Whenever a packet is sent, its sequence number should be recorded in clientseqnum.log. This includes the EOT packet sent by the client.
The file clientack.log should record the sequence numbers of all the ACK packets and the EOT packet (from the server) that the client receives during the entire period of transmission.
Each instance the program is executed, these files should be overwritten (not appended to).
The format for these two log files is one number per line. You must follow this format to avoid losing marks as the TA will be using these log files to grade the correctness of your assignment.

## 2. Server Program (server)
You should implement the server program, named server. Its command line input includes the following:

<serverPort: UDP port number used by the server to receive data from the client>, 

<fileName: name of the file into which the received data is written>
in the given order.

When receiving packets sent by the client, it should execute the following:
- check the sequence number of the packet.
- if the sequence number is the one that it is expecting, it should send an ACK packet back to the client with the sequence number of the received packet (indicating it has received all packets up to and including this packet).
- In all other cases, it should discard the received packet and resends an ACK packet for the most recently received in-order packet.
- After the server has received all data packets and an EOT from the client, it should send an EOT packet with the type field set to 2, and then exit.
 Of course, the server must also write the received data from the client to the file fileName.
 
 ### 2.1 Output from Server
In addition to the file containing the text transferred from the client, the server program is also required to generate a log file, named as arrival.log. The file arrival.log should record the sequence numbers of all the data packets and the EOT packet (from the client) that the server receives during the entire period of transmission.
Each instance the program is executed, this file should be overwritten (not appended to). Similarly, in each instance the program is executed, fileName should be overwritten.
The format for the log file is one number per line. 
