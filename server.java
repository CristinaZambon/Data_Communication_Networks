//MARIA CRISTINA ZAMBON
//mz348

import java.net.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;

// documentation: professor's java example, java SE 8 doc from oracle.com, stackoverflow.com, https://www.geeksforgeeks.org
//
public class server {

	public static void main(String args[]) {

		int n_port = Integer.parseInt(args[0]);

		// for the UDP component
		byte[] recBuf = new byte[1024]; // buffer for holding the incoming datagram
		byte[] sendBuf = new byte[1024];
		DatagramSocket dserverSocket = null;

		try {
			// receive the msg from client using UDP socket
			String message = "";
			byte[] buffer = new byte[1024];
			dserverSocket = new DatagramSocket(n_port);

			recBuf = new byte[1024];
			DatagramPacket recpacket = new DatagramPacket(recBuf, recBuf.length);
			dserverSocket.receive(recpacket);
			buffer = recpacket.getData();

			message = new String(buffer, 0, recpacket.getLength());

			int randomPort = ThreadLocalRandom.current().nextInt(1024, 65535 + 1);

			InetAddress IPAddress = recpacket.getAddress();
			int port = recpacket.getPort();
			message = Integer.toString(randomPort);
			sendBuf = message.getBytes(); // convert payload to uppercase and repack

			DatagramPacket sendpacket = new DatagramPacket(sendBuf, sendBuf.length, IPAddress, port);
			dserverSocket.send(sendpacket);

			System.out.println("The random port chosen is " + message);

			n_port = randomPort;

			dserverSocket.close(); // close socket
		} // end try
		catch (SocketTimeoutException t) { // catch a timeout event
			System.out.println("Socket time out event");
		} catch (IOException e) { // catch IO exception
			System.out.println("I/O exception stage 1");
		}

		// STAGE 2____________________________

		// for the UDP component
		DatagramSocket dserverSocket1 = null;

		try {

			String receivedTxt = "";
			byte[] buffer = new byte[4];
			dserverSocket1 = new DatagramSocket(n_port);

			byte[] recBuf2 = new byte[4];
			byte[] sendBuf2 = new byte[4];
			
			int flag = 0;
			
			//keep receiving packets until the special character is found
			//special character means that client has reached EOF and that has closed the socket
			while (!receivedTxt.contains("#"))

			{
				DatagramPacket recpacket = new DatagramPacket(recBuf2, recBuf2.length);
				dserverSocket1.receive(recpacket);

				buffer = recpacket.getData();

				receivedTxt = new String(buffer, 0, recpacket.getLength());

				// writing on a file
				File file = new File("blah.txt");
				if (!file.exists()) { // if file doesnt exists 
					file.createNewFile(); //create a new one
					flag = 1;
				}
				if (file.exists() && flag == 0) { //if exists and first time checking
					file.delete(); //delete the content previously stored
					file.createNewFile(); //create a new one
					flag = 1; //setting the flag means not doing again this procedure
				}

				InetAddress IPAddress = recpacket.getAddress();
				int port = recpacket.getPort();
				
				//need to delete the special characters before writing on file and before sending back to client
				String checkTEXT = receivedTxt.replace("#", "");
				sendBuf2 = (checkTEXT.toUpperCase()).getBytes(); // convert text to uppercase and repack

				DatagramPacket sendpacket = new DatagramPacket(sendBuf2, sendBuf2.length, IPAddress, port);
				dserverSocket1.send(sendpacket);

				// creating fileWriter object with the file
				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true); 
				BufferedWriter bw = new BufferedWriter(fw); // creating bufferWriter to write the content
														
				bw.write(checkTEXT); // write method  to write the content into the file
				bw.close();

			}

			dserverSocket1.close(); // close socket

		} // end try

		catch (SocketTimeoutException t) { // catch a timeout event
			System.out.println("Socket time out event");
		} catch (IOException e) { // catch IO exception
			System.out.println("I/O exception stage 2");
		}
	}
}
