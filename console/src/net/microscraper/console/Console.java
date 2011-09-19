package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.microscraper.database.ConnectionException;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;
import net.microscraper.log.Logger;
import net.microscraper.util.StringUtils;

/**
 * @author realest
 *
 */
public class Console {
	
	private final Logger logger;
	private final Database database;
	private final Input input;

	private final Instruction instruction;
	private final String source;
	private final HttpBrowser browser;
	
	private final int threadsPerRow;
	private final ExecutorService executor;
	
	public Console(String... stringArgs) throws InvalidOptionException, UnsupportedEncodingException {
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		ConsoleOptions options = new ConsoleOptions(stringArgs);
		database = options.getDatabase();
		logger = options.getLogger();
		input = options.getInput();
		browser = options.getBrowser();
		
		executor = Executors.newFixedThreadPool(options.getNumRowsToRead());
		threadsPerRow = options.getThreadsPerRow();
		
		instruction = options.getInstruction();
		source = options.getSource();
	}
	
	public void open() throws IOException {
		try {
			logger.open();
			database.open();
			input.open();
		} catch(ConnectionException e) {
			throw new IOException(e);
		} catch(DatabaseException e) {
			throw new IOException(e);
		}
	}
	
	public void execute() throws IOException, InterruptedException {
		
		// Start to read input.
		Map<String, String> inputMap;
		while((inputMap = input.next()) != null) {
			AsyncScraper scraper = new AsyncScraper(
					instruction, inputMap, database, source, browser.copy(), threadsPerRow);
			scraper.register(logger);
			executor.submit(scraper);
		}
		
		try {
			input.close();
			logger.i("Closed input " + StringUtils.quote(input));
		} catch(IOException e) {
			System.out.println("Could not close input " + StringUtils.quote(input) + ": " + e.getMessage());
		}
		
		executor.shutdown();
		executor.awaitTermination(100, TimeUnit.DAYS);
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
				executor.shutdownNow();
				try {
					executor.awaitTermination(10, TimeUnit.SECONDS);
				} catch(InterruptedException e) {
					System.out.println("Could not terminate all scrapers.");
				}
					
				try {
					logger.close();
				} catch (IOException e) {
					System.out.println("Could not close logger " + StringUtils.quote(logger) + ": " + e.getMessage());
				}
				
				try {
					input.close();
				} catch(IOException e) {
					System.out.println("Could not close input " + StringUtils.quote(input) + ": " + e.getMessage());
				}
				
				try {
					database.close();
					System.out.println("Saved to database " + database);
				} catch(ConnectionException e) {
					System.out.println("Could not close connection for database "
							+ StringUtils.quote(database) + ": " + e.getMessage());
				} catch(DatabaseException e) {
					System.out.println("Could not close database " + StringUtils.quote(database) + ": " + e.getMessage());					
				}
			}
		};
	}
}