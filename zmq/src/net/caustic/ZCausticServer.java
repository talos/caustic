package net.caustic;

import net.caustic.Scraper;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.util.StringUtils;

import org.json.me.JSONException;
import org.zeromq.ZMQ;

/**
 * Receive and execute Caustic JSON templates received via a ZMQ socket.
 * Push the results back to the wire.
 * @author talos
 *
 */
public class ZCausticServer implements Runnable, Loggable {
	
	private final ZMQ.Socket socket;
	private final MultiLog log = new MultiLog();
	private final Scraper scraper;
	
	public ZCausticServer(ZMQ.Context context, int sockFlag) {
	    //  Prepare our context and socket
		//this.context = context;
		socket = context.socket(sockFlag);
		socket.bind ("tcp://*:5555");
		
		//byte[] request = socket.recv(0);
    	scraper = new DefaultScraper();

	}
	
	public void run() {
		while(true) {

            //  Wait for next request from client
            String reqStr = new String(socket.recv (0));
            System.out.println ("Received request: [" + reqStr + "]");
            
            try {
            	Request request = Request.fromJSON(reqStr);
	            Response response = null;
	            
	            // Scrape away!
	            try {
	            	response = scraper.scrape(request);
	            	socket.send(response.serialize().getBytes(), 0);
	            } catch(InterruptedException e) {
	            	socket.send(Response.Failed(request, "Interrupted.", e.getMessage()).serialize().getBytes(), 0);
	            }
            } catch(JSONException e) {
            	e.printStackTrace();
            	socket.send(new String("Invalid Request: " + StringUtils.quote(reqStr)
            			+ " because of " + e.getMessage()).getBytes(), 0);
            }
        
		}
	}
	
	public void register(Logger logger) {
		this.log.register(logger);
	}
	
    public static void main(String[] args) {
    	ZCausticServer server = new ZCausticServer(ZMQ.context(1), ZMQ.REP);
        server.run();
    }
}
