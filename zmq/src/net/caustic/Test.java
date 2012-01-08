package net.caustic;

import java.util.concurrent.atomic.AtomicInteger;

import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.zeromq.ZMQ;

public class Test {
	private static AtomicInteger cnt = new AtomicInteger();
    private static final String CLIENT_URL = "ipc://caustic.ipc";
	private static final ZMQ.Context ctx = ZMQ.context(1);

	public static void main(String[] args) {
		new Test();
	}
	
	private Test() {
		for(int i = 0 ; i < 4 ; i ++) {
			new Thread(new TestRunnable()).start();
		}
	}
	
	private class TestRunnable implements Runnable {
		private final int id = cnt.incrementAndGet();
		public void run() {
			try {
				ZMQ.Socket clientSocket = ctx.socket(ZMQ.REQ);
				clientSocket.connect(CLIENT_URL);
				clientSocket.send(
						/*new JSONObject()
							.put("id", id)
							.put("instruction", "http://www.accursedware.com:6767/property")
						.toString()*/
						new JSONObject()
							.put("id", id)
							.put("cookies", "{}")
							.put("force", true)
							.put("tags", new JSONObject()
								.put("Borough","3")
								.put("Street","Atlantic Ave")
								.put("Number","373")
								.put("Apt",""))
							.put("instruction","/acris-index-all-docs")
							.put("uri","http://www.accursedware.com:6767/property").toString().getBytes(), 0);
				System.out.println(new String(clientSocket.recv(0)));
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
