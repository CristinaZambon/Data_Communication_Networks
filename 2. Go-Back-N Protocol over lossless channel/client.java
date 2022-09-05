//MARIA CRISTINA ZAMBON
//mz348

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.LinkedList;
//import java.util.Timer;
//import java.util.TimerTask;
//system. nanotime(
//documentation: professor's java example, java SE 8 doc from oracle.com, stackoverflow.com, https://www.geeksforgeeks.org 
//https://github.com/elailai94/The-Go-Back-N-Protocol

public class client {
	static final int WINDOW_SIZE = 7;
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
		byte[] sendBuf = new byte[30];
		sendBuf = oSt.toByteArray();
		DatagramPacket PAK = new DatagramPacket(sendBuf, sendBuf.length, address, port);
		dataTransferSocket.send(PAK);
	}

	static public int openFileAndWrite(int flag, String filename, int num) throws Exception {

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
		LogWriter.write(num + "\n");
		LogWriter.close();
		return flag;
	}

	public static void main(String[] args) throws IOException, Exception, UnknownHostException, SocketException,
			FileNotFoundException, ClassNotFoundException {

		String serverName = args[0];
		int port = Integer.parseInt(args[1]);
		DatagramSocket dSocket = new DatagramSocket();
		InetAddress address = InetAddress.getByName(serverName);

		int base = 0; // Stores seqnum of the packet sent-but not acknowledged
		int nextSeqNum = 0; // Stores seq number of the next packet to be sent
		boolean reachedEOF = false; // Stores if EOT packet is sent

		// Stores all the packets sent-but not acknowledged in a buffer queue
		LinkedList<packet> notACKed_sentPackets = new LinkedList<packet>();

		Reader file = new FileReader(args[2]); // open the file from command line
		BufferedReader br = new BufferedReader(file);
		int flag1 = 0;
		int flag2 = 0;

		/*
		 * Timer timer = new Timer(); TimerTask task = new TimerTask() { //
		 * 
		 * @Override public void run() { System.out.println("\n re sending packets|\n");
		 * 
		 * for (packet pk : notACKed_sentPackets) { try { sendPkt(pk,port, dSocket,
		 * address); } catch (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } if (pk.getType()==data_packet) { // Sent a data
		 * packet? // Writes the sequence number of the sent packet to try { //flag1 =
		 * openFileAndWrite(flag1, "clientseqnum.log", pk.getSeqNum()); } catch
		 * (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 * 
		 * } }
		 * 
		 * return; } };
		 */
		// read method returns the number of characters that have been read OR -1 when
		// EOF
		while (true) {
			packet pkt = null;
			if (!isWindowFull(base, nextSeqNum) && !reachedEOF) {
				char[] payload = new char[30];

				if (br.read(payload, 0, 30) == -1) { // reached EOF-->send EOT packet from client to server
					// pkt = new packet(EOT_clientTOserver, nextSeqNum, 0, null);
					reachedEOF = true;
				} else { // create a data packet
					String data = new String(payload);
					pkt = new packet(data_packet, nextSeqNum, data.length(), data);

					notACKed_sentPackets.offer(pkt); // add packet to queue tail
					sendPkt(pkt, port, dSocket, address); // send packet to server
					/*
					 * if (base == nextSeqNum) { timer = new Timer(); timer.schedule(task, 2000); }
					 */

					if (pkt.getType() == data_packet) { // Sent a data packet?
						// Writes the sequence number of the sent packet to clientseqnum.log file
						flag1 = openFileAndWrite(flag1, "clientseqnum.log", pkt.getSeqNum());
					}

					// Computes the sequence number of the next packet to be sent
					nextSeqNum = (nextSeqNum + 1) % 8;
					
					if (base == nextSeqNum) {
						try {
							dSocket.setSoTimeout(2000);
							
						} catch (SocketException  e) {
							reSendPkt(notACKed_sentPackets, port, dSocket, address);
						}
					}
				}
			} // end of assuming window not full and not end of transmission from client side

			// now assume window is full
			if (notACKed_sentPackets.size() == WINDOW_SIZE || reachedEOF) {

				// Creates a packet to receive data from the server
				byte[] recBuf = new byte[576];
				DatagramPacket recpacket = new DatagramPacket(recBuf, recBuf.length);

				dSocket.receive(recpacket);

				ByteArrayInputStream oSt = new ByteArrayInputStream(recpacket.getData(), recpacket.getOffset(),
						recpacket.getLength());
				ObjectInputStream ooSt = new ObjectInputStream(oSt);
				packet packetFromServer = (packet) ooSt.readObject();

				if (packetFromServer.getType() == ack) { // Received an ACK packet?
					// Writes the sequence number of the received packet to the ACK

					flag2 = openFileAndWrite(flag2, "clientack.log", packetFromServer.getSeqNum());

				}
				// Computes the seq_num of the packet that has been
				// previously sent but has not yet been acknowledged
				base = (packetFromServer.getSeqNum() + 1) % 8;
				

				removeACKedPacketsSent(notACKed_sentPackets, base);
				
				
				
				if (base != nextSeqNum) { 
				try {
					dSocket.setSoTimeout(2000);
					
				} catch (SocketException  e) {
					reSendPkt(notACKed_sentPackets, port, dSocket, address);
				}}
			}

			if (notACKed_sentPackets.size() == 0 && reachedEOF) {
				// timer.cancel();
				pkt = new packet(EOT_clientTOserver, nextSeqNum, 0, "EOT from client");
				sendPkt(pkt, port, dSocket, address);
				flag1 = openFileAndWrite(flag1, "clientseqnum.log", pkt.getSeqNum());

				byte[] recBuf = new byte[576];
				DatagramPacket recpacket = new DatagramPacket(recBuf, recBuf.length);
				dSocket.receive(recpacket);
				ByteArrayInputStream oSt = new ByteArrayInputStream(recpacket.getData(), recpacket.getOffset(),
						recpacket.getLength());
				ObjectInputStream ooSt = new ObjectInputStream(oSt);
				packet packetFromServer = (packet) ooSt.readObject();

				if (packetFromServer.getType() == EOT_serverTOclient) { // Received the EOT from server?
					// Writes the sequence number of the packet to client ack log
					flag2 = openFileAndWrite(flag2, "clientack.log", packetFromServer.getSeqNum());
					
					break;
				}

			}
		}
		dSocket.close(); // close the UDP socket
		br.close();
	}

	private static boolean isWindowFull(int base, int nextSeqNum) {
		final int WINDOW_SIZE = 7;
		if ((base + WINDOW_SIZE) >= 8) {

			if ((nextSeqNum >= base) && (nextSeqNum < 8)) {
				return false;
			} else if ((nextSeqNum >= 0) && (nextSeqNum < ((base + WINDOW_SIZE) % 8))) {
				return false;
			} else {
				return true;
			} // if

		} else {

			if ((nextSeqNum >= base) && (nextSeqNum < (base + WINDOW_SIZE))) {
				return false;
			} else {
				return true;
			}
		}
	}

	// Removes all the acknowledged packets sent to the server from the buffer queue
	private static void removeACKedPacketsSent(LinkedList<packet> unACKedPacketsSent, int base) {
		Iterator<packet> it = unACKedPacketsSent.iterator();

		while (it.hasNext()) {
			packet packetToEmulator = it.next();

			if (packetToEmulator.getSeqNum() == base) {
				break;
			} else {
				it.remove();
			} // if
		}
	}

	private static void reSendPkt(LinkedList<packet> unACKedPacketsSent, int port, DatagramSocket dataTransferSocket,
			InetAddress address) {

		System.out.println("\n re sending packets|\n");

		for (packet pk : unACKedPacketsSent) {
			try {
				sendPkt(pk, port, dataTransferSocket, address);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (pk.getType() == data_packet) { // Sent a data packet? // Writes the sequence number of the sent packet
												// to
				try {
					// flag1 = openFileAndWrite(flag1, "clientseqnum.log", pk.getSeqNum());
					BufferedWriter b = new BufferedWriter(new FileWriter("clientseqnum.logt", true));
					PrintWriter p = new PrintWriter(b);
					p.println(pk.getSeqNum());
					p.flush();
					p.close();
					b.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

}
