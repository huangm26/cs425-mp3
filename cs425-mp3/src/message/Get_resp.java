package message;

import java.util.Date;

public class Get_resp extends Message{

	public String content;
	public int level;
	
	public Get_resp(int from, int to, Date timeStamp, int key, int messageID, String content, int level) {
		super(from, to, timeStamp, key, messageID);
		// TODO Auto-generated constructor stub
		this.content = content;
		this.level = level;
	}

}
