/**
 * Bartleby Android
 * A project to enable public access to public building information.
 */
package net.caustic.android;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Use this class to check whether a server is available (is responding to HEAD on root path).
 * @author talos
 *
 */
class ServerPinger {
	/**
	 * How long to wait between pings.
	 */
	private static final int DELAY_MILLISECONDS = 4000;
	private final HttpClient client = new DefaultHttpClient();
	private final Runnable runnable;
	private final ExecutorService service = Executors.newSingleThreadExecutor();
	
	private Future<?> lastPing;
	
	/**
	 * 
	 */
	public ServerPinger(final String serverAddr, final ServerPingerListener listener) {

		this.runnable = new Runnable() {
			
			/**
			 * This will run in a loop until the listener tells it to stop.
			 */
			public void run() {
				boolean keepGoing = true;
				HttpHead head = new HttpHead(serverAddr);
				
				while(keepGoing) {
					boolean success = false;
					try {
						HttpResponse response = client.execute(head);
						int code = response.getStatusLine().getStatusCode();
						if(code >= 200 && code < 300) {
							success = true;
						}
						
					} catch(IOException e) {
						// equivalent to unsuccessful response.
					}
					
					if(success) {
						keepGoing = listener.onAlive();
					} else {
						keepGoing = listener.onDead();
					}
					
					try {
						Thread.sleep(DELAY_MILLISECONDS);
					} catch(InterruptedException e) {
						keepGoing = false;
					}
				}
			}
		};
	}
	
	/**
	 * Check to see whether the server is alive.  This will call back to the
	 * {@link ServerPingerListener} passed in the constructor.
	 */
	void ping() {
		if(lastPing != null) {
			lastPing.cancel(true);
		}
		lastPing = service.submit(runnable);
	}
	
	/**
	 * Stop the pinging.
	 */
	void stop() {
		if(lastPing != null) {
			lastPing.cancel(true);
		}
	}
}
