package message;

import java.io.Serializable;


public abstract class Message implements Serializable {
	
	public int from;
	public int to;
	public String timeStamp;
	public int key;
	
	public Message(int from, int to, String timeStamp, int key) {
		this.to = to;
		this.from = from;
		this.timeStamp = timeStamp;
		this.key = key;
	}
	
	public boolean isDelete() {
		return this instanceof Delete;
	}

	public boolean isGet() {
		return this instanceof Get;
	}
	
	public boolean isInsert() {
		return this instanceof Insert;
	}
	
	public boolean isUpdate() {
		return this instanceof Update;
	}
}