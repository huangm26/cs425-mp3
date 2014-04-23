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

public class ProcessSend implements Runnable {

	@SuppressWarnings({ "unused" })
	public void send(Message message) throws IOException, InterruptedException {

		Random rand = new Random();
		// Delay in range [0, 2*mean delay]
		// int randomDelay = rand.nextInt(2 * Process.delayTime + 1);

		DatagramChannel channel;
		channel = DatagramChannel.open();
		int destPort = 6000 + message.to;
		try {
			InetSocketAddress destAddress = new InetSocketAddress(
					InetAddress.getByName(Process.IP), destPort);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(message);
			byte[] data = outputStream.toByteArray();
			ByteBuffer buffer = ByteBuffer.wrap(data);
			// channel.connect(new
			// InetSocketAddress(InetAddress.getByName(Process.IP), destPort));
			int bytesend = channel.send(buffer, new InetSocketAddress(
					InetAddress.getByName(Process.IP), destPort));
			// int bytesend = channel.write(buffer);
			// channel.disconnect();
			System.out.println(String.format("send %d bytes from %d to %d",
					bytesend, message.from, message.to));
			channel.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// if there are messages in the queue, try to send them all
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (!Process.inputQueue.isEmpty()) {
				try {
					Message m = Process.inputQueue.poll();
					send(m);
					
					// send 2 replicas
					if (m.to == 0) {
						// special case: for server_0, send replicas to server_1
						// and server_numproc-1
					} else {

					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
