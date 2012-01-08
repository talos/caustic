package net.caustic;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.caustic.Scraper;
import net.caustic.log.Logger;
import net.caustic.log.SystemErrLogger;
import net.caustic.util.StringUtils;

import org.json.me.JSONException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQQueue;

/**
 * Receive and execute Caustic JSON templates received via a ZMQ socket.
 * Push the results back to the wire.
 * @author talos
 *
 */
public class ZCausticServer {

	private static final ZMQ.Context ctx = ZMQ.context(1);
	private static final int NUM_THREADS = 40;
    private static final String WORKER_URL = "inproc://workers";
    private static final String CLIENT_URL = "ipc://caustic.ipc";
	
    public static void main(String[] args) {
    	new ZCausticServer();
    }
	
	private final Scraper scraper = new DefaultScraper();
	private final Logger logger = new SystemErrLogger();
	private final ExecutorService service = Executors.newFixedThreadPool(NUM_THREADS);
	
	public ZCausticServer() {
		/*ZMQ.Socket test = ctx.socket(ZMQ.REP);
		test.bind(CLIENT_URL);
		while(true) {
			String recv = new String(test.recv(0));
			test.send(recv.getBytes(), 0);
		}*/
		
		//  Prepare our context and socket
		
		ZMQ.Socket clientSocket = ctx.socket(ZMQ.ROUTER);
		ZMQ.Socket workerSocket = ctx.socket(ZMQ.DEALER);
		
		clientSocket.bind(CLIENT_URL);
		workerSocket.bind(WORKER_URL);

		scraper.register(logger);
    	for(int i = 0 ; i < NUM_THREADS ; i ++) {
    		service.submit(new Worker());
    	}

		new ZMQQueue(ctx, clientSocket, workerSocket).run();
		service.shutdownNow();
	}
	
    private final class Worker implements Runnable {
    	private final String id = UUID.randomUUID().toString();
    	private final ZMQ.Socket socket = ctx.socket(ZMQ.REP);
    	
		@Override
		public void run() {
			socket.connect(WORKER_URL);
			while(true) {
	            //  Wait for next request from client
	            String reqStr = new String(socket.recv(0));
	            logger.i("Running request on worker " + id + " : "); //+ StringUtils.quote(reqStr));
				try {
	            	Request request = Request.fromJSON(reqStr);
		            
		            // Scrape away!
		            try {
		            	sendOut(scraper.scrape(request).serialize());
		            } catch(InterruptedException e) {
		            	//socket.send(new Response.Failed(request.id, request.uri, e.getMessage()).serialize().getBytes(), 0);
		            	sendOut(new Response.Failed(request.id, request.uri, e.getMessage()).serialize());
		            }
	            } catch(JSONException e) {
	            	e.printStackTrace();
	            	/*socket.send(new String("Invalid Request: " + StringUtils.quote(reqStr)
	            			+ " because of " + StringUtils.quote(e.getMessage())).getBytes(), 0);*/
	            	sendOut("Invalid Request: " + StringUtils.quote(reqStr)
	            			+ " because of " + StringUtils.quote(e.getMessage()));

	            }
			}
		}
		
		private void sendOut(String msg) {
			logger.i("Sending out from worker " + id);
			socket.send(msg.getBytes(), 0);
		}
    }
}
