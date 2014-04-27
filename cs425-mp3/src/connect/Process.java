package connect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

import message.Delete;
import message.Get;
import message.Get_resp;
import message.Insert;
import message.Insert_ack;
import message.Message;
import message.Update;
import message.Update_ack;
import util.Configuration;

public class Process {
	public static int ID; // use id to link to port
	public static String IP;
	public static int myPort;
	public static int messageID;
	public static DatagramChannel myChannel;
	public static int numProc; // number of processes
	public static int avgDelayTo1, avgDelayTo2, avgDelayTo3;
	public static Hashtable<Integer, String> dataStore; // Hashtable is already
														// synchronized
	public static Queue<Message> inputQueue;
	public static Queue<Message> receiveQueue;
	// public static ArrayList<Message> ackList;
	public static boolean[][] ack;
	public static boolean[] get_level_one;
	// starts from 0, and if reaches 3, means it have get all responses
	public static int[] get_level_all;
	public static Get_resp[] store_resp;

	public static void main(String args[]) throws IOException,
			InterruptedException {

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
		receiveQueue = new LinkedList<Message>();
		dataStore = new Hashtable<Integer, String>();
		messageID = 0;
		// ackList = new ArrayList<Message>();
		ack = new boolean[10000][numProc];
		get_level_one = new boolean[10000];
		get_level_all = new int[10000];
		store_resp = new Get_resp[10000];
		init_array();

		// start ReadInput thread
		ReadInput inputThread = new ReadInput();
		new Thread(inputThread).start();

		// start send thread
		ProcessSend sendThread = new ProcessSend();
		new Thread(sendThread).start();

		// receiving
		while (true) {
			receiveAll();
		}
	}

	public static void receiveAll() throws IOException, InterruptedException {
		// System.out.println("receiving");
		Message msg = null;
		receiveQueue.add(receive());
		while (receiveQueue.peek() != null) {
			// System.out.println("have");
			msg = receiveQueue.poll();
			if (msg.isGet()) {
				System.out.println("get");
				onRecvGet((Get) msg);
			} else if (msg.isInsert()) {
				System.out.println("insert");
				onRecvInsert((Insert) msg);
			} else if (msg.isUpdate()) {
				System.out.println("update");
				onRecvUpdate((Update) msg);
			} else if (msg.isDelete()) {
				System.out.println("delete");
				onRecvDelete((Delete) msg);
			} else if (msg.isGet_resp()) {
				System.out.println("Get_resp");
				onRecvGet_resp((Get_resp) msg);
			} else if (msg.isInsert_ack()) {
				System.out.println("Insert_ack");
				onRecvInsert_ack((Insert_ack) msg);

			} else if (msg.isUpdate_ack()) {
				System.out.println("Update_ack");
				onRecvUpdate_ack((Update_ack) msg);
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

	private static void onRecvGet(Get g) throws IOException,
			InterruptedException {
		if (dataStore.containsKey(g.key)) {
			String content = dataStore.get(g.key);

			// System.out.println("This is before resp get " + content);
			Get_resp resp = new Get_resp(Process.ID, g.from, g.timeStamp,
					g.key, g.messageID, content, g.level);
			send((Message) resp, g.from);
		} else {
			String content = null;
			Get_resp resp = new Get_resp(Process.ID, g.from, g.timeStamp,
					g.key, g.messageID, content, g.level);
			send((Message) resp, g.from);
		}
	}

	private static void onRecvInsert(Insert i) throws IOException,
			InterruptedException {
		if (!dataStore.containsKey(i.key)) {
			dataStore.put(i.key, i.value);
		}
		// no matter contains key or not, send ack anyway to prevent deadlock
		Insert_ack ack = new Insert_ack(Process.ID, i.from, i.timeStamp, i.key,
				i.messageID);
		send((Message) ack, i.from);
	}

	private static void onRecvUpdate(Update u) throws IOException,
			InterruptedException {
		// If key already exists, the old value will be replaced
		if (dataStore.containsKey(u.key)) {
			dataStore.put(u.key, u.value);
		}
		Update_ack ack = new Update_ack(Process.ID, u.from, u.timeStamp, u.key,
				u.messageID);
		send((Message) ack, u.from);
	}

	private static void onRecvDelete(Delete d) {
		if (dataStore.containsKey(d.key)) {
			dataStore.remove(d.key);
		}
		// ////not sure if needs delete
	}

	private static void onRecvInsert_ack(Insert_ack ack) {
		// mark the ack as true
		// System.out.println("ack from " + ack.from);
		// System.out.println("messageID " + ack.messageID);
		Process.ack[ack.messageID][ack.from] = true;
	}

	private static void onRecvUpdate_ack(Update_ack ack) {
		// mark the ack as true
		Process.ack[ack.messageID][ack.from] = true;
	}

	private static void onRecvGet_resp(Get_resp resp) {
		// mark the ack as true
		Process.ack[resp.messageID][resp.from] = true;

		// It is a level one response
		if (resp.level == 1) {
			// if haven't received response of this message
			if (!Process.get_level_one[resp.messageID]) {
				System.out.println("***************");
				System.out.println("This is the result from get: "
						+ resp.content);
				System.out.println("***************");
				Process.get_level_one[resp.messageID] = true;
			}
		} else if (resp.level == 9)
		// it is a level 9 response
		{
			Process.get_level_all[resp.messageID]++;

			// if this is the first response, store it
			if (store_resp[resp.messageID] == null) {
				store_resp[resp.messageID] = resp;
			} else
			// if not first one, compare
			{
				// the received response is the latest
				if (resp.timeStamp
						.compareTo(store_resp[resp.messageID].timeStamp) > 0) {
					store_resp[resp.messageID] = resp;
				}
			}

			// has get all responses
			if (Process.get_level_all[resp.messageID] == 3) {
				System.out.println("***************");
				System.out.println("This is the result from get: "
						+ store_resp[resp.messageID].content);
				System.out.println("***************");
			}
		}

	}

	private static void init_array() {
		for (int i = 0; i < 10000; i++) {
			for (int j = 0; j < 3; j++) {
				ack[i][j] = false;
			}
			get_level_one[i] = false;
			get_level_all[i] = 0;
			store_resp[i] = null;
		}
	}

	private static void send(Message message, int to) throws IOException,
			InterruptedException {
		System.out.println("sending response");
		Random rand = new Random();
		// Delay in range [0, 2*mean delay]
		// int randomDelay = rand.nextInt(2 * Process.delayTime + 1);

		DatagramChannel channel;
		channel = DatagramChannel.open();
		int destPort = 6000 + to;
		try {
			InetSocketAddress destAddress = new InetSocketAddress(
					InetAddress.getByName(Process.IP), destPort);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(message);
			byte[] data = outputStream.toByteArray();
			ByteBuffer buffer = ByteBuffer.wrap(data);
			// channel.connect(new
			// InetSocketAddress(InetAddress.getByName(Process.IP), destPort));
			int bytesend = channel.send(buffer, new InetSocketAddress(
					InetAddress.getByName(Process.IP), destPort));
			// int bytesend = channel.write(buffer);
			// channel.disconnect();
			// System.out.println(String.format("send %d bytes from %d to %d",
			// bytesend, message.from, message.to));
			channel.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}
