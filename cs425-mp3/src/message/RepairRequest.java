package message;

import java.util.Date;

public class RepairRequest extends Message {

	public String value;
	public Date timeStamp;
	
	public RepairRequest(int from, int to, Date timeStamp, int key, String value, int messageID) {
		super(from, to, key, messageID);
		// TODO Auto-generated constructor stub
		this.value = value;
		this.timeStamp = timeStamp;
	}

}
