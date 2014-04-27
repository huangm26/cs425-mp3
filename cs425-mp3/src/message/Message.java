package message;

import java.io.Serializable;
import java.util.Date;


public abstract class Message implements Serializable {
	
	public int from;
	public int to;
	public int key;
	public int messageID;
	
	public Message(int from, int to, int key, int messageID) {
		this.to = to;
		this.from = from;
		this.key = key;
		this.messageID = messageID;
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
	
	public boolean isInsert_ack() {
		return this instanceof Insert_ack;
	}
	
	public boolean isUpdate_ack() {
		return this instanceof Update_ack;
	}
	
	public boolean isGet_resp() {
		return this instanceof Get_resp;
	}
}