
//MARIA CRISTINA ZAMBON

//mz348

import java.net.*;
import java.io.*;

//documentation: professor's java example, java SE 8 doc from oracle.com, stackoverflow.com, https://www.geeksforgeeks.org
//
public class server {
	static final int EOT_clientTOserver = 3;
	static final int EOT_serverTOclient = 2;
	static final int data_packet = 1;
	static final int ack = 0;

	static public void sendPkt(packet pkt, int port, DatagramSocket dataTransferSocket, InetAddress address)
			throws Exception {

		ByteArrayOutputStream oSt = new ByteArrayOutputStream();
		ObjectOutputStream ooSt = new ObjectOutputStream(oSt);
		ooSt.writeObject(pkt);
		ooSt.flush();
		byte[] sendBuf = new byte[576];
		sendBuf = oSt.toByteArray();
		DatagramPacket PAK = new DatagramPacket(sendBuf, sendBuf.length, address, port);
		dataTransferSocket.send(PAK);
	}

	public static void main(String[] args) throws IOException, Exception, UnknownHostException, SocketException,
			FileNotFoundException, SocketTimeoutException, ClassNotFoundException {

		int n_port = Integer.parseInt(args[0]);
		DatagramSocket receiverSocket = new DatagramSocket(n_port);
		byte[] recBuf = new byte[576];
		// Stores the expected sequence number of the next packet to be received
		int expectedSeqNum = 0;
		int flag1 = 0, flag2=0;
		
		boolean isEOT_serverTOclient = false;
		// keep receiving packets until the special character is found
		// special character means that client has reached EOF and that has closed the
		// socket
		while (isEOT_serverTOclient == false) { // Creates a packet to receive data from the emulator and reads into
			// it from the receiver socket
			
			DatagramPacket recpacket = new DatagramPacket(recBuf, recBuf.length);
			receiverSocket.receive(recpacket);

			ByteArrayInputStream oSt = new ByteArrayInputStream(recpacket.getData(), recpacket.getOffset(),
					recpacket.getLength());
			ObjectInputStream ooSt = new ObjectInputStream(oSt);

			packet packetFromClient = (packet) ooSt.readObject();
			
			InetAddress IPAddress = recpacket.getAddress();
			// Reads in the sequence number of the received packet
			int seqNum = packetFromClient.getSeqNum();

			if (packetFromClient.getType() == data_packet) { // Received a data packet?
				// Writes the sequence number of the received packet to the arrival log
				flag1= openFileAndWrite(flag1, "arrival.log", seqNum+"\n");
			}

			if (seqNum == expectedSeqNum) { // server received the sequence number of the packet it was expecting?
					
				if (packetFromClient.getType() == data_packet) { // Received data packet?

					
					flag2= openFileAndWrite(flag2, args[1], packetFromClient.getData());

					// Creates an ACK packet with the same sequence number as the
					// received packet to send to the client and writes it out
					// to the receiver socket
					packet packetToClient = new packet(ack, expectedSeqNum, packetFromClient.getLength(),
							packetFromClient.getData().toUpperCase());

					sendPkt(packetToClient, recpacket.getPort(), receiverSocket, recpacket.getAddress());
					// Computes the expected sequence number of the next packet to be received
					expectedSeqNum = (expectedSeqNum + 1) % 8;

				}
				if (packetFromClient.getType() == EOT_clientTOserver) {// Received an EOT client to server packet?
					// Creates an EOT packet with the same seqnumb as the
					// received packet to send to the client but different type

					packet packetToClient = new packet(EOT_serverTOclient, expectedSeqNum, 0, null);
					sendPkt(packetToClient,  recpacket.getPort(), receiverSocket, recpacket.getAddress());
				
					isEOT_serverTOclient = true;
					flag1= openFileAndWrite(flag1, "arrival.log", packetFromClient.getSeqNum()+"\n");

					break; // exit the while, server closes the connection after EOT packet

				}
			} 
			/*else {
				System.out.println("reached this code");
				// Computes the sequence number of the most recently received in-order packet
				int lastSeqNum = (expectedSeqNum-2) % 8;

				if (lastSeqNum < 0) { // Got a negative modulus?
					// Computes a positive modulus
					lastSeqNum = lastSeqNum + 8;
				} 
				packet packetToClient = new packet(ack, lastSeqNum, packetFromClient.getLength(),
						packetFromClient.getData().toUpperCase());

				sendPkt(packetToClient, n_port, receiverSocket, IPAddress);

			}*/
		}
		receiverSocket.close(); // close socket

	}
	
	
	static public int openFileAndWrite(int flag, String filename, String num) throws Exception {

		File log_file = new File(filename);
		if (!log_file.exists()) { // if file doesnt exists
			log_file.createNewFile(); // create a new one
			flag = 1;
		}
		if (log_file.exists() && flag == 0) { // if exists and first time checking
			log_file.delete(); // delete the content previously stored
			log_file.createNewFile(); // create a new one
			flag = 1; // setting the flag means not doing again this procedure
		}
		FileWriter fw = new FileWriter(log_file.getAbsoluteFile(), true);
		BufferedWriter LogWriter = new BufferedWriter(fw); // creating bufferWriter to write the
															// content
		LogWriter.write(num);
		LogWriter.close();
		return flag;
	}
}
