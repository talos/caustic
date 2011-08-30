package net.microscraper.impl.commandline;

import static net.microscraper.impl.commandline.Arguments.LOG_STDOUT;
import static net.microscraper.impl.commandline.Arguments.LOG_TO_FILE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
	public static void main (String[] stringArgs) {
		try {
			Arguments arguments = new Arguments(stringArgs);
			
			// Extract implementations from arguments
			Deserializer deserializer = arguments.getDeserializer();
			Database database = arguments.getDatabase();
			String executionDir = arguments.getExecutionDir();
			List<Logger> loggers = arguments.getLoggers();
			try {
				
				database.open();
				Microscraper scraper = new Microscraper(deserializer, database, executionDir);				
				
				Iterator<Logger> iter = loggers.iterator();
				while(iter.hasNext()) {
					Logger logger = iter.next();
					logger.open();
					scraper.register(logger);
				}
				for (Logger logger : loggers) {
					
				}
			} finally {
				database.close();
			}
		} catch(UnsupportedEncodingException e) {
			System.out.println("Your computer does not support the required encoding: " + e.getMessage());
		} catch(ArgumentsException e) {
			System.out.println(e.getMessage());
			System.out.println(Arguments.USAGE);
		}
	}
	
	private static Scraper getScraper(Arguments args) {	
		try {
			
			// Register logs.
			if(args.has(LOG_TO_FILE)) {
				Logger logger = new JavaIOFileLogger(new File(args.get(LOG_TO_FILE)));
				logger.open();
				scraper.register(logger);
			}
			if(args.has(LOG_STDOUT)) {
				scraper.register(new SystemOutLogger());
			}
			return scraper;
		} catch(IllegalArgumentException e) {
			print(e.getMessage());
			print(Arguments.USAGE);
		} catch(FileNotFoundException e) {
			print("Could not find the input file: " + e.getMessage());
		} catch(SQLConnectionException e) {
			print("Could not open connection to SQL: " + e.getMessage());
		} catch(UnsupportedEncodingException e) {
			print("Unsupported encoding: " + e.getMessage());
		} catch(IOException e) {
			print("Could not open log file: " + e.getMessage());
		} 
		return null;
	}

	/*
	@SuppressWarnings("unchecked")
	public void scrape() throws IOException, InterruptedException, DeserializationException, MalformedUriException { 		
		// Run (handle inputs)
		if(args.has(INPUT)) {
			char inputColumnDelimiter;
			String delim = args.get(INPUT_COLUMN_DELIMITER);
			if(delim.length() > 1) {
				throw new IllegalArgumentException(INPUT_COLUMN_DELIMITER + " must be a single character.");
			}
			inputColumnDelimiter = delim.charAt(0);
			CSVReader input = new CSVReader(new FileReader(args.get(INPUT)), inputColumnDelimiter);
			
			String[] headers = input.readNext();
			String[] values;
			while((values = input.readNext()) != null) {
				Hashtable<String, String> lineDefaults = new Hashtable<String, String>();
				for(int i = 0 ; i < values.length ; i ++) {
					lineDefaults.put(headers[i], values[i]);
				}
				scrape(HashtableUtils.combine(new Hashtable[] { defaults, lineDefaults }));
			}
		} else {
			scrape(defaults);
		}
	}
	
	private void scrape(Hashtable<String, String> defaults) throws InterruptedException, IOException, DeserializationException, MalformedUriException {
		scraper.scrape(args.get(INSTRUCTION), defaults);
	}*/
}