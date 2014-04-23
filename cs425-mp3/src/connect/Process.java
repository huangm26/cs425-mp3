package connect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import message.Message;
import util.Configuration;

public class Process {
	public static int ID; // use id to link to port
	public static String IP;
	public static int myPort;
	public static int messageID;
	public static DatagramChannel mychannel;
	public static int delayTime = 0;
	public static int numProc; // number of processes
	public static Hashtable<Integer, String> store;
	public static Queue<Message> input_queue;
	
	public static void main(String args[]) throws IOException
	{
			// User determine id
			Scanner scanner = new Scanner(System.in);
			do {
				System.out.println("Enter the ID starting from 0 (to 5): ");
				ID = scanner.nextInt();
			} while (ID < 0 || ID > 5);

			
			
			// Get configuration values
			Configuration.getInstance();
			IP = Configuration.IP[ID];
			delayTime = Configuration.delayTime[ID];
			numProc = Configuration.numProc;
			
			
			//setup socket channel using UDP
			myPort = ID + 6000; // define every process's port by the ID
			mychannel = DatagramChannel.open();
			System.out.println(myPort);
			//bind to socket to specified IP and Port
			mychannel.socket().bind(new InetSocketAddress(InetAddress.getByName(IP), myPort));
			// set the channel to non-blocking
//			mychannel.configureBlocking(false);
			
			//instantiate variables
			input_queue = new LinkedList<Message>();
			store = new Hashtable<Integer, String>();
			
			//start ReadInput thread
			ReadInput input_thread = new ReadInput();
			new Thread(input_thread).start();
			
			//start send thread
			Process_send send_thread = new Process_send();
			new Thread(send_thread).start();
	}
	
	
	public static Message receive()
			throws IOException {
		
		Message message = null;

		ByteBuffer buffer = ByteBuffer.allocate(1000);
		
		while (mychannel.receive(buffer) == null) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		buffer.flip();
		ByteArrayInputStream in = new ByteArrayInputStream(buffer.array());
		ObjectInputStream is = new ObjectInputStream(in);

		try {
			message = (Message) is.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return message;
	}
}
