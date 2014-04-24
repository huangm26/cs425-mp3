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

import message.Get;
import message.Insert;
import message.Message;
import message.Update;

public class ProcessSend implements Runnable {

	@SuppressWarnings({ "unused" })
	public void send(Message message, int to) throws IOException, InterruptedException {

		Random rand = new Random();
		// Delay in range [0, 2*mean delay]
		// int randomDelay = rand.nextInt(2 * Process.delayTime + 1);

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
			// channel.connect(new
			// InetSocketAddress(InetAddress.getByName(Process.IP), destPort));
			int bytesend = channel.send(buffer, new InetSocketAddress(
					InetAddress.getByName(Process.IP), destPort));
			// int bytesend = channel.write(buffer);
			// channel.disconnect();
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
		// if there are messages in the queue, try to send them all
		System.out.println("starting send thread");
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (!Process.inputQueue.isEmpty()) {
				try {
					Message m = Process.inputQueue.poll();
					System.out.println("ready to send message");
					send(m, m.to);
					
					// send 2 replicas
					if (m.to == 0) {
						// special case: for server_0, send replicas to server_1
						// and server_numproc-1
						send(m,m.to+1);
						send(m,Process.numProc -1 );
					} else if(m.to == Process.numProc -1){	
						send(m, 0);
						send(m, m.to-1);
					}	else
					{
						send(m,m.to+1);
						send(m,m.to-1);
					}
					wait_for_ack(m);
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	
	private void wait_for_ack(Message m)
	{
		if(m.isInsert())
		{
			//level one case
			if(((Insert)m).level == 0)
			{
				boolean sentinel = true;
				while(sentinel)
				{
					for(int i = 0; i < 3; i++)
					{
						if(Process.ack[m.messageID][i])
						{
							System.out.println("Level one insert successful");
							sentinel = false;
							break;
						}
					}
				}
			}	
			//level all case
			else if(((Insert)m).level == 9)
			{
				boolean sentinel = true;
				while(sentinel)
				{
					boolean temp = true;
					for(int i = 0; i < 3; i++)
					{
						temp = (temp && Process.ack[m.messageID][i]);
						
					}
					if(temp)
					{
						System.out.println("Level All insert successful");
						break;
					}
				}
			}
		}	else if(m.isUpdate())
		{
			//level one case
			if(((Update)m).level == 0)
			{
				boolean sentinel = true;
				while(sentinel)
				{
					for(int i = 0; i < 3; i++)
					{
						if(Process.ack[m.messageID][i])
						{
							System.out.println("Level one update successful");
							sentinel = false;
							break;
						}
					}
				}
			}	
			//level all case
			else if(((Update)m).level == 9)
			{
				boolean sentinel = true;
				while(sentinel)
				{
					boolean temp = true;
					for(int i = 0; i < 3; i++)
					{
						temp = (temp && Process.ack[m.messageID][i]);
						
					}
					if(temp)
					{
						System.out.println("Level All update successful");
						break;
					}
				
				}
			}
		}	else if(m.isGet())
		{
			//level one case
			if(((Get)m).level == 0)
			{
				boolean sentinel = true;
				while(sentinel)
				{
					for(int i = 0; i < 3; i++)
					{
						if(Process.ack[m.messageID][i])
						{
							System.out.println("Level one get successful");
							sentinel = false;
							break;
						}
					}
				}
			}	
			//level all case
			else if(((Get)m).level == 9)
			{
				boolean sentinel = true;
				while(sentinel)
				{
					boolean temp = true;
					for(int i = 0; i < 3; i++)
					{
						temp = (temp && Process.ack[m.messageID][i]);
						
					}
					if(temp)
					{
						System.out.println("Level All update successful");
						break;
					}
				
				}
			}
		}
	}

}
