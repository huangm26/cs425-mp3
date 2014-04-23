package message;


public class Message  {
	
	public int from;
	public int to;
	public int messageID;
	public String content;
	
	public Message(int from, int to, int messageID, String content) {
		this.to = to;
		this.from = from;
		this.messageID = messageID;
		this.content = content;
	}

}