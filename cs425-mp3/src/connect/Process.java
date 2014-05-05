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
import java.util.Date;
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
import message.RepairAck;
import message.RepairRequest;
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
	public static Hashtable<Integer, Date> dataTimetable;

	public static Queue<Message> inputQueue;
	public static Queue<Message> receiveQueue;
	// public static ArrayList<Message> ackList;
	public static boolean[][] ack;
	public static boolean[] get_level_one;
	// starts from 0, and if reaches 3, means it have get all responses
	public static int[] get_level_all;
	public static Get_resp[] store_resp;
	public static int[] repairAck;

	public static final int MAX_MESSAGE_NUM = 10000;

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
		dataTimetable = new Hashtable<Integer, Date>();
		messageID = 0;
		// ackList = new ArrayList<Message>();
		ack = new boolean[MAX_MESSAGE_NUM][numProc];
		repairAck = new int[MAX_MESSAGE_NUM];
		get_level_one = new boolean[MAX_MESSAGE_NUM];
		get_level_all = new int[MAX_MESSAGE_NUM];
		store_resp = new Get_resp[MAX_MESSAGE_NUM];
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
		Message msg = null;
		receiveQueue.add(receive());
		while (receiveQueue.peek() != null) {
			msg = receiveQueue.poll();
			if (msg.isGet()) {
				onRecvGet((Get) msg);
			} else if (msg.isInsert()) {
				onRecvInsert((Insert) msg);
			} else if (msg.isUpdate()) {
				onRecvUpdate((Update) msg);
			} else if (msg.isDelete()) {
				onRecvDelete((Delete) msg);
			} else if (msg.isGet_resp()) {
				onRecvGet_resp((Get_resp) msg);
			} else if (msg.isInsert_ack()) {
				onRecvInsert_ack((Insert_ack) msg);
			} else if (msg.isUpdate_ack()) {
				onRecvUpdate_ack((Update_ack) msg);
			} else if (msg.isRepairRequest()) {
				onRecvRepairRequest((RepairRequest) msg);
			} else if (msg.isRepairAck()) {
				onRecvRepairAck((RepairAck) msg);
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
		String value = null;
		Date timeStamp = null;
		if (dataStore.containsKey(g.key)) {
			value = dataStore.get(g.key);
			timeStamp = dataTimetable.get(g.key);
		}
		Get_resp resp = new Get_resp(ID, g.from, timeStamp, g.key, g.messageID,
				value, g.level);
		send((Message) resp, g.from);
	}

	private static void onRecvInsert(Insert i) throws IOException,
			InterruptedException {
		if (!dataStore.containsKey(i.key)) {
			dataStore.put(i.key, i.value);
			dataTimetable.put(i.key, i.timeStamp);
		}
		// no matter contains key or not, send ack anyway to prevent deadlock
		Insert_ack ack = new Insert_ack(ID, i.from, i.key, i.messageID);
		send((Message) ack, i.from);
	}

	private static void onRecvUpdate(Update u) throws IOException,
			InterruptedException {
		// If key already exists, the old value will be replaced
		if (dataStore.containsKey(u.key)) {
			dataStore.put(u.key, u.value);
			dataTimetable.put(u.key, u.timeStamp);
		}
		Update_ack ack = new Update_ack(ID, u.from, u.key, u.messageID);
		send((Message) ack, u.from);
	}

	private static void onRecvDelete(Delete d) {
		if (dataStore.containsKey(d.key)) {
			dataStore.remove(d.key);
			dataTimetable.remove(d.key);
		}
		// ////not sure if needs delete
	}

	private static void onRecvInsert_ack(Insert_ack iAck) {
		// mark the ack as true
		// System.out.println("ack from " + ack.from);
		// System.out.println("messageID " + ack.messageID);
		ack[iAck.messageID][iAck.from] = true;
	}

	private static void onRecvUpdate_ack(Update_ack uAck) {
		// mark the ack as true
		ack[uAck.messageID][uAck.from] = true;
	}

	private static void onRecvGet_resp(Get_resp resp) {
		// mark the ack as true
		ack[resp.messageID][resp.from] = true;

		// It is a level one response
		if (resp.level == 1) {
			// if haven't received response of this message
			if (!get_level_one[resp.messageID]) {
				System.out.println("***************");
				System.out
						.println("This is the result from get: " + resp.value);
				System.out.println("***************");
				get_level_one[resp.messageID] = true;
			}
		}

		get_level_all[resp.messageID]++;
		// if this is the first response, store it
		if (store_resp[resp.messageID] == null) {
			store_resp[resp.messageID] = resp;
		} else {
			// if not first one, compare
			// the received response is the latest
			if (resp.timeStamp.compareTo(store_resp[resp.messageID].timeStamp) > 0) {
				store_resp[resp.messageID] = resp;
			}
		}

		// has got all responses
		if (get_level_all[resp.messageID] == 3) {
			Get_resp targetResp = store_resp[resp.messageID];
			if (resp.level == 9) {
				System.out.println("***************");
				System.out.println("result for GET: " + targetResp.value);
				System.out.println("***************");
			}
			// starting Read Repair once all responses received
			// no matter what consistency level is applied
			repairInBackground(targetResp);
		}

	}

	private static void repairInBackground(Get_resp resp) {
		RepairThread rt = new RepairThread(resp);
		new Thread(rt).start();
	}

	private static void onRecvRepairRequest(RepairRequest rr)
			throws IOException, InterruptedException {
		if (dataStore.containsKey(rr.key)) {
			// Update local table to latest data
			System.out.println(String.format(
					"Repairing server %d's [%d => %s] to [%d => %s]", ID, rr.key,
					dataStore.get(rr.key), rr.key, rr.value));
			dataStore.put(rr.key, rr.value);
			dataTimetable.put(rr.key, rr.timeStamp);
			// Send back ack
			RepairAck ra = new RepairAck(ID, rr.from, rr.key, rr.messageID);
			send(ra, rr.from);
		} else {
			System.out.println("****SOMETHING WRONG IF THIS LINE APPEAR****");
		}
	}

	private static void onRecvRepairAck(RepairAck ra) {
		// using a new ack table, each GET message id corresponds to a
		// repair operation
		repairAck[ra.messageID]++;
		if (repairAck[ra.messageID] == 3) {
			System.out.println(String.format("Repair for key %d successful",
					ra.key));
		}
	}

	private static void init_array() {
		for (int i = 0; i < MAX_MESSAGE_NUM; i++) {
			for (int j = 0; j < 3; j++) {
				ack[i][j] = false;
			}
			get_level_one[i] = false;
			get_level_all[i] = 0;
			store_resp[i] = null;
			repairAck[i] = 0;
		}
	}

	private static void send(Message message, int to) throws IOException,
			InterruptedException {
		Random rand = new Random();
		// Delay in range [0, 2*mean delay]
		// int randomDelay = rand.nextInt(2 * delayTime + 1);

		DatagramChannel channel;
		channel = DatagramChannel.open();
		int destPort = 6000 + to;
		try {
			InetSocketAddress destAddress = new InetSocketAddress(
					InetAddress.getByName(IP), destPort);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(message);
			byte[] data = outputStream.toByteArray();
			ByteBuffer buffer = ByteBuffer.wrap(data);
			// channel.connect(new
			// InetSocketAddress(InetAddress.getByName(IP), destPort));
			int bytesend = channel.send(buffer, new InetSocketAddress(
					InetAddress.getByName(IP), destPort));
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
