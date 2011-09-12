package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.microscraper.client.Scraper;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseView;
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
	
	private final ScraperExecutor executor;

	public Console(String... stringArgs) throws InvalidOptionException, UnsupportedEncodingException {
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		ConsoleOptions options = new ConsoleOptions(stringArgs);
		database = options.getDatabase();
		logger = options.getLogger();
		input = options.getInput();
		
		instruction = options.getInstruction();
		source = options.getSource();
		
		executor = options.getExecutor();
	}
	
	public String execute() throws IOException  {
		logger.open();
		database.open();
		input.open();
		
		// Start to read input.
		DatabaseView view;
		while((view = input.next(database)) != null) {
			Scraper scraper = new Scraper(instruction, view, source);
			CallableScraper cScraper = new CallableScraper(scraper, executor, logger);
			executor.submit(cScraper);
		}
		
		// wait for executor to finish
		try {
			executor.join();
		} catch(InterruptedException e) {
			executor.kill();
		}
		return executor.getStatusLine();
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
				} catch(IOException e) {
					System.out.println("Could not close database " + StringUtils.quote(database) + ": " + e.getMessage());
				}
			}
		};
	}
}