package connect;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import message.Message;;

public class ReadInput  implements Runnable{
		
		
	//this is the thread for reading input from stdin and put the inputted message into input_queue
	@Override
	public void run() {
		// TODO Auto-generated method stub

		System.out.println("Type input: ");
		while(true) {
			String content = null;
			String timestamp;
			Scanner scanner = new Scanner(System.in);
			content = scanner.nextLine();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			//get current date time with Date()
			Date date = new Date();
			content = content + " " + "From Process" + Process.ID + " MessageID " + Process.messageID;
			timestamp =  dateFormat.format(date);
			
			// if it's delete operation
			
			
			//if it's get operation
			
			//if it's insert operation
			
			//if it's update operation
			
			
			///////need to use this later
//			Process.input_queue.add(message);
		}
	}
}

