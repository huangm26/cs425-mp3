package message;

import java.io.Serializable;


public abstract class Message implements Serializable {
	
	public int from;
	public int to;
	public int timestamp;
	public int key;
	
	public Message(int from, int to, int timestamp, int key) {
		this.to = to;
		this.from = from;
		this.timestamp = timestamp;
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