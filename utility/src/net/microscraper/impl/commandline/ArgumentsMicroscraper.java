package net.microscraper.impl.commandline;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import au.com.bytecode.opencsv.CSVReader;

import net.microscraper.browser.JavaNetBrowser;
import net.microscraper.client.BasicMicroscraper;
import net.microscraper.client.Browser;
import net.microscraper.client.DeserializationException;
import net.microscraper.client.Microscraper;
import net.microscraper.database.Database;
import net.microscraper.database.SQLConnectionException;
import net.microscraper.impl.log.JavaIOFileLogger;
import net.microscraper.impl.log.SystemOutLogger;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.util.HashtableUtils;

import static net.microscraper.impl.commandline.Arguments.*;

public class ArgumentsMicroscraper {

	private final Microscraper scraper;
	private final Arguments args;
	
	/**
	 * Initialize an {@link ArgumentsMicroscraper}.
	 * @param arguments The array of {@link String} arguments from the command line to use.
	 * @throws IOException 
	 * @throws SQLConnectionException 
	 */
	public ArgumentsMicroscraper(Arguments args) throws SQLConnectionException, IOException {		
		Database database = new ArgumentsDatabase(args);
		
		this.args = args;
		final int rateLimit;
		final int timeout;
		
		// Set rate limit.
		try {
			rateLimit = Integer.parseInt(args.get(RATE_LIMIT));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(RATE_LIMIT + " must be an integer");
		}
		
		// Set timeout.
		try {
			timeout = Integer.parseInt(args.get(TIMEOUT));
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException(TIMEOUT + " must be an integer");
		}
		
		scraper = BasicMicroscraper.get(database, rateLimit, timeout);

		// Register logs.
		if(args.has(LOG_FILE)) {
			scraper.register(new JavaIOFileLogger(new File(args.get(LOG_FILE))));
		}
		if(args.has(LOG_STDOUT)) {
			scraper.register(new SystemOutLogger());
		}
	}
	
	public void scrape() throws IOException, InterruptedException, DeserializationException, MalformedUriException { 
		Hashtable<String, String> defaults;
		defaults = HashtableUtils.fromFormEncoded(new JavaNetBrowser(), args.get(DEFAULTS), Browser.UTF_8);
		
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
		if(args.has(JSON_INSTRUCTION)) {
			scraper.scrapeFromJson(args.get(JSON_INSTRUCTION), defaults);
		} else if(args.has(URI_INSTRUCTION)) {
			scraper.scrapeFromUri(args.get(URI_INSTRUCTION), defaults);
		}
	}
}
