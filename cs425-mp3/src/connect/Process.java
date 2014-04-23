package connect;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

import util.Configuration;

public class Process {
	public static int ID; // use id to link to port
	public static String IP;
	public static int myPort;
	public static int messageID;
	public static DatagramChannel mychannel;
	
	
	
	
	
	public static void main(String args[]) throws IOException
	{
		// User determine id
				Scanner scanner = new Scanner(System.in);
				do {
					System.out.println("Enter the ID starting from 0 (to 5): ");
					ID = scanner.nextInt();
				} while (ID < 0 || ID > 5);

				// Get configuration values
				Configuration.getInstance();
				IP = Configuration.IP[ID];
		
				//setup socket channel using UDP
				myPort = ID + 6000; // define every process's port by the ID
				mychannel = DatagramChannel.open();
				System.out.println(myPort);

	}
}
