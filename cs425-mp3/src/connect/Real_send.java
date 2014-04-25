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

public class Real_send implements Runnable{

	Message message;
	int to;
	int delayTime;
	
	public Real_send(Message message, int to, int delayTime)
	{
		this.message = message;
		this.to = to;
		this.delayTime = delayTime;
	}
	
	public void send(Message message, int to, int delayTime) throws IOException, InterruptedException {

		Random rand = new Random();
		//Delay in range [0, 2*mean delay]
		int randomDelay = rand.nextInt(2 * delayTime + 1);

		DatagramChannel channel;
		channel = DatagramChannel.open();
		int destPort = 6000 + to;
		try {
			InetSocketAddress destAddress = new InetSocketAddress(
					InetAddress.getByName(Process.IP), destPort);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(message);
			byte[] data = outputStream.toByteArray();
			ByteBuffer buffer = ByteBuffer.wrap(data);
			//delay for delayTime
			Thread.sleep(randomDelay);
			int bytesend = channel.send(buffer, new InetSocketAddress(
					InetAddress.getByName(Process.IP), destPort));
//			System.out.println(String.format("send %d bytes from %d to %d",
//					bytesend, message.from, message.to));
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
		try {
			send(message, to, delayTime);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
