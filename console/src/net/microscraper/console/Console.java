package net.microscraper.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.List;

import net.microscraper.client.Deserializer;
import net.microscraper.client.Scraper;
import net.microscraper.database.Database;
import net.microscraper.instruction.Instruction;
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
	 * @param strToPrint The strint to print to the console.
	 */
	private static void println(String strToPrint) {
		System.out.println(strToPrint);
	}
	
	public static void main (String[] stringArgs) {
		List<Logger> loggers;
		Database database;
		Input input;

		Deserializer deserializer;
		String instructionSerialized;
		String executionDir;
		String source;
		
		// Extract implementations from arguments, exit if there's a bad argument or this
		// system does not support the encoding.
		try {
			ConsoleOptions options = new ConsoleOptions(stringArgs);
			database = options.getDatabase();
			loggers = options.getLoggers();
			input = options.getInput();

			deserializer = options.getDeserializer();
			executionDir = options.getExecutionDir();
			instructionSerialized = options.getInstruction();
			source = options.getSource();
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
		for(Logger logger : loggers) {
			try {
				logger.open();
			} catch (IOException e) {
				println("Could not open logger " + StringUtils.quote(logger) + ": " + e.getMessage());
			}
		}
		
		// This opens a block that must be able to fail while ensuring that
		// everything is closed.
		try {
			database.open();
			input.open();
			
			Hashtable<String, String> inputRow;
			while((inputRow = input.next()) != null) {
				Scraper scraper = new Scraper(instructionSerialized, deserializer, executionDir,
						database, inputRow, source);
				for(Logger logger : loggers) {
					scraper.register(logger);
				}
				scraper.run();
				
			}
		} catch(IOException e) {
			println("IO exception while scraping: " + e.getMessage());
		} catch(RuntimeException e) {
			e.printStackTrace(); // catch & log uncaught exceptions.
		} catch(Error e) {
			e.printStackTrace(); // catch & log uncaught exceptions.
		} finally {
			// Finally block, try to close everything.
			for(Logger logger : loggers) {
				try {
					logger.close();
				} catch (IOException e) {
					println("Could not close logger " + StringUtils.quote(logger) + ": " + e.getMessage());
				}
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