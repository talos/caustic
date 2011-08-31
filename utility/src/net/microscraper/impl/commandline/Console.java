package net.microscraper.impl.commandline;

import static net.microscraper.impl.commandline.Arguments.LOG_STDOUT;
import static net.microscraper.impl.commandline.Arguments.LOG_TO_FILE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.microscraper.client.DeserializationException;
import net.microscraper.client.Deserializer;
import net.microscraper.client.Microscraper;
import net.microscraper.database.Database;
import net.microscraper.database.SQLConnectionException;
import net.microscraper.log.JavaIOFileLogger;
import net.microscraper.log.Loggable;
import net.microscraper.log.Logger;
import net.microscraper.log.SystemOutLogger;
import net.microscraper.uri.MalformedUriException;

/**
 * Main class for the Utility.
 * @author realest
 *
 */
public class Console {
	private static void openLoggers(List<Logger> loggers) {
		for(Logger logger : loggers) {
			try {
				logger.open();
			} catch (IOException e) {
				System.out.println("Could not open logger " + logger + ": " + e.getMessage());
			}
		}
	}
	
	private static void closeLoggers(List<Logger> loggers) {
		for(Logger logger : loggers) {
			try {
				logger.close();
			} catch (IOException e) {
				System.out.println("Could not close logger " + logger + ": " + e.getMessage());
			}
		}
	}
	
	public static void main (String[] stringArgs) {
		try {
			Arguments arguments = new Arguments(stringArgs);
			
			// Extract implementations from arguments
			Deserializer deserializer = arguments.getDeserializer();
			Database database = arguments.getDatabase();
			String executionDir = arguments.getExecutionDir();
			List<Logger> loggers = arguments.getLoggers();
			String instructionSerialized = arguments.getInstruction();
			Hashtable<String, String> defaults = arguments.getDefaults();
			try {
				
				database.open();
				Microscraper scraper = new Microscraper(deserializer, database, executionDir);				
				
				openLoggers(loggers);
				for(Logger logger : loggers) {
					scraper.register(logger);
				}
				
				scraper.scrape(instructionSerialized, defaults);
			} catch(IOException e) {
				// could not open database
			} finally {
				closeLoggers(loggers);
				try {
					database.close();
				} catch(IOException e) {
					System.out.println("Could not close database: " + e.getMessage());
				}
			}
		} catch(UnsupportedEncodingException e) {
			System.out.println("Your computer does not support the required encoding: " + e.getMessage());
		} catch(ArgumentsException e) {
			System.out.println(e.getMessage());
			System.out.println(Arguments.USAGE);
		}
	}
}