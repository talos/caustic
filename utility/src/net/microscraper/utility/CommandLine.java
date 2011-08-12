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
	
	private static final String INLINE_SWITCH = "-e";
	
	private static final String TIMESTAMP = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());
	
	private static final String BATCH_SIZE_OPTION = "--batch-size";
	private final int DEFAULT_BATCH_SIZE = 100;
	
	private static final String DEFAULTS_OPTION = "--defaults";
	private final String defaults;
	
	private static final String INPUT_OPTION = "--input";
	private final String inputPath;
	private final CSVReader input;
	
	private static final String INPUT_COLUMN_DELIMITER = "--column-delimiter";
	private static final char DEFAULT_INPUT_COLUMN_DELIMITER = ',';
	
	private static final String LOG_FILE_OPTION = "--log-file";
	private final JavaIOFileLogger fileLog;
	
	private static final String LOG_STDOUT_OPTION = "--log-stdout";

	private static String MAX_RESPONSE_SIZE_OPTION = "--max-response-size";
	private static int DEFAULT_MAX_RESPONSE_SIZE = Browser.DEFAULT_MAX_RESPONSE_SIZE;
	
	private static final String OUTPUT_FORMAT_OPTION = "--output-format";
	private static final String CSV_OUTPUT_FORMAT_VALUE = "csv";
	private static final String TAB_OUTPUT_FORMAT_VALUE = "tab";
	private static final String SQLITE_OUTPUT_FORMAT_VALUE = "sqlite";
	private static final String DEFAULT_FILE_OUTPUT_FORMAT = SQLITE_OUTPUT_FORMAT_VALUE;
	private static final String DEFAULT_STDOUT_OUTPUT_FORMAT = TAB_OUTPUT_FORMAT_VALUE;
	private static final List<String> validOutputFormats = Arrays.asList(
			CSV_OUTPUT_FORMAT_VALUE,
			TAB_OUTPUT_FORMAT_VALUE,
			SQLITE_OUTPUT_FORMAT_VALUE
			);
	private final char outputColumnDelimiter;
	private static final char TAB_OUTPUT_COLUMN_DELIMITER = '\t';
	private static final char CSV_OUTPUT_COLUMN_DELIMITER = ',';
	
	private static final String OUTPUT_TO_FILE_OPTION = "--output-to-file";
		
	private static final String RATE_LIMIT_OPTION = "--rate-limit";
	private final int DEFAULT_RATE_LIMIT = Browser.DEFAULT_RATE_LIMIT;
	
	private static final String SINGLE_TABLE_OPTION = "--single-table";
	
	private static final String TIMEOUT_OPTION = "--timeout";
	private final int DEFAULT_TIMEOUT = Browser.DEFAULT_TIMEOUT;

	private final String instructionURI;
	private final String instructionJSON;
	
	private final Database database;
	private final Microscraper client;
	
	private final String usage = 
