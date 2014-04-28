package connect;

import message.Get;
import message.Insert;
import message.Message;
import message.Update;

public class ProcessSend implements Runnable {

	@Override
	public void run() {
		// if there are messages in the queue, try to send them all
		System.out.println("starting send thread");
		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (!Process.inputQueue.isEmpty()) {
				Message m = Process.inputQueue.poll();
				System.out.println("ready to send message");
				Real_send send1 = new Real_send(m, m.to, Process.avgDelayTo2);
				new Thread(send1).start();

				// send 2 replicas
				if (m.to == 0) {
					// special case: for server_0, send replicas to server_1
					// and server_numproc-1
					Real_send send2 = new Real_send(m, m.to + 1,
							Process.avgDelayTo3);
					new Thread(send2).start();
					Real_send send3 = new Real_send(m, Process.numProc - 1,
							Process.avgDelayTo1);
					new Thread(send3).start();

				} else if (m.to == Process.numProc - 1) {
					Real_send send2 = new Real_send(m, 0, Process.avgDelayTo3);
					new Thread(send2).start();
					Real_send send3 = new Real_send(m, m.to - 1,
							Process.avgDelayTo1);
					new Thread(send3).start();
				} else {
					Real_send send2 = new Real_send(m, m.to + 1,
							Process.avgDelayTo3);
					new Thread(send2).start();
					Real_send send3 = new Real_send(m, m.to - 1,
							Process.avgDelayTo1);
					new Thread(send3).start();
				}
				wait_for_ack(m);
			}
		}
	}

	private void wait_for_ack(Message m) {
		if (m.isInsert()) {
			// level one case
			if (((Insert) m).level == 1) {
				boolean sentinel = true;
				while (sentinel) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// special case when destionation is P0
					if (m.to == 0) {
						if (Process.ack[m.messageID][Process.numProc - 1]) {
							System.out.println("Level ONE insert successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][0]) {
							System.out.println("Level ONE insert successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][1]) {
							System.out.println("Level ONE insert successful");
							sentinel = false;
							break;
						}

					} else if (m.to == Process.numProc - 1) {
						// special case when destination is the last Process
						if (Process.ack[m.messageID][0]) {
							System.out.println("Level ONE insert successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][Process.numProc - 1]) {
							System.out.println("Level ONE insert successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][Process.numProc - 2]) {
							System.out.println("Level ONE insert successful");
							sentinel = false;
							break;
						}
					} else {
						for (int i = -1; i < 2; i++) {
							if (Process.ack[m.messageID][m.to + i]) {
								System.out
										.println("Level ONE insert successful");
								sentinel = false;
								break;
							}
						}
					}

				}
			}
			// level all case
			else if (((Insert) m).level == 9) {
				boolean sentinel = true;
				while (sentinel) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					boolean temp = true;

					// special case when destionation is P0
					if (m.to == 0) {
						temp = (temp && Process.ack[m.messageID][Process.numProc - 1]);
						temp = (temp && Process.ack[m.messageID][0]);
						temp = (temp && Process.ack[m.messageID][1]);
						if (temp) {
							System.out.println("Level ALL insert successful");
							break;
						}

					} else if (m.to == Process.numProc - 1) {
						// special case when destination is the last Process
						temp = (temp && Process.ack[m.messageID][0]);
						temp = (temp && Process.ack[m.messageID][Process.numProc - 1]);
						temp = (temp && Process.ack[m.messageID][Process.numProc - 2]);
						if (temp) {
							System.out.println("Level ALL insert successful");
							break;
						}

					} else {
						for (int i = -1; i < 2; i++) {
							temp = (temp && Process.ack[m.messageID][m.to + i]);
						}
						if (temp) {
							System.out.println("Level ALL insert successful");
							break;
						}
					}
				}

			}
		} else if (m.isUpdate()) {
			// level one case
			if (((Update) m).level == 1) {
				boolean sentinel = true;
				while (sentinel) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// special case when destionation is P0
					if (m.to == 0) {
						if (Process.ack[m.messageID][Process.numProc - 1]) {
							System.out.println("Level ONE update successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][0]) {
							System.out.println("Level ONE update successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][1]) {
							System.out.println("Level ONE update successful");
							sentinel = false;
							break;
						}

					} else if (m.to == Process.numProc - 1) {
						// special case when destination is the last Process
						if (Process.ack[m.messageID][0]) {
							System.out.println("Level ONE update successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][Process.numProc - 1]) {
							System.out.println("Level ONE update successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][Process.numProc - 2]) {
							System.out.println("Level ONE update successful");
							sentinel = false;
							break;
						}
					} else {
						for (int i = -1; i < 2; i++) {
							if (Process.ack[m.messageID][m.to + i]) {
								System.out
										.println("Level ONE update successful");
								sentinel = false;
								break;
							}
						}
					}

				}
			}
			// level all case
			else if (((Update) m).level == 9) {
				boolean sentinel = true;
				while (sentinel) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					boolean temp = true;

					// special case when destionation is P0
					if (m.to == 0) {
						temp = (temp && Process.ack[m.messageID][Process.numProc - 1]);
						temp = (temp && Process.ack[m.messageID][0]);
						temp = (temp && Process.ack[m.messageID][1]);
						if (temp) {
							System.out.println("Level ALL update successful");
							break;
						}
					} else if (m.to == Process.numProc - 1) {
						// special case when destination is the last Process
						temp = (temp && Process.ack[m.messageID][0]);
						temp = (temp && Process.ack[m.messageID][Process.numProc - 1]);
						temp = (temp && Process.ack[m.messageID][Process.numProc - 2]);
						if (temp) {
							System.out.println("Level ALL update successful");
							break;
						}

					} else {
						for (int i = -1; i < 2; i++) {
							temp = (temp && Process.ack[m.messageID][m.to + i]);
						}
						if (temp) {
							System.out.println("Level ALL update successful");
							break;
						}
					}
				}
			}

		} else if (m.isGet()) {
			System.out.println("IS a get");
			// level one case
			if (((Get) m).level == 1) {
				boolean sentinel = true;
				while (sentinel) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					// special case when destionation is P0
					if (m.to == 0) {
						if (Process.ack[m.messageID][Process.numProc - 1]) {
							System.out.println("Level ONE get successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][0]) {
							System.out.println("Level ONE get successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][1]) {
							System.out.println("Level ONE get successful");
							sentinel = false;
							break;
						}

					} else if (m.to == Process.numProc - 1) {
						// special case when destination is the last Process
						if (Process.ack[m.messageID][0]) {
							System.out.println("Level ONE get successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][Process.numProc - 1]) {
							System.out.println("Level ONE get successful");
							sentinel = false;
							break;
						}
						if (Process.ack[m.messageID][Process.numProc - 2]) {
							System.out.println("Level ONE get successful");
							sentinel = false;
							break;
						}
					} else {
						for (int i = -1; i < 2; i++) {
							if (Process.ack[m.messageID][m.to + i]) {
								System.out.println("Level ONE get successful");
								sentinel = false;
								break;
							}
						}
					}

				}
			}
			// level all case
			else if (((Get) m).level == 9) {
				boolean sentinel = true;
				while (sentinel) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					boolean temp = true;

					// special case when destionation is P0
					if (m.to == 0) {
						temp = (temp && Process.ack[m.messageID][Process.numProc - 1]);
						temp = (temp && Process.ack[m.messageID][0]);
						temp = (temp && Process.ack[m.messageID][1]);
						if (temp) {
							System.out.println("Level ALL get successful");
							break;
						}

					} else if (m.to == Process.numProc - 1) {
						// special case when destination is the last Process
						temp = (temp && Process.ack[m.messageID][0]);
						temp = (temp && Process.ack[m.messageID][Process.numProc - 1]);
						temp = (temp && Process.ack[m.messageID][Process.numProc - 2]);
						if (temp) {
							System.out.println("Level ALL get successful");
							break;
						}

					} else {
						for (int i = -1; i < 2; i++) {
							temp = (temp && Process.ack[m.messageID][m.to + i]);
						}
						if (temp) {
							System.out.println("Level ALL get successful");
							break;
						}
					}

				}
			}
		}
	}

}
