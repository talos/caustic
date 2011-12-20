package net.caustic.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.caustic.DefaultScraper;
import net.caustic.Request;
import net.caustic.Response;
import net.caustic.Scraper;
import net.caustic.database.Connection;
import net.caustic.database.ConnectionException;
import net.caustic.log.Logger;
import net.caustic.util.CollectionStringMap;
import net.caustic.util.StringMap;
import net.caustic.util.StringUtils;

/**
 * @author realest
 *
 */
public class Console {
	
	private final Logger logger;
	//private final Connection connection; // can be null.
	private final Input input;

	private final String instruction;
	private final Scraper scraper;
		
	public Console(String... stringArgs) throws InvalidOptionException, UnsupportedEncodingException {
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		ConsoleOptions options = new ConsoleOptions(stringArgs);
		logger = options.getLogger();
		input = options.getInput();
		instruction = options.getInstruction();
		scraper = new DefaultScraper();
		/*connection = options.getConnection();
		if(connection != null) {
			database = options.getSQLDatabase(connection);
		} else {
			database = options.getInMemoryDatabase();
		}
		
		scraper = new Scraper(database, options.getNumThreads());
		scraper.register(logger);*/
	}
	
	public void open() throws IOException {
		input.open();
	}
	
	public void execute() throws IOException, InterruptedException {
		
		// Start to read input.
		Map<String, String> inputMap;
		
		while((inputMap = input.next()) != null) {

			// add initial request
			Request request = new Request(UUID.randomUUID().toString(), instruction,
					StringUtils.USER_DIR, null, new CollectionStringMap(inputMap), new String[] {}, true);
			makeRequest(request);
		}
		try {
			input.close();
			logger.i("Closed input " + StringUtils.quote(input));
		} catch(IOException e) {
			logger.i("Could not close input " + StringUtils.quote(input) + ": " + e.getMessage());
			logger.e(e);
		}
		//scraper.join();
		
		/*if(connection != null) {
			connection.close();
		}*/
	}
	
	private void makeRequest(Request request) throws InterruptedException {

		
		Response response = scraper.scrape(request);
		if(response.children != null) {
			
			// launch children
			for(String child : response.children) {
				
				// launch children from Find
				if(response.values != null) {
					
					for(String value : response.values) {
						makeRequest(new Request(UUID.randomUUID().toString(), child, 
								null, response.uri, request.tags,
								request.cookies, true));
						
					}
				} else if(response.content != null) { // launch children from Load

					makeRequest(new Request(UUID.randomUUID().toString(), child,
							response.uri,response.content, request.tags,
							response.cookies, true));
					
				}
			}
		} else if(response.missingTags != null) {
			// TODO
		} else if(response.failedBecause != null) {
			// TODO
		}
	}
	
	/**
	 * This thread is registered with {@link Runtime#getRuntime()} shutdown
	 * hook, and cleans up everything that could be open, while also
	 * giving a readout of what happened.
	 *
	 */
	/*public Thread getShutdownThread() {
		return new Thread() {
			public void run() { 
				if(!scraper.isIdle()) {
					scraper.interrupt();
				}
				try {
					input.close();
				} catch(IOException e) {
					logger.i("Could not close input " + StringUtils.quote(input) + ": " + e.getMessage());
					logger.e(e);
				}
				try {
					if(connection != null) {
						connection.close();
					}
				} catch(ConnectionException e) {
					logger.i("Couldn't close connection: " + e.getMessage());
					logger.e(e);
				}
			}
		};
	}*/
}