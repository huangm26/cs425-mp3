package connect;

import java.util.Date;
import java.util.Map.Entry;
import java.util.Scanner;

import message.Delete;
import message.Get;
import message.Insert;
import message.Message;
import message.Update;

;

public class ReadInput implements Runnable {

	// this is the thread for reading input from stdin and put the inputted
	// message into input_queue
	@Override
	public void run() {

		while (true) {
			int key;
			String value;
			int level;
			int destID;

			Boolean validInput = false;
			Scanner scanner = new Scanner(System.in);

			do {
				System.out.println("Enter your command");
				String content = scanner.nextLine();
				Date timeStamp = new Date();
				if (content != null) {
					String[] contentArr = content.split(" ");
					int len = contentArr.length;
					// Command only valid with length between 2 and 4
					if (len > 1 && len < 5) {
						switch (contentArr[0].toLowerCase()) {

						// delete operation
						case "delete":
							if (len == 2 && isInteger(contentArr[1])) {
								key = Integer.valueOf(contentArr[1]);
								destID = getHashingValue(key);
								Delete delete = new Delete(Process.ID, destID,
										key, Process.messageID);
								afterMessageGenerated(delete, validInput);
							}
							break;

						// get operation
						case "get":
							if (len == 3 && isInteger(contentArr[1])
									&& isInteger(contentArr[2])) {
								key = Integer.valueOf(contentArr[1]);
								level = Integer.valueOf(contentArr[2]);
								destID = getHashingValue(key);
								Get get = new Get(Process.ID, destID, key,
										Process.messageID, level);
								afterMessageGenerated(get, validInput);
							}
							break;

						// insert operation
						case "insert":
							if (len == 4 && isInteger(contentArr[1])
									&& isInteger(contentArr[3])) {
								key = Integer.valueOf(contentArr[1]);
								value = contentArr[2];
								level = Integer.valueOf(contentArr[3]);
								destID = getHashingValue(key);
								System.out.println("Inserting to server "
										+ destID);
								Insert insert = new Insert(Process.ID, destID,
										timeStamp, key, Process.messageID, value,
										level);
								afterMessageGenerated(insert, validInput);
							}
							break;

						// update operation
						case "update":
							if (len == 4 && isInteger(contentArr[1])
									&& isInteger(contentArr[3])) {
								key = Integer.valueOf(contentArr[1]);
								value = contentArr[2];
								level = Integer.valueOf(contentArr[3]);
								destID = getHashingValue(key);
								Update update = new Update(Process.ID, destID,
										timeStamp, key, Process.messageID, value,
										level);
								afterMessageGenerated(update, validInput);
							}
							break;

						// search key
						case "search":
							if (len == 2 && isInteger(contentArr[1])) {
								int[] place = new int[3];
								key = Integer.valueOf(contentArr[1]);
								destID = getHashingValue(key);
								if (destID == 0) {
									place[0] = Process.numProc - 1;
									place[1] = destID;
									place[2] = 1;
								} else if (destID == Process.numProc - 1) {
									place[0] = Process.numProc - 2;
									place[1] = Process.numProc - 1;
									place[2] = 0;
								} else {
									place[0] = destID - 1;
									place[1] = destID;
									place[2] = destID + 1;
								}
								System.out.println(String.format(
										"Servers that store the key %d", key));
								System.out.println("***************");
								System.out.println(String.format("%d %d %d",
										place[0], place[1], place[2]));
								System.out.println("***************");
							}
							break;
						}
					}
					// show all
					else if (content.equals("show-all")) {
						System.out.println("All key-value pairs stored are");
						System.out.println("***************");
						for (Entry<Integer, String> entry : Process.dataStore
								.entrySet()) {
							key = entry.getKey();
							value = entry.getValue();
							Date t = Process.dataTimetable.get(key);
							System.out.println(String.format("%d => %s [%s]", key,
									value, t.toString()));
						}
						System.out.println("***************");
					}
				}
			} while (!validInput);
			scanner.close();
		}
	}

	private void afterMessageGenerated(Message message, Boolean valid) {
		synchronized (this) {
			Process.messageID++;
			Process.inputQueue.add(message);
		}
		valid = true;
	}

	private int getHashingValue(int key) {
		return key % Process.numProc;
	}

	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}
}
