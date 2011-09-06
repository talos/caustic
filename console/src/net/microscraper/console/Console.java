package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.microscraper.client.Deserializer;
import net.microscraper.client.Scraper;
import net.microscraper.database.Database;
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
	
	public static void main (String... stringArgs) {
		Logger logger;
		Database database;
		Input input;

		Deserializer deserializer;
		String instructionSerialized;
		String executionDir;
		String source;
		
		ExecutorService executor;
		List<Execution> executions = new ArrayList<Execution>();
		List<ExecutionException> exceptions = new ArrayList<ExecutionException>();
		
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
		
		// This opens a block where files are opened, and more unexpected failures
		// could happen.
		try {
			// Register a shutdown hook that closes everything up.
			Runtime.getRuntime().addShutdownHook(
					new ShutdownThread(executions, exceptions, logger, input, database));
			
			logger.open();
			database.open();
			input.open();
			
			Map<String, String> inputRow;
			List<Future<Scraper>> results = new ArrayList<Future<Scraper>>();
			while((inputRow = input.next()) != null) {
				Scraper scraper = new Scraper(
						instructionSerialized,
						deserializer,
						executionDir,
						database,
						new Hashtable<String, String>(inputRow),
						source);
				scraper.register(logger);
				results.add(executor.submit(scraper, scraper));
				if(Thread.interrupted()) {
					executor.shutdownNow();
					throw new InterruptedException();
				}
			}
			
			while(results.size() > 0) {
				Iterator<Future<Scraper>> iter = results.iterator();
				while(iter.hasNext()) {
					if(Thread.interrupted()) {
						executor.shutdownNow();
						throw new InterruptedException();
					}
					Future<Scraper> future = iter.next();
					if(future.isDone()) {
						try {
							Scraper scraper = future.get();
							executions.addAll(Arrays.asList(scraper.getExecutions()));
						} catch(ExecutionException e) {
							logger.e(e.getCause());
							exceptions.add(e);
							println("Skipped instruction: " + e.toString());
							e.printStackTrace();
						}
						iter.remove();
					}
				}
			}
			
			executor.shutdown();
		} catch(InterruptedException e) {
			println("Interrupted");
		} catch(IOException e) {
			println("IO exception: " + e.getMessage());
		} catch(RuntimeException e) {
			e.printStackTrace(); // catch & log uncaught exceptions.
		} catch(Error e) {
			e.printStackTrace(); // catch & log uncaught exceptions.
		}
	}
	
	/**
	 * This thread is registered with {@link Runtime#getRuntime()} shutdown
	 * hook, and cleans up everything that could be open, while also
	 * giving a readout of what happened.
	 * @author talos
	 *
	 */
	private static class ShutdownThread extends Thread {
		private final List<Execution> executions;
		private final List<ExecutionException> exceptions;
		private final Logger logger;
		private final Input input;
		private final Database database;
		
		public ShutdownThread(List<Execution> executions,
				List<ExecutionException> exceptions, Logger logger,
				Input input, Database database) {
			this.executions = executions;
			this.exceptions = exceptions;
			this.logger = logger;
			this.input = input;
			this.database = database;
		}
		
		public void run() {
			
			// Readout of success/stuck/failure over course of executions.
			int successful = 0, stuck = 0, failed = 0;
			for(Execution execution : executions) {
				if(execution.isSuccessful()) {
					successful++;
				} else if(execution.isMissingVariables()) {
					stuck++;
				} else if(execution.hasFailed()) {
					failed++;
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
	}
}