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

import message.Delete;
import message.Get;
import message.Insert;
import message.Message;
import message.Update;
import util.Configuration;

public class Process {
	public static int ID; // use id to link to port
	public static String IP;
	public static int myPort;
	public static int messageID;
	public static DatagramChannel myChannel;
	public static int numProc; // number of processes
	public static int avgDelayTo1, avgDelayTo2, avgDelayTo3;
	public static Hashtable<Integer, String> dataStore;
	public static Queue<Message> inputQueue;

	public static void main(String args[]) throws IOException {

		// Get configuration values
		Configuration.getInstance();
		IP = Configuration.IP[ID];
		numProc = Configuration.numProc;
		avgDelayTo1 = Configuration.avgDelayTo[0];
		avgDelayTo2 = Configuration.avgDelayTo[1];
		avgDelayTo3 = Configuration.avgDelayTo[2];

		// User determine id
		Scanner scanner = new Scanner(System.in);
		do {
			System.out.println(String.format(
					"Enter the ID starting from 0 (up to %d): ", numProc - 1));
			ID = scanner.nextInt();
		} while (ID < 0 || ID > 5);

		// setup socket channel using UDP
		myPort = ID + 6000; // define every process's port by the ID
		myChannel = DatagramChannel.open();
		System.out.println(myPort);
		// bind to socket to specified IP and Port
		myChannel.socket().bind(
				new InetSocketAddress(InetAddress.getByName(IP), myPort));
		// set the channel to non-blocking
		// mychannel.configureBlocking(false);

		// instantiate variables
		inputQueue = new LinkedList<Message>();
		dataStore = new Hashtable<Integer, String>();

		// start ReadInput thread
		ReadInput inputThread = new ReadInput();
		new Thread(inputThread).start();

		// start send thread
		ProcessSend sendThread = new ProcessSend();
		new Thread(sendThread).start();

		// receiving
		while (true) {

		}
	}

	public static void onGet(Get g) {
		
	}

	public static void onInsert(Insert i) {
		if (!dataStore.contains(i.key)) {
			dataStore.put(i.key, i.value);
		}
	}

	public static void onUpdate(Update u) {
		
	}

	public static void onDelete(Delete d) {
		if (dataStore.contains(d.key)) {
			dataStore.remove(d.key);
		}
	}

	public static void receiveAll() throws IOException {
		Message recvMsg = null;
		for (int i = 0; i < numProc; i++) {
			recvMsg = receive();
			if (recvMsg.isGet()) {
				System.out.println("get");
			} else if (recvMsg.isInsert()) {
				System.out.println("insert");
			} else if (recvMsg.isUpdate()) {
				System.out.println("update");
			} else {
				System.out.println("delete");
			}
		}
	}

	public static Message receive() throws IOException {

		Message message = null;

		ByteBuffer buffer = ByteBuffer.allocate(1000);

		while (myChannel.receive(buffer) == null) {
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
