package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import net.microscraper.client.Deserializer;
import net.microscraper.client.Scraper;
import net.microscraper.database.Database;
import net.microscraper.log.Logger;
import net.microscraper.log.MultiLog;
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
		System.out.println(objToPrint);
	}
	
	public static void main (String[] stringArgs) {
		Logger logger;
		Database database;
		Input input;

		Deserializer deserializer;
		String instructionSerialized;
		String executionDir;
		String source;
		
		Executor executor;
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		try {
			ConsoleOptions options = new ConsoleOptions(stringArgs);
			database = options.getDatabase();
			logger = options.getLogger();
			input = options.getInput();

			deserializer = options.getDeserializer();
			executionDir = options.getExecutionDir();
			instructionSerialized = options.getInstruction();
			source = options.getSource();
			
			executor = options.getExecutor();
		} catch(InvalidOptionException e) {
			println(e.getMessage());
			println("");
			println(ConsoleOptions.USAGE);
			return;
		} catch(UnsupportedEncodingException e) {
			println("Your computer does not support the required encoding: " + e.getMessage());
			return;
		}
		
		// Open up and register loggers.
		try {
			logger.open();
		} catch (IOException e) {
			println("Could not open logger " + StringUtils.quote(logger) + ": " + e.getMessage());
		}
		
		// This opens a block that must be able to fail while ensuring that
		// everything is closed.
		try {
			database.open();
			input.open();
			
			Hashtable<String, String> inputRow;
			
			List<Scraper> runningScrapers = new ArrayList<Scraper>();
			List<Scraper> doneScrapers = new ArrayList<Scraper>();
			while((inputRow = input.next()) != null) {
				Scraper scraper = new Scraper(instructionSerialized, deserializer, executionDir,
						database, inputRow, source);
				System.out.println(inputRow);
				scraper.register(logger);
				executor.execute(scraper);
				runningScrapers.add(scraper);
			}
			
			while(runningScrapers.size() > 0) {
				System.out.println(runningScrapers.size());
				Iterator<Scraper> iter = runningScrapers.iterator();
				while(iter.hasNext()) {
					Scraper scraper = iter.next();
					if(scraper.hasBeenRun()) {
						doneScrapers.add(scraper);
						iter.remove();
					}
				}
			}
			
			for(Scraper scraper : doneScrapers) {
				Execution[] executions = scraper.getExecutions();
				for(Execution execution : executions) {
					println(execution.isSuccessful());
					println(execution.isMissingVariables());
					if(execution.hasFailed()) {
						println(execution.failedBecause()[0]);
					}
				}
			}
		} catch(IOException e) {
			println("IO exception: " + e.getMessage());
		} catch(RuntimeException e) {
			e.printStackTrace(); // catch & log uncaught exceptions.
		} catch(Error e) {
			e.printStackTrace(); // catch & log uncaught exceptions.
		} finally {
			// Finally block, try to close everything.
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