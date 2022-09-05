//MARIA CRISTINA ZAMBON
//mz348

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
//documentation: professor's java example, java SE 8 doc from oracle.com, stackoverflow.com, https://www.geeksforgeeks.org

public class client {

	public static void main(String[] args) {
		DatagramSocket dSocket = null;
		InetAddress address = null;

		String serverName = args[0];

		int port = Integer.parseInt(args[1]);

		String handshakeMSG = "1234";
		int newPort = 0;

		try {
			address = InetAddress.getByName(serverName);

			dSocket = new DatagramSocket();

			byte[] sendBuf = new byte[20];
			sendBuf = handshakeMSG.getBytes(); 

			DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, port); // create datagram
			dSocket.send(packet); // send the message to the server

			// Receive  ACK from the server
			byte[] recBuf = new byte[1024];
			DatagramPacket recpacket = new DatagramPacket(recBuf, recBuf.length);
			dSocket.receive(recpacket);

			handshakeMSG = new String(recpacket.getData());

			dSocket.close();
		}

		catch (NumberFormatException e) {
		} catch (UnknownHostException e) {
			System.out.println("Unknown host.");
		} catch (IOException e) {
			System.out.println("I/O error");
		}

		try {
			String txt = handshakeMSG.trim().toString();
			newPort = Integer.parseInt(txt);
		} catch (NumberFormatException e) {
		}

//STAGE2________
		DatagramSocket dSocket1 = null;
		InetAddress address1 = null;

		try {
			
			//open the file from command line
			Reader file = new FileReader(args[2]);
			BufferedReader br = new BufferedReader(file);

			address1 = InetAddress.getByName(serverName);
			dSocket1 = new DatagramSocket();

			char[] payload = new char[4];
			int flag = 0; //flag stores the number of characters that have been read

			//read method returns the number of characters that have been read OR -1 when EOF
			while ((flag = br.read(payload, 0, 4)) != -1) {
				byte[] sendBuf;

				if (flag == 4) {
					sendBuf = new byte[4];
				} else {
					sendBuf = new byte[flag];
					int i = flag;
					while (i < 4) {
		//use special character '#' to tell the server that clients has closed the connection
						payload[i] = '#';
						i++;
					}
				}
				sendBuf = new String(payload).getBytes(StandardCharsets.US_ASCII);

				DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address1, newPort); // create
																										// datagram
				dSocket1.send(packet); // send the message to the server

				// Receive the ACKNOWLEDGEMENT from the server
				byte[] recBuf = new byte[4];
				DatagramPacket recpacket = new DatagramPacket(recBuf, recBuf.length);
				dSocket1.receive(recpacket);

				String returnedMsg = new String(recpacket.getData());

				System.out.println(returnedMsg);

			} // end while

			file.close();
			if (flag == -1) {

				byte[] sendBuf = new byte[4];
				String closing = new String(payload);
				sendBuf = closing.getBytes(StandardCharsets.US_ASCII);

				DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address1, newPort); // create
																										// datagram
				dSocket1.send(packet); // send the message to the server

			}
			dSocket.close(); // close the UDP socket
		} // end try
		catch (FileNotFoundException e1) {
			System.out.println("file not found stage 2");
		}

		catch (UnknownHostException e) {
			System.out.println("Unknown host.");
		} catch (IOException e) {
			System.out.println("I/O error stage 2");
		}

	}
}
