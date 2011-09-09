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
 * Main class for the Utility.
 * @author realest
 *
 */
public class Console {
	
	/**
	 * 
	 * @param objToPrint The object to print to the console.
	 */
	private static void println(Object objToPrint) {
		System.out.print(objToPrint.toString());
		println();
	}
	
	/**
	 * Print blank line to console.
	 */
	private static void println() {
		System.out.println();
	}
	
	private static Logger logger;
	private static Database database;
	private static Input input;

	private static Instruction instruction;
	private static String source;
	
	private static ScraperRunner runner;
	
	//private static ExecutorService executor;
	//private static List<Future<ScraperResult>> futures =
	//		Collections.synchronizedList(new ArrayList<Future<ScraperResult>>());
	//private static final List<Execution> executions = new ArrayList<Execution>();

	/**
	 * This thread is registered with {@link Runtime#getRuntime()} shutdown
	 * hook, and cleans up everything that could be open, while also
	 * giving a readout of what happened.
	 *
	 */
	public static final Thread shutdownThread = new Thread() {
		public void run() {
			
			
			//println(statusLine(successful, stuck, failed));
			
			try {
				logger.close();
			} catch (IOException e) {
				println("Could not close logger " + StringUtils.quote(logger) + ": " + e.getMessage());
			}
			
			try {
				input.close();
			} catch(IOException e) {
				println("Could not close input " + StringUtils.quote(input) + ": " + e.getMessage());
			}
			try {
				database.close();
			} catch(IOException e) {
				println("Could not close database " + StringUtils.quote(database) + ": " + e.getMessage());
			}
		}
	};
	
	/**
	 * 
	 */
	public static final Thread inputThread = new Thread() {
		@Override
		public void run() {
			DatabaseView view;
			try {
				while((view = input.next(database)) != null) {
					runner.submit(new Scraper(
							instruction, view,
							source));
				}
			} catch(IOException e) {
				logger.i("Terminated input");
				logger.e(e);
			}
		}
	};
	
	public static void main (String... stringArgs) {
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		try {
			ConsoleOptions options = new ConsoleOptions(stringArgs);
			database = options.getDatabase();
			logger = options.getLogger();
			input = options.getInput();
			
			instruction = options.getInstruction();
			source = options.getSource();
			
			runner = options.getScraperRunner();
			
		} catch(InvalidOptionException e) {
			println(e.getMessage());
			println();
			println(ConsoleOptions.USAGE);
			return;
		} catch(UnsupportedEncodingException e) {
			println("Your computer does not support the required encoding: " + e.getMessage());
			return;
		}
		
		Runtime.getRuntime().addShutdownHook(shutdownThread);
		try {
			logger.open();
			database.open();
			input.open();
		} catch(IOException e) {
			println("Could not open necessary file: " + e.getMessage());
			return;
		}
		
		// Start to read input.
		inputThread.start();
		
		try {
			inputThread.join(); 
		} catch(InterruptedException e) {
			println("Interrupted input reading.");
		}
		
		try {
			runner.await(); // orderly shutdown
		} catch (InterruptedException e) {
			println("Interrupted scraping.");
		}
	}

	/**
	 * Generate a readout of the relative success of the {@link Scraper}s' executions.
	 * @param successful the number of successful executions.
	 * @param stuck the number of executions that got stuck.
	 * @param failed the number of failed executions.
	 * @return A {@link String} summary.
	 */
	public static String statusLine(int successful, int stuck, int failed) {
		return successful + " successful executions, " + stuck + " stuck executions, " + failed +
				" failed executions.";
	}
	
}