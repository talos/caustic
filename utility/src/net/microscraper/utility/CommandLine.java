package net.microscraper.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.microscraper.BasicMicroscraper;
import net.microscraper.Microscraper;
import net.microscraper.MicroscraperException;
import net.microscraper.Utils;
import net.microscraper.database.impl.DelimitedConnection;
import net.microscraper.impl.database.JDBCSqliteConnection;
import net.microscraper.impl.database.MultiTableDatabase;
import net.microscraper.impl.database.SQLConnectionException;
import net.microscraper.impl.database.SingleTableDatabase;
import net.microscraper.impl.log.JavaIOFileLogger;
import net.microscraper.impl.log.SystemOutLogger;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.database.IOConnection;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.uri.URIInterfaceException;
import au.com.bytecode.opencsv.CSVReader;

public class CommandLine {

	private static final String newline = System.getProperty("line.separator");
	
	public static final String URI_INSTRUCTION_OPTION = "--uri";
	public static final String JSON_INSTRUCTION_OPTION = "--json";
	
	public static final String TIMESTAMP = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());
	
	public static final String BATCH_SIZE_OPTION = "--batch-size";
	public static final int DEFAULT_BATCH_SIZE = 100;
	
	public static final String DEFAULTS_OPTION = "--defaults";
	
	public static final String INPUT_OPTION = "--input";
	
	public static final String INPUT_COLUMN_DELIMITER = "--column-delimiter";
	public static final char DEFAULT_INPUT_COLUMN_DELIMITER = ',';
	
	public static final String LOG_FILE_OPTION = "--log-file";	
	public static final String LOG_STDOUT_OPTION = "--log-stdout";

	public static String MAX_RESPONSE_SIZE_OPTION = "--max-response-size";
	public static int DEFAULT_MAX_RESPONSE_SIZE = Browser.DEFAULT_MAX_RESPONSE_SIZE;
	
	public static final String OUTPUT_FORMAT_OPTION = "--output-format";
	public static final String CSV_OUTPUT_FORMAT_VALUE = "csv";
	public static final String TAB_OUTPUT_FORMAT_VALUE = "tab";
	public static final String SQLITE_OUTPUT_FORMAT_VALUE = "sqlite";
	public static final String DEFAULT_FILE_OUTPUT_FORMAT = SQLITE_OUTPUT_FORMAT_VALUE;
	public static final String DEFAULT_STDOUT_OUTPUT_FORMAT = TAB_OUTPUT_FORMAT_VALUE;
	public static final List<String> validOutputFormats = Arrays.asList(
			CSV_OUTPUT_FORMAT_VALUE,
			TAB_OUTPUT_FORMAT_VALUE,
			SQLITE_OUTPUT_FORMAT_VALUE
			);
	public static final char TAB_OUTPUT_COLUMN_DELIMITER = '\t';
	public static final char CSV_OUTPUT_COLUMN_DELIMITER = ',';
	
	public static final String OUTPUT_TO_FILE_OPTION = "--output-to-file";
		
	public static final String RATE_LIMIT_OPTION = "--rate-limit";
	public static final int DEFAULT_RATE_LIMIT = Browser.DEFAULT_RATE_LIMIT;
	
	public static final String SINGLE_TABLE_OPTION = "--single-table";
	
	public static final String TIMEOUT_OPTION = "--timeout";
	public static final int DEFAULT_TIMEOUT = Browser.DEFAULT_TIMEOUT;
		
	public static final String USAGE = 