"usage: microscraper <uri> [<options>]" + newline +
"		microscraper -e \"<json>\" [<options>]" + newline +
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
	
	
	public CommandLine(String[] args) throws IllegalArgumentException,
					FileNotFoundException, SQLConnectionException, JSONInterfaceException,
					DatabaseException, IOException,
					UnsupportedEncodingException, URIInterfaceException {
		if(args.length == 0) {
			throw new IllegalArgumentException("You must specify the URI of scraper instructions.");
		}
		
		int argStartIndex = 1;
		if(args[0].equals(INLINE_SWITCH)) {
			argStartIndex = 2;
		}
		
		HashMap<String, String> argsMap = new HashMap<String, String> ();
		for(int i = argStartIndex ; i < args.length ; i ++) {
			String arg = args[i];
			String value = null;
			if(arg.indexOf('=') > -1) {
				value = arg.substring(arg.indexOf('=') + 1);
				arg = arg.substring(0, arg.indexOf('='));
			}
			argsMap.put(arg, value);
		}
		
		
		if(argsMap.containsKey(DEFAULTS_OPTION)) {
			String value = argsMap.get(DEFAULTS_OPTION);
			if(value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 2);
			}
			defaults = value;
		} else {
			defaults = "";
		}

		if(argsMap.containsKey(INPUT_COLUMN_DELIMITER)) {
			String delim = argsMap.get(INPUT_COLUMN_DELIMITER);
			if(delim.length() > 1) {
				throw new IllegalArgumentException("Column delimiter must be a single character.");
			}
			inputColumnDelimiter = delim.charAt(0);
		} else {
			inputColumnDelimiter = DEFAULT_INPUT_COLUMN_DELIMITER;
		}
		
		if(argsMap.containsKey(INPUT_OPTION)) {
			inputPath = argsMap.get(INPUT_OPTION);
			input = new CSVReader(new FileReader(inputPath), inputColumnDelimiter);
		} else {
			inputPath = null;
			input = null;
		}
		
		if(argsMap.containsKey(LOG_FILE_OPTION)) {
			fileLogPath = argsMap.get(LOG_FILE_OPTION);
			fileLog = new JavaIOFileLogger(new File(fileLogPath));
		}
		
		if(argsMap.containsKey(LOG_STDOUT_OPTION)) {
			logStdout = true;
		} else {
			logStdout = false;
		}
		
		
		if(argsMap.containsKey(RATE_LIMIT_OPTION)) {
			rateLimit = Integer.parseInt(argsMap.get(RATE_LIMIT_OPTION));
		} else {
			rateLimit = DEFAULT_RATE_LIMIT;
		}
		
		if(argsMap.containsKey(TIMEOUT_OPTION)) {
			timeout = Integer.parseInt(argsMap.get(TIMEOUT_OPTION));
		} else {
			timeout = DEFAULT_TIMEOUT;
		}
		
		if(logStdout) {
			client.register(new SystemOutLogger());
		}
		if(fileLog != null) {
			client.register(fileLog);
			fileLog.open();
		}
		
		if(args[0].equals(INLINE_SWITCH)) {
			instructionJSON = args[1];
			instructionURI = null;
		} else {
			instructionJSON = null;
			instructionURI = args[0];
		}
		
		
		
		if(argsMap.containsKey(OUTPUT_TO_FILE_OPTION)) {
			
			String format;
			if(argsMap.containsKey(OUTPUT_FORMAT_OPTION)) {
				format = argsMap.get(OUTPUT_FORMAT_OPTION);
				if(validOutputFormats.contains(format)) {
					throw new IllegalArgumentException(Utils.quote(format)
							+ " is not a valid output format.");
				}
			} else {
				format = DEFAULT_FILE_OUTPUT_FORMAT;
			}

			String outputLocation = argsMap.get(OUTPUT_TO_FILE_OPTION);
			if(outputLocation == null) {
				outputLocation = TIMESTAMP + "." + format;
			}
			
			if(format.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
				
				int batchSize;
				if(argsMap.containsKey(BATCH_SIZE_OPTION)) {
					batchSize = Integer.parseInt(argsMap.get(BATCH_SIZE_OPTION));
				} else {
					batchSize = DEFAULT_BATCH_SIZE;
				}
				
				IOConnection connection = JDBCSqliteConnection.toFile(outputLocation, client, batchSize);

				if(argsMap.containsKey(SINGLE_TABLE_OPTION)) {
					database = new SingleTableDatabase(connection);
				} else {
					database = new MultiTableDatabase(connection);
				}
				
			} else if(format.equals(TAB_OUTPUT_FORMAT_VALUE) || format.equals(CSV_OUTPUT_FORMAT_VALUE)) {
				database = new SingleTableDatabase(DelimitedConnection.toFile(outputLocation, outputColumnDelimiter));
			}
			
		} else { // output to STDOUT
			database = new SingleTableDatabase(DelimitedConnection.toStdOut(outputColumnDelimiter));
		}
		
		if(stdoutOutputFormat.equals(CSV_OUTPUT_FORMAT_VALUE)) {
			outputColumnDelimiter = CSV_OUTPUT_COLUMN_DELIMITER;
		} else if(stdoutOutputFormat.equals(TAB_OUTPUT_FORMAT_VALUE)) {
			outputColumnDelimiter = TAB_OUTPUT_COLUMN_DELIMITER;
		}
		
	}
	
	public void scrape(String urlEncodedDefaults, Hashtable extraDefaults) throws MicroscraperException {
		if(instructionJSON != null) {
			client.scrapeWithJSON(instructionJSON, defaults);
		} else if(instructionURI != null) {
			client.scrapeWithURI(instructionURI, defaults);
		}
	}
	
	public void scrapeInputFile(CSVReader input, String urlEncodedDefaults) throws MicroscraperException, IOException {
		String[] headers = input.readNext();
		String[] values;
		while((values = input.readNext()) != null) {
			Hashtable<String, String> lineDefaults = new Hashtable<String, String>();
			for(int i = 0 ; i < values.length ; i ++) {
				lineDefaults.put(headers[i], values[i]);
			}
			scrape(urlEncodedDefaults, lineDefaults);
		}
	}
	
	public void finish() throws IOException {
		try {
			if(database != null) {
				database.close();
			}
		} catch(DatabaseException e) {
			throw new IOException(e);
		}
		if(input != null) {
			input.close();
		}
		if(fileLog != null) {
			fileLog.close();
			print("Log saved to " + Utils.quote(fileLogPath));
		}
		if(outputLocation != null) {
			print("Output saved to " + Utils.quote(outputLocation));
		}
	}
	
	private static void print(String text) {
		System.out.print(text);
		System.out.println();
	}
}
