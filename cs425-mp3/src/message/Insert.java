package message;

public class Insert extends Message{
	
	public String value;
	public int level;
	
	public Insert(int from, int to, String timeStamp, int key, String value, int level) {
		super(from, to, timeStamp, key);
		// TODO Auto-generated constructor stub
		
		this.value = value;
		this.level = level;
	}

}
