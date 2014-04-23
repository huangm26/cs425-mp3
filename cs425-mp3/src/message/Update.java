package message;

public class Update extends Message{

	public String value;
	public int level;
	
	public Update(int from, int to, String timeStamp, int key, String value, int level) {
		super(from, to, timeStamp, key);
		// TODO Auto-generated constructor stub
		
		this.value = value;
		this.level = level;
	}

}
