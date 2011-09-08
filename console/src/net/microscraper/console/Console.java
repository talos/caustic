package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.microscraper.client.Scraper;
import net.microscraper.database.Database;
import net.microscraper.deserializer.Deserializer;
import net.microscraper.log.Logger;
import net.microscraper.util.Execution;
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

	private static Deserializer deserializer;
	private static String instructionSerialized;
	private static String executionDir;
	private static String source;
	
	private static ExecutorService executor;
	private static List<Future<Execution[]>> futures = new ArrayList<Future<Execution[]>>();
	//private static final List<Execution> executions = new ArrayList<Execution>();

	/**
	 * This thread is registered with {@link Runtime#getRuntime()} shutdown
	 * hook, and cleans up everything that could be open, while also
	 * giving a readout of what happened.
	 *
	 */
	public static final Thread shutdownThread = new Thread() {
		public void run() {
			
			// Readout of success/stuck/failure over course of executions.
			int successful = 0, stuck = 0, failed = 0;
			for(Future<Execution[]> future : futures) {
				try {
					Execution[] executions = future.get();
					for(Execution execution : executions) {
						if(execution.isSuccessful()) {
							successful++;
						} else if(execution.isMissingVariables()) {
							stuck++;
						} else if(execution.hasFailed()) {
							failed++;
						}
					}
				} catch(InterruptedException e) {
					
				} catch(ExecutionException e) {
					
				}
			}
			println(statusLine(successful, stuck, failed));
			
			// Readout of skipped instructions
			/*for(ExecutionException exception : exceptions) {
				
			}*/
			
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
			Map<String, String> inputRow;
			try {
				while((inputRow = input.next()) != null) {
					try {
						Scraper scraper = new Scraper(
								instructionSerialized,
								deserializer,
								executionDir,
								database,
								new Hashtable<String, String>(inputRow),
								source);
						scraper.register(logger);
						futures.add(executor.submit(new CallableScraper(scraper)));
					} catch(IOException e) {
						logger.i("Could not generate scraper for input " + inputRow.toString());
						logger.e(e);
					}
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
			
			deserializer = options.getDeserializer();
			deserializer.register(logger);
			executionDir = options.getExecutionDir();
			instructionSerialized = options.getInstruction();
			source = options.getSource();
			
			executor = options.getExecutor();
			
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
		
		executor.shutdown(); // orderly shutdown
		try {
			executor.awaitTermination(100, TimeUnit.DAYS);
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