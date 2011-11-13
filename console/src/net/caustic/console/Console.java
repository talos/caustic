package net.caustic.console;

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

import net.caustic.Scraper;
import net.caustic.database.ConnectionException;
import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.DatabaseView;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;
import net.caustic.log.Logger;
import net.caustic.util.StringUtils;

/**
 * @author realest
 *
 */
public class Console {
	
	private final Logger logger;
	private final Database database;
	private final Input input;

	private final String instruction;
	private final String source;
	private final Scraper scraper;
		
	public Console(String... stringArgs) throws InvalidOptionException, UnsupportedEncodingException {
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		ConsoleOptions options = new ConsoleOptions(stringArgs);
		logger = options.getLogger();
		input = options.getInput();
		instruction = options.getInstruction();
		database = options.getDatabase();
		source = options.getSource();
		scraper = options.getScraper();
	}
	
	public void open() throws IOException {
		try {
			logger.open();
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
			scraper.scrape(instruction, inputMap);
		}
		
		try {
			input.close();
			logger.i("Closed input " + StringUtils.quote(input));
		} catch(IOException e) {
			System.out.println("Could not close input " + StringUtils.quote(input) + ": " + e.getMessage());
		}
		scraper.join();
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