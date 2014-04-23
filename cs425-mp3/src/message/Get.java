package message;

public class Get extends Message{

	public int level;
	
	public Get(int from, int to, String timeStamp, int key, int level) {
		super(from, to, timeStamp, key);
		this.level = level;
	}

}
