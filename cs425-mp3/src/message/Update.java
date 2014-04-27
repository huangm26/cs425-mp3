package message;

import java.util.Date;

public class Update extends Message{

	public String value;
	public int level;
	public Date timeStamp;
	
	public Update(int from, int to, Date timeStamp, int key, int messageID, String value, int level) {
		super(from, to, key, messageID);
		
		this.value = value;
		this.level = level;
		this.timeStamp = timeStamp;
	}

}
