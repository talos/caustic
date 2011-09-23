package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.microscraper.client.Executor;
import net.microscraper.client.Scraper;
import net.microscraper.database.ConnectionException;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionResult;
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
	private final BlockingQueue<Scraper> queue;
	
	public Console(String... stringArgs) throws InvalidOptionException, UnsupportedEncodingException {
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		ConsoleOptions options = new ConsoleOptions(stringArgs);
		database = options.getDatabase();
		logger = options.getLogger();
		input = options.getInput();
		browser = options.getBrowser();
		
		threadsPerRow = options.getThreadsPerRow();
		queue = new ArrayBlockingQueue<Scraper>(options.getNumRowsToRead());
		//executor = options.getExecutor();
		//executor = new AsyncThreadExecutor();
		
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
	
	public void execute() throws IOException, InterruptedException, DatabaseException {
		
		// Start to read input.
		Map<String, String> inputMap;
		while((inputMap = input.next()) != null) {
			DatabaseView view = database.newView();
			for(Map.Entry<String, String> entry : inputMap.entrySet()) {
				view.put(entry.getKey(), entry.getValue());
			}
			
			Scraper scraper = new Scraper(instruction, view,
					source, browser.copy(), threadsPerRow);
			
			if(queue.remainingCapacity() == 0) {
				Scraper scraperToFinish = queue.poll(); // wait for spot to become available
				scraperToFinish.join();
				logger.i("Finished " + scraperToFinish);
			}
			queue.put(scraper);
			scraper.scrape();
			logger.i("Scraping " + scraper);
		}
		
		try {
			input.close();
			logger.i("Closed input " + StringUtils.quote(input));
		} catch(IOException e) {
			System.out.println("Could not close input " + StringUtils.quote(input) + ": " + e.getMessage());
		}
		
		while(queue.peek() != null) {
			Scraper scraper = queue.poll();
			scraper.join();
			logger.i("Finished " + scraper);

		}
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
				for(Scraper scraper : queue) {
					scraper.interrupt();
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