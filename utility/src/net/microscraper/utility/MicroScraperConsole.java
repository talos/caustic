package net.microscraper.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import net.microscraper.Client;
import net.microscraper.ClientException;
import net.microscraper.BasicNameValuePair;
import net.microscraper.Log;
import net.microscraper.NameValuePair;
import net.microscraper.Utils;
import net.microscraper.database.impl.DelimitedConnection;
import net.microscraper.impl.browser.JavaNetBrowser;
import net.microscraper.impl.database.JDBCSqliteConnection;
import net.microscraper.impl.database.MultiTableDatabase;
import net.microscraper.impl.database.SQLConnectionException;
import net.microscraper.impl.database.SingleTableDatabase;
import net.microscraper.impl.file.JavaIOFileLoader;
import net.microscraper.impl.json.JSONME;
import net.microscraper.impl.log.JavaIOFileLogger;
import net.microscraper.impl.log.SystemOutLogger;
import net.microscraper.impl.regexp.JavaUtilRegexpCompiler;
import net.microscraper.impl.uri.JavaNetURI;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.database.IOConnection;
import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.regexp.RegexpCompiler;
import net.microscraper.interfaces.uri.URIInterfaceException;

public class MicroScraperConsole {
	private static final String newline = System.getProperty("line.separator");
	
	private static int rateLimit = Browser.DEFAULT_MAX_KBPS_FROM_HOST;
	private static int timeout = Browser.TIMEOUT;
	private static int batchSize = 100;
	
	private static final String INLINE_SWITCH = "-e";
	
