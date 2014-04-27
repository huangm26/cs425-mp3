package message;

import java.util.Date;

public class Get extends Message{

	public int level;
	
	public Get(int from, int to, int key, int messageID, int level) {
		super(from, to, key, messageID);
		this.level = level;
	}

}