"usage: microscraper <uri> [<options>]" + newline +
"		microscraper (" + JSON_INSTRUCTION_OPTION + "=\"<json>\"|" + URI_INSTRUCTION_OPTION + "<uri>) [<options>]" + newline +
"" + newline +
"uri" + newline +
"	A URI that points to microscraper instruction JSON." + newline +
"json" + newline +
"	Microscraper instruction JSON." + newline +
"options:" + newline +
"	" + BATCH_SIZE_OPTION + "=<batch-size>" + newline +
"		If saving to SQL, assigns the batch size.  " + newline +
"		Defaults to " + Integer.toString(DEFAULT_BATCH_SIZE) + newline +
"	" + DEFAULTS_OPTION + "=\"<defaults>\"" + newline +
"		A form-encoded string of name value pairs to use as" + newline +
"		defaults during execution." + newline +
"	" + INPUT_OPTION + "=<path> [--column-delimiter=<delimiter>]" + newline +
"		Path to a file with any number of additional default" + newline +
"		values.  Each row is executed separately.  The first" + newline +
"		row contains column names." + newline +
"		The default column delimiter is '"+ DEFAULT_INPUT_COLUMN_DELIMITER + "'." + newline +
"	" + LOG_FILE_OPTION + "[=<path>]" + newline +
"		Pipe the log to a file." + newline +
"		Path is optional, defaults to 'yyyyMMddkkmmss.log' in the" + newline +
"		current directory." + newline +
"	" + LOG_STDOUT_OPTION + newline +
"		Pipe the log to stdout." + newline +
"	" + MAX_RESPONSE_SIZE_OPTION + newline +
"		How many KB of a response to load from a single request before " + newline +
"		cutting off the response.  Defaults to " + DEFAULT_MAX_RESPONSE_SIZE + "KB." + newline +
"	" + OUTPUT_FORMAT_OPTION + "=(" + Utils.join(validOutputFormats.toArray(new String[0]), "|") +")" + newline +
"		How to format output.  Defaults to " + DEFAULT_FILE_OUTPUT_FORMAT + " unless " + newline +
"		--log-stdout is specified, in which case it defaults to " + DEFAULT_STDOUT_OUTPUT_FORMAT + "." + newline +
"	" + OUTPUT_TO_FILE_OPTION + "[=<path>], " + newline +
"		Where to save the output.  Defaults to 'yyyyMMddkkmmss.<format>' in" + newline +
"		the current directory output." + newline +
"	" + RATE_LIMIT_OPTION + "=<max-kbps>" + newline +
"		The rate limit, in KBPS, for loading from a single host." + newline +
"		Defaults to " + Integer.toString(DEFAULT_RATE_LIMIT) + " KBPS." + newline +
"	" + SINGLE_TABLE_OPTION + newline +
"		Save all results to a single sqlite table, if using sqlite" + newline +
"	" + TIMEOUT_OPTION + "=<timeout>" + newline +
"		How many seconds to wait before giving up on a request." + newline + 
"		Defaults to " + Integer.toString(DEFAULT_TIMEOUT) + " seconds.";
	
	public static Map<String, String> getArguments(String[] args) throws IllegalArgumentException,
					FileNotFoundException, SQLConnectionException, JSONInterfaceException,
					DatabaseException, IOException,
					UnsupportedEncodingException, URIInterfaceException {
		if(args.length == 0) {
			throw new IllegalArgumentException("You must specify the URI of scraper instructions.");
		}
		
		//boolean isInline;
		//String instruction;
		int argStartIndex = 0;
		Map<String, String> arguments = new HashMap<String, String>();
		
		if(!args[0].equals(JSON_INSTRUCTION_OPTION) && !args[0].equals(URI_INSTRUCTION_OPTION)) {
			argStartIndex = 1;
			arguments.put(URI_INSTRUCTION_OPTION, args[0]);
		}
		
		for(int i = argStartIndex ; i < args.length ; i ++) {
			String arg = args[i];
			String value = null;
			if(arg.indexOf('=') > -1) {
				value = arg.substring(arg.indexOf('=') + 1);
				arg = arg.substring(0, arg.indexOf('='));
			}
			arguments.put(arg, value);
		}
		return arguments;
		//Database database = getDatabase(arguments);
		//Microscraper scraper = getScraper(arguments, database);
	}
	
	
	public static Database getDatabase(Map<String, String> arguments)
			throws SQLConnectionException, DatabaseException, IOException {

		// Determine format.
		String format;
		if(arguments.containsKey(OUTPUT_FORMAT_OPTION)) {
			format = arguments.get(OUTPUT_FORMAT_OPTION);
			if(validOutputFormats.contains(format)) {
				throw new IllegalArgumentException(Utils.quote(format)
						+ " is not a valid output format.");
			}
		} else if(arguments.containsKey(OUTPUT_TO_FILE_OPTION)) {
			format = DEFAULT_FILE_OUTPUT_FORMAT;
		} else {
			format = DEFAULT_STDOUT_OUTPUT_FORMAT;
		}
		
		// Determine delimiter.
		char delimiter;
		if(format.equals(CSV_OUTPUT_FORMAT_VALUE)) {
			delimiter = CSV_OUTPUT_COLUMN_DELIMITER;
		} else { // (format.equals(TAB_OUTPUT_COLUMN_DELIMITER)) {
			delimiter = TAB_OUTPUT_COLUMN_DELIMITER;
		}
		
		// Set up output and databases.
		if(arguments.containsKey(OUTPUT_TO_FILE_OPTION)) {
			String outputLocation = arguments.get(OUTPUT_TO_FILE_OPTION);
			if(outputLocation == null) {
				outputLocation = TIMESTAMP + "." + format;
			}
			
			if(format.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
				
				int batchSize;
				if(arguments.containsKey(BATCH_SIZE_OPTION)) {
					batchSize = Integer.parseInt(arguments.get(BATCH_SIZE_OPTION));
				} else {
					batchSize = DEFAULT_BATCH_SIZE;
				}
				
				IOConnection connection = JDBCSqliteConnection.toFile(outputLocation, batchSize);

				if(arguments.containsKey(SINGLE_TABLE_OPTION)) {
					return new SingleTableDatabase(connection);
				} else {
					return new MultiTableDatabase(connection);
				}
				
			} else {
				return new SingleTableDatabase(DelimitedConnection.toFile(outputLocation, delimiter));
			}
			
		} else { // output to STDOUT
			return new SingleTableDatabase(DelimitedConnection.toStdOut(delimiter));
		}
	}
	
	/**
	 * Initialize a {@link Microscraper}.
	 * @param arguments A {@link Map} of arguments for the scraper.
	 * @param database The {@link Database} to use.
	 * @return A {@link Microscraper}.
	 */
	public static Microscraper getScraper(Map<String, String> arguments, Database database) {
		
		Microscraper scraper = BasicMicroscraper.to(database);

		// Register logs.
		if(arguments.containsKey(LOG_FILE_OPTION)) {
			scraper.register(new JavaIOFileLogger(new File(arguments.get(LOG_FILE_OPTION))));
		}
		if(arguments.containsKey(LOG_STDOUT_OPTION)) {
			scraper.register(new SystemOutLogger());
		}
		
		// Set rate limit.
		if(arguments.containsKey(RATE_LIMIT_OPTION)) {
			try {
				scraper.setRateLimit(Integer.parseInt(arguments.get(RATE_LIMIT_OPTION)));
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException(RATE_LIMIT_OPTION + " must be an integer");
			}
		} else {
			scraper.setRateLimit(DEFAULT_RATE_LIMIT);
		}
		
		// Set timeout.
		if(arguments.containsKey(TIMEOUT_OPTION)) {
			try {
				scraper.setTimeout(Integer.parseInt(arguments.get(TIMEOUT_OPTION)));
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException(TIMEOUT_OPTION + " must be an integer");
			}
		} else {
			scraper.setTimeout(DEFAULT_TIMEOUT);
		}
		
		return scraper;
	}
	
	public static void runScraper(Map<String, String> arguments, Microscraper scraper) throws MicroscraperException, IOException { 
		// Fix quotations on default values.
		if(arguments.containsKey(DEFAULTS_OPTION)) {
			String possiblyQuotedDefaults = arguments.get(DEFAULTS_OPTION);
			if(possiblyQuotedDefaults.startsWith("\"") && possiblyQuotedDefaults.endsWith("\"")) {
				arguments.put(DEFAULTS_OPTION, possiblyQuotedDefaults.substring(1, possiblyQuotedDefaults.length() - 2));
			}
		}
		
		// Run (handle inputs)
		if(arguments.containsKey(INPUT_OPTION)) {
			char inputColumnDelimiter;
			if(arguments.containsKey(INPUT_COLUMN_DELIMITER)) {
				String delim = arguments.get(INPUT_COLUMN_DELIMITER);
				if(delim.length() > 1) {
					throw new IllegalArgumentException(INPUT_COLUMN_DELIMITER + " must be a single character.");
				}
				inputColumnDelimiter = delim.charAt(0);
			} else {
				inputColumnDelimiter = DEFAULT_INPUT_COLUMN_DELIMITER;
			}
			CSVReader input = new CSVReader(new FileReader(arguments.get(INPUT_OPTION)), inputColumnDelimiter);
			
			String[] headers = input.readNext();
			String[] values;
			while((values = input.readNext()) != null) {
				Hashtable<String, String> lineDefaults = new Hashtable<String, String>();
				for(int i = 0 ; i < values.length ; i ++) {
					lineDefaults.put(headers[i], values[i]);
				}
				scrape(arguments, scraper, lineDefaults);
			}
		} else {
			scrape(arguments, scraper, new Hashtable<String, String>());
		}	
	}
	
	private static void scrape(Map<String, String> arguments, Microscraper scraper, Hashtable<String, String> extraDefaults) throws MicroscraperException {
		if(arguments.containsKey(JSON_INSTRUCTION_OPTION)) {
			scraper.scrapeWithJSON(arguments.get(JSON_INSTRUCTION_OPTION), arguments.get(DEFAULTS_OPTION), extraDefaults);
		} else if(arguments.containsKey(URI_INSTRUCTION_OPTION)) {
			scraper.scrapeWithURI(arguments.get(URI_INSTRUCTION_OPTION), arguments.get(DEFAULTS_OPTION), extraDefaults);
		}
	}
}
