package net.microscraper.impl.commandline;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import au.com.bytecode.opencsv.CSVReader;

import net.microscraper.client.BasicMicroscraper;
import net.microscraper.client.Microscraper;
import net.microscraper.client.MicroscraperException;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.SQLConnectionException;
import net.microscraper.impl.log.JavaIOFileLogger;
import net.microscraper.impl.log.SystemOutLogger;

import static net.microscraper.impl.commandline.Arguments.*;

public class ArgumentsMicroscraper {

	private final Microscraper scraper;
	private final Arguments args;
	
	/**
	 * Initialize an {@link ArgumentsMicroscraper}.
	 * @param arguments The array of {@link String} arguments from the command line to use.
	 * @throws IOException 
	 * @throws DatabaseException 
	 * @throws SQLConnectionException 
	 */
	public ArgumentsMicroscraper(Arguments args) throws SQLConnectionException, DatabaseException, IOException {		
		Database database = new ArgumentsDatabase(args);
		
		this.args = args;
		scraper = new BasicMicroscraper(database);

		// Register logs.
		if(args.has(LOG_FILE)) {
			scraper.register(new JavaIOFileLogger(new File(args.get(LOG_FILE))));
		}
		if(args.has(LOG_STDOUT)) {
			scraper.register(new SystemOutLogger());
		}
		
		// Set rate limit.
		try {
			scraper.setRateLimit(Integer.parseInt(args.get(RATE_LIMIT)));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(RATE_LIMIT + " must be an integer");
		}
		
		// Set timeout.
		try {
			scraper.setTimeout(Integer.parseInt(args.get(TIMEOUT)));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(TIMEOUT + " must be an integer");
		}
	}
	
	public void scrape() throws IOException, MicroscraperException { 
		
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
				scrape(lineDefaults);
			}
		} else {
			scrape(new Hashtable<String, String>());
		}
	}
	
	private void scrape(Hashtable<String, String> extraDefaults) throws MicroscraperException {
		if(args.has(JSON_INSTRUCTION)) {
			scraper.scrapeWithJSON(args.get(JSON_INSTRUCTION), args.get(DEFAULTS), extraDefaults);
		} else if(args.has(URI_INSTRUCTION)) {
			scraper.scrapeWithURI(args.get(URI_INSTRUCTION), args.get(DEFAULTS), extraDefaults);
		}
	}
}
