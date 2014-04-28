package connect;

import java.util.Date;

import message.Get_resp;
import message.RepairRequest;

public class RepairThread implements Runnable {

	int from;
	int dest1, dest2, dest3;
	int key;
	String value;
	Date timeStamp;
	int repairID;

	public RepairThread(Get_resp gr) {
		from = Process.ID;
		key = gr.key;
		value = gr.value;
		timeStamp = gr.timeStamp;
		repairID = gr.messageID;
		dest2 = getHashingValue(key);
		if (dest2 == 0) {
			dest1 = Process.numProc - 1;
			dest3 = 1;
		} else if (dest2 == Process.numProc - 1) {
			dest1 = dest2 - 1;
			dest3 = 0;
		} else {
			dest1 = dest2 - 1;
			dest3 = dest2 + 1;
		}
	}

	@Override
	public void run() {
		// Prepare 3 repair requests
		RepairRequest rr1 = new RepairRequest(from, dest1, timeStamp, key,
				value, repairID);
		RepairRequest rr2 = new RepairRequest(from, dest2, timeStamp, key,
				value, repairID);
		RepairRequest rr3 = new RepairRequest(from, dest3, timeStamp, key,
				value, repairID);
		
		// Send them to different destination by defined average delay
		Real_send send1 = new Real_send(rr1, dest1, Process.avgDelayTo1);
		Real_send send2 = new Real_send(rr2, dest2, Process.avgDelayTo2);
		Real_send send3 = new Real_send(rr3, dest3, Process.avgDelayTo3);
		System.out
				.println(String
						.format("Sending repair request to %d %d %d with key=%d value=%s timeStamp=%s",
								dest1, dest2, dest3, key, value, timeStamp));
		new Thread(send1).start();
		new Thread(send2).start();
		new Thread(send3).start();
		
		// Waiting for acknowledgments
		// Close thread when all the acks have been received
		waitForAck();
	}

	private void waitForAck() {
		
	}
	
	private int getHashingValue(int key) {
		return key % Process.numProc;
	}

}
