package message;

public class Update extends Message{

	String content;
	int level;
	
	public Update(int from, int to, int timestamp, int key, String content, int level) {
		super(from, to, timestamp, key);
		// TODO Auto-generated constructor stub
		
		this.content = content;
		this.level = level;
	}

}