	private static final String TIMESTAMP = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());
	private static final String ENCODING = "UTF-8";
	
	private static final String BATCH_SIZE_OPTION = "--batch-size";
	
	private static final String DEFAULTS_OPTION = "--defaults";
	private static NameValuePair[] defaults = new NameValuePair[0];
	
	private static final String INPUT_OPTION = "--input";
	private static String inputPath = null;
	private static CSVReader input = null;
	
	private static final String INPUT_COLUMN_DELIMITER = "--column-delimiter";
	private static char inputColumnDelimiter = ',';
	
	private static final String LOG_FILE_OPTION = "--log-file";
	private static String fileLogPath = null;
	private static JavaIOFileLogger fileLog = null;
	
	private static String LOG_STDOUT_OPTION = "--log-stdout";
	private static boolean logStdout = false;

	private static String MAX_RESPONSE_SIZE_OPTION = "--max-response-size";
	private static int maxResponseSize = Browser.DEFAULT_MAX_RESPONSE_SIZE;
	
	private static final String OUTPUT_FORMAT_OPTION = "--output-format";
	private static final String CSV_OUTPUT_FORMAT_VALUE = "csv";
	private static final String TAB_OUTPUT_FORMAT_VALUE = "tab";
	private static final String SQLITE_OUTPUT_FORMAT_VALUE = "sqlite";
	private static String fileOutputFormat = SQLITE_OUTPUT_FORMAT_VALUE;
	private static String stdoutOutputFormat = TAB_OUTPUT_FORMAT_VALUE;
	private static final List<String> validOutputFormats = Arrays.asList(
			CSV_OUTPUT_FORMAT_VALUE,
			TAB_OUTPUT_FORMAT_VALUE,
			SQLITE_OUTPUT_FORMAT_VALUE
			);
	private static char outputColumnDelimiter;
	private static final char TAB_OUTPUT_COLUMN_DELIMITER = '\t';
	private static final char CSV_OUTPUT_COLUMN_DELIMITER = ',';
	
	private static final String OUTPUT_TO_FILE_OPTION = "--output-to-file";
	private static String outputLocation = null;
	
	//private static final String OUTPUT_STDOUT_OPTION = "--output-stdout";
	//private static boolean outputStdout = false;
	
	private static final String RATE_LIMIT_OPTION = "--rate-limit";
	
	private static final String SINGLE_TABLE_OPTION = "--single-table";
	private static boolean singleTable = false;
	
	private static final String TIMEOUT_OPTION = "--timeout";
		
	private static final String usage = 
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
"		Defaults to " + Integer.toString(batchSize) + newline +
"	" + DEFAULTS_OPTION + "=\"<defaults>\"" + newline +
"		A form-encoded string of name value pairs to use as" + newline +
"		defaults during execution." + newline +
"	" + INPUT_OPTION + "=<path> [--column-delimiter=<delimiter>]" + newline +
"		Path to a file with any number of additional default" + newline +
"		values.  Each row is executed separately.  The first" + newline +
"		row contains column names." + newline +
"		The default column delimiter is ',' ." + newline +
"	" + LOG_FILE_OPTION + "[=<path>]" + newline +
"		Pipe the log to a file." + newline +
"		Path is optional, defaults to 'yyyyMMddkkmmss.log' in the" + newline +
"		current directory." + newline +
"	" + LOG_STDOUT_OPTION + newline +
"		Pipe the log to stdout." + newline +
"	" + MAX_RESPONSE_SIZE_OPTION + newline +
"		How many KB of a response to load from a single request before " + newline +
"		cutting off the response.  Defaults to " + maxResponseSize + "KB." + newline +
"	" + OUTPUT_FORMAT_OPTION + "=(" + Utils.join(validOutputFormats.toArray(new String[0]), "|") +")" + newline +
"		How to format output.  Defaults to " + fileOutputFormat + " unless " + newline +
"		--log-stdout is specified, in which case it defaults to " + stdoutOutputFormat + "." + newline +
"	" + OUTPUT_TO_FILE_OPTION + "[=<path>], " + newline +
"		Where to save the output.  Defaults to 'yyyyMMddkkmmss.<format>' in" + newline +
"		the current directory output." + newline +
"	" + RATE_LIMIT_OPTION + "=<max-kbps>" + newline +
"		The rate limit, in KBPS, for loading from a single host." + newline +
"		Defaults to " + Integer.toString(rateLimit) + " KBPS." + newline +
"	" + SINGLE_TABLE_OPTION + newline +
"		Save all results to a single sqlite table, if using sqlite" + newline +
"	" + TIMEOUT_OPTION + "=<timeout>" + newline +
"		How many seconds to wait before giving up on a request." + newline + 
"		Defaults to " + Integer.toString(timeout) + " seconds.";

	//private static JSONLocation instructionsLocation;
	//private static String instructionsString;
	private static JSONInterfaceObject instructions;
	
	private static final Log log = new Log();
	private static Browser browser;
	private static final FileLoader fileLoader = new JavaIOFileLoader();
	private static final JSONInterface jsonInterface = new JSONME(fileLoader, browser);
	//private static final RegexpCompiler regexpCompiler = new JakartaRegexpCompiler();
	private static final RegexpCompiler regexpCompiler = new JavaUtilRegexpCompiler();
	//private static Connection connection;
	private static Database database;
	private static Client client;
	
	public static void main (String[] args) {
		try {
			initialize(args);
			try {
				scrape();
			} catch(ClientException e) {
				print("Error scraping: " + e.getMessage());
			} catch(IOException e) {
				print("Error reading input file or writing to output file (log or output): " + e.getMessage());
			}
		} catch(IllegalArgumentException e) {
			// Error with args provided
			print(e.getMessage());
			print(usage);
		} catch(FileNotFoundException e) {
			print("Could not find the input file: " + e.getMessage());
		} catch(SQLConnectionException e) {
			print("Could not open connection to SQL: " + e.getMessage());
		} catch(DatabaseException e) {
			print("Could not set up database: " + e.getMessage());
		} catch(UnsupportedEncodingException e) {
			print("Unsupported encoding: " + e.getMessage());
		} catch(URIInterfaceException e) {
			print("Could not locate instructions: " + e.getMessage());
		} catch(IOException e) {
			print("Could not open log file: " + e.getMessage());
		} catch(Throwable e) {
			print("Unhandled exception scraping: " + e.getClass().getName());
			print("Stack trace is in the log.");
			
			log.e(e);
			StackTraceElement[] trace = e.getStackTrace();
			for(int i = 0 ; i < trace.length ; i ++) {
				log.i(trace[i].toString());
			}
		}
		try {
			finish();
		} catch(IOException e) {
			print("Could not close file: " + e.getMessage());
		}
	}
	
	private static void initialize(String[] args) throws IllegalArgumentException,
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
		
		for(int i = argStartIndex ; i < args.length ; i ++) {
			try {
				String arg = args[i];
				String value = null;
				if(arg.indexOf('=') > -1) {
					value = arg.substring(arg.indexOf('=') + 1);
					arg = arg.substring(0, arg.indexOf('='));
				}
				if(arg.equals(BATCH_SIZE_OPTION)) {
					batchSize = Integer.parseInt(value);
				} else if(arg.equals(DEFAULTS_OPTION)) {
					// Quotations are optional.
					if(value.startsWith("\"") && value.endsWith("\"")) {
						value = value.substring(1, value.length() - 2);
					}
					defaults = Utils.formEncodedDataToNameValuePairs(value, ENCODING);
				} else if(arg.equals(INPUT_OPTION)) {
					inputPath = value;
					input = new CSVReader(new FileReader(inputPath), inputColumnDelimiter);
				} else if(arg.equals(INPUT_COLUMN_DELIMITER)) {
					if(value.length() > 1) {
						throw new IllegalArgumentException("Column delimiter must be a single character.");
					}
					inputColumnDelimiter = value.charAt(0);
				} else if(arg.equals(LOG_FILE_OPTION)) {
					fileLogPath = value;
					fileLog = new JavaIOFileLogger(new File(fileLogPath));
				} else if(arg.equals(LOG_STDOUT_OPTION)) {
					logStdout = true;
				} else if(arg.equals(OUTPUT_FORMAT_OPTION)) {
					if(validOutputFormats.contains(value)) {
						stdoutOutputFormat = value;
						fileOutputFormat = value;

					} else {
						throw new IllegalArgumentException(Utils.quote(value)
								+ " is not a valid output format.");
					}
				} else if(arg.equals(OUTPUT_TO_FILE_OPTION)) {
					if(value == null) {
						outputLocation = TIMESTAMP + "." + fileOutputFormat;
					} else {
						outputLocation = value;
					}
				} else if(arg.equals(RATE_LIMIT_OPTION)) {
					try {
						rateLimit = Integer.parseInt(value);
					} catch(NumberFormatException e) {
						
					}
				} else if(arg.equals(SINGLE_TABLE_OPTION)) {
					singleTable = true;
				} else if(arg.equals(TIMEOUT_OPTION)) {
					timeout = Integer.parseInt(value);
				} else {
					throw new IllegalArgumentException(Utils.quote(arg) + " is not a valid parameter.");
				}
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException(Utils.quote(args[i]) + " must be an integer.");
			}
		}
		
		if(logStdout) {
			log.register(new SystemOutLogger());
		}
		if(fileLog != null) {
			log.register(fileLog);
			fileLog.open();
		}
		
		if(args[0].equals(INLINE_SWITCH)) {
			instructions = jsonInterface.parse(new JavaNetURI(""), args[1]);
		} else {
			instructions = jsonInterface.load(new JavaNetURI(args[0]));
		}
		
		if(stdoutOutputFormat.equals(CSV_OUTPUT_FORMAT_VALUE)) {
			outputColumnDelimiter = CSV_OUTPUT_COLUMN_DELIMITER;
		} else if(stdoutOutputFormat.equals(TAB_OUTPUT_FORMAT_VALUE)) {
			outputColumnDelimiter = TAB_OUTPUT_COLUMN_DELIMITER;
		}
		
		if(outputLocation != null) {
			if(fileOutputFormat.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
				IOConnection connection = JDBCSqliteConnection.toFile(outputLocation, log, batchSize);
				if(singleTable == true) {
					database = new SingleTableDatabase(connection);
				} else {
					database = new MultiTableDatabase(connection);
				}
			} else if(fileOutputFormat.equals(TAB_OUTPUT_FORMAT_VALUE) || fileOutputFormat.equals(CSV_OUTPUT_FORMAT_VALUE)) {
				database = new SingleTableDatabase(DelimitedConnection.toFile(outputLocation, outputColumnDelimiter));
			}
			
		} else {
			if(stdoutOutputFormat.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
				throw new IllegalArgumentException("You cannot pipe " + SQLITE_OUTPUT_FORMAT_VALUE + " to stdout.");
			} else {
				database = new SingleTableDatabase(DelimitedConnection.toStdOut(outputColumnDelimiter));
			}
		}
		
		
		browser = new JavaNetBrowser(log, rateLimit, timeout, maxResponseSize);
		client = new Client(regexpCompiler,	log, browser, jsonInterface, database);
	}
	
	private static void scrape() throws ClientException, IOException {
		if(input == null) {
			log.i("Scraping using instructions " + Utils.quote(instructions.toString()) +
					" and defaults " + Utils.quote(Utils.preview(defaults)));
			client.scrape(instructions, defaults);
		} else {			
			log.i("Scraping each row of " + Utils.quote(inputPath) + 
					" using instructions " + Utils.quote(instructions.toString())  +
					" and defaults " + Utils.quote(Utils.preview(defaults)));
			String[] headers = input.readNext();
			NameValuePair[] lineDefaults = Arrays.copyOf(defaults, defaults.length + headers.length);
			
			String[] values;
			while((values = input.readNext()) != null) {
				for(int i = 0 ; i < values.length ; i ++) {
					lineDefaults[i + defaults.length] = new BasicNameValuePair(headers[i], values[i]);
				}
				client.scrape(instructions, lineDefaults);
			}
		}
	}
	
	private static void finish() throws IOException {
		try {
			if(database != null) {
				database.close();
			}
			/*if(connection != null) {
				connection.close();
			}*/
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