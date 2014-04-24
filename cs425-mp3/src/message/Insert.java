package message;

import java.util.Date;

public class Insert extends Message{
	
	public String value;
	public int level;
	
	public Insert(int from, int to, Date timeStamp, int key, int messageID, String value, int level) {
		super(from, to, timeStamp, key, messageID);
		// TODO Auto-generated constructor stub
		
		this.value = value;
		this.level = level;
	}
	
}
