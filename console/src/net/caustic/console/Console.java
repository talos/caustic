package net.caustic.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;

import net.caustic.LogScraperListener;
import net.caustic.Scraper;
import net.caustic.ScraperListener;
import net.caustic.database.Connection;
import net.caustic.database.ConnectionException;
import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.log.Logger;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * @author realest
 *
 */
public class Console {
	
	private final Logger logger;
	private final Connection connection; // can be null.
	private final Database database;
	private final Input input;

	private final String instruction;
	private final Scraper scraper;
	private final ConsoleScraperListener listener = new ConsoleScraperListener();
	private final int numThreads;
		
	public Console(String... stringArgs) throws InvalidOptionException, UnsupportedEncodingException {
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		ConsoleOptions options = new ConsoleOptions(stringArgs);
		logger = options.getLogger();
		input = options.getInput();
		instruction = options.getInstruction();
		connection = options.getConnection();
		numThreads= options.getNumThreads();
		if(connection != null) {
			database = options.getSQLDatabase(connection);
		} else {
			database = options.getInMemoryDatabase();
		}
		//listener.register(logger);
		
		scraper = new Scraper(database, numThreads, listener);
		scraper.setAutoRun(true);
		scraper.register(logger);
	}
	
	public void open() throws IOException {
		input.open();
	}
	
	public void execute() throws IOException, InterruptedException, DatabaseException {
		
		// Start to read input.
		Map<String, String> inputMap;
		
		
		while((inputMap = input.next()) != null) {
			scraper.scrape(instruction,
					new Hashtable<String, String>(inputMap));
			
			// no point in reading more from input than we have threads
			while(listener.getNumberOfOpenScopes() > numThreads) {
				Thread.sleep(100);
			}
		}
		try {
			input.close();
			logger.i("Closed input " + StringUtils.quote(input));
		} catch(IOException e) {
			logger.i("Could not close input " + StringUtils.quote(input) + ": " + e.getMessage());
			logger.e(e);
		}
		
		// Wait for scraper to finish up
		while(listener.getNumberOfOpenScopes() > 0) {
			Thread.sleep(100);
		}
		
		if(connection != null) {
			connection.close();
		}
		
		// kill the scraper, it's done
		scraper.interrupt();
	}
	
	/**
	 * This thread is registered with {@link Runtime#getRuntime()} shutdown
	 * hook, and cleans up everything that could be open, while also
	 * giving a readout of what happened.
	 *
	 */
	public Thread getShutdownThread() {
		return new Thread() {
			public void run() { 
				scraper.interrupt();
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
	}
}