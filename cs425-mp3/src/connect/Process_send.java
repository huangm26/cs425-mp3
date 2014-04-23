package connect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Random;

import message.Message;



public class Process_send implements Runnable {

	@SuppressWarnings({ "unused", "unused" })
	public void send(int destID, Message message)
			throws IOException, InterruptedException {
		
			Random rand = new Random();
			// Delay in range [0, 2*mean delay]
			int randomDelay = rand.nextInt(2 * Process.delayTime + 1);
		
			DatagramChannel channel;
			channel = DatagramChannel.open();
			int destPort = 6000 + destID;
			try {
					InetSocketAddress destAddress = new InetSocketAddress(
								InetAddress.getByName(Process.IP), destPort);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ObjectOutputStream os = new ObjectOutputStream(outputStream);
					os.writeObject(message);
					byte[] data = outputStream.toByteArray();
					ByteBuffer buffer = ByteBuffer.wrap(data);				
//					channel.connect(new InetSocketAddress(InetAddress.getByName(Process.IP), destPort));
					int bytesend = channel.send(buffer, new InetSocketAddress(InetAddress.getByName(Process.IP), destPort));
//					int bytesend = channel.write(buffer);
//					channel.disconnect();
					System.out.println("send "+ bytesend + " bytes");
					channel.close();
					
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
