package message;

import java.util.Date;

public class Get_resp extends Message{

	public String value;
	public int level;
	public Date timeStamp;
	
	public Get_resp(int from, int to, Date timeStamp, int key, int messageID, String value, int level) {
		super(from, to, key, messageID);
		// TODO Auto-generated constructor stub
		this.value = value;
		this.level = level;
		this.timeStamp = timeStamp;
	}

}
