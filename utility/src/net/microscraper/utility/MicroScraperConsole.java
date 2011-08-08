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
import net.microscraper.impl.browser.JavaNetBrowser;
import net.microscraper.impl.database.MultiTableDatabase;
import net.microscraper.impl.file.JavaIOFileLoader;
import net.microscraper.impl.json.JSONME;
import net.microscraper.impl.json.JavaNetJSONLocation;
import net.microscraper.impl.log.JavaIOFileLogger;
import net.microscraper.impl.log.SystemOutLogger;
import net.microscraper.impl.publisher.JDBCSqliteConnection;
import net.microscraper.impl.publisher.SQLConnectionException;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.impl.regexp.JavaUtilRegexpCompiler;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.FindOne;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.database.Connection;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.json.JSONLocation;
import net.microscraper.interfaces.json.JSONLocationException;
import net.microscraper.interfaces.regexp.RegexpCompiler;

public class MicroScraperConsole {
	private static final String newline = System.getProperty("line.separator");
	
	private static int rateLimit = Browser.DEFAULT_MAX_KBPS_FROM_HOST;
	private static int timeout = Browser.TIMEOUT;
	
	private static final String usage = 
"usage: microscraper <uri> [<options>]" + newline +
"" + newline +
"uri" + newline +
"	A URI that points to microscraper instructions." + newline +
"options:" + newline +
"	--defaults=\"<defaults>\"" + newline +
"		A form-encoded string of name value pairs to use as" + newline +
"		defaults during execution." + newline +
"	--input=<path> [--column-delimiter=<delimiter>]" + newline +
"		Path to a file with any number of additional default" + newline +
"		values.  Each row is executed separately.  The first" + newline +
"		row contains column names." + newline +
"		The default column delimiter is ',' ." + newline +
"	--log-file[=<path>]" + newline +
"		Pipe the log to a file." + newline +
"		Path is optional, defaults to 'yyyyMMddkkmmss.log' in the" + newline +
"		current directory." + newline +
"	--log-stdout" + newline +
"		Pipe the log to stdout." + newline +
"	--output-format=(csv|formencoded|tab|sqlite)" + newline +
"		How to format output.  Defaults to sqlite." + newline +
"	--output-file[=<path>], --no-output-file" + newline +
"		Whether to save output to a file.  Enabled by default." + newline +
"		Path is optional, defaults to 'yyyyMMddkkmmss.<format>' in" + newline +
"		the current directory." + newline +
"	--output-stdout" + newline +
"		Pipe output to stdout.  This is not compatible with" + newline +
"		sqlite." +
"	--rate-limit=<max-kbps>" + newline +
"		The rate limit, in KBPS, for loading from a single host." + newline +
"		Defaults to " + Integer.toString(rateLimit) + " KBPS." +
"	--timeout=<timeout>" + newline +
"		How many milliseconds to wait before giving up on a request." +
"		Defaults to " + Integer.toString(timeout) + " milliseconds.";

	private static final String TIMESTAMP = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());
	private static final String ENCODING = "UTF-8";
	
	private static String DEFAULTS_OPTION = "--defaults";
	private static NameValuePair[] defaults = new NameValuePair[0];
	
	private static String INPUT_OPTION = "--input";
	private static String inputPath = null;
	private static CSVReader input = null;
	
	private static String COLUMN_DELIMITER_OPTION = "--column-delimiter";
	private static char columnDelimiter = ',';
	
	private static String LOG_FILE_OPTION = "--log-file";
	private static String fileLogPath = null;
	private static JavaIOFileLogger fileLog = null;
	
	private static String LOG_STDOUT_OPTION = "--log-stdout";
	private static boolean logStdout = false;

	private static String OUTPUT_FORMAT_OPTION = "--output-format";
	private static String CSV_OUTPUT_FORMAT_VALUE = "csv";
	private static String FORM_ENCODED_OUTPUT_FORMAT_VALUE = "formencoded";
	private static String TAB_OUTPUT_FORMAT_VALUE = "tab";
	private static String SQLITE_OUTPUT_FORMAT_VALUE = "sqlite";
	private static String outputFormat = SQLITE_OUTPUT_FORMAT_VALUE;
	private static final List<String> validOutputFormats = Arrays.asList(
			CSV_OUTPUT_FORMAT_VALUE,
			FORM_ENCODED_OUTPUT_FORMAT_VALUE,
			TAB_OUTPUT_FORMAT_VALUE,
			SQLITE_OUTPUT_FORMAT_VALUE
			);
	
	private static String OUTPUT_FILE_OPTION = "--output-file";
	private static File outputFile = null;
	//private static String outputFileName = null;
	
	private static String NO_OUTPUT_FILE_OPTION = "--no-output-file";
	private static boolean noOutputFile = false;
	
	private static String OUTPUT_STDOUT_OPTION = "--output-stdout";
	private static boolean outputStdout = false;
	
	private static String RATE_LIMIT_OPTION = "--rate-limit";
	
	private static String TIMEOUT_OPTION = "--timeout";
		
	private static JSONLocation instructionsLocation;
	
	private static final Log log = new Log();
	private static Browser browser;
	private static final FileLoader fileLoader = new JavaIOFileLoader();
	private static final JSONInterface jsonInterface = new JSONME(fileLoader, browser);
	//private static final RegexpCompiler regexpCompiler = new JakartaRegexpCompiler();
	private static final RegexpCompiler regexpCompiler = new JavaUtilRegexpCompiler();
	private static Connection connection;
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
		} catch(JSONLocationException e) {
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
					FileNotFoundException, SQLConnectionException, JSONLocationException,
					DatabaseException, IOException,
					UnsupportedEncodingException {
		if(args.length == 0)
			throw new IllegalArgumentException("You must specify the URI of scraper instructions.");
		
		for(int i = 1 ; i < args.length ; i ++) {
			try {
				String arg = args[i];
				String value = null;
				if(arg.indexOf('=') > -1) {
					value = arg.substring(arg.indexOf('=') + 1);
				}
				if(arg.startsWith(DEFAULTS_OPTION)) {
					// Quotations are optional.
					if(value.startsWith("\"") && value.endsWith("\"")) {
						value = value.substring(1, value.length() - 2);
					}
					defaults = Utils.formEncodedDataToNameValuePairs(value, ENCODING);
				} else if(arg.startsWith(INPUT_OPTION)) {
					inputPath = value;
					input = new CSVReader(new FileReader(inputPath), columnDelimiter);
				} else if(arg.startsWith(COLUMN_DELIMITER_OPTION)) {
					if(value.length() > 1) {
						throw new IllegalArgumentException("Column delimiter must be a single character.");
					}
					columnDelimiter = value.charAt(0);
				} else if(arg.startsWith(LOG_FILE_OPTION)) {
					fileLogPath = value;
					fileLog = new JavaIOFileLogger(new File(fileLogPath));
				} else if(arg.startsWith(LOG_STDOUT_OPTION)) {
					logStdout = true;
				} else if(arg.startsWith(OUTPUT_FORMAT_OPTION)) {
					if(validOutputFormats.contains(value)) {
						outputFormat = value;
					} else {
						throw new IllegalArgumentException(Utils.quote(value)
								+ " is not a valid output format.");
					}
				} else if(arg.startsWith(OUTPUT_FILE_OPTION)) {
					outputFile = new File(value);
				} else if(arg.startsWith(NO_OUTPUT_FILE_OPTION)) {
					noOutputFile = true;
				} else if(arg.startsWith(OUTPUT_STDOUT_OPTION)) {
					outputStdout = true;
				} else if(arg.startsWith(RATE_LIMIT_OPTION)) {
					try {
						rateLimit = Integer.parseInt(value);
					} catch(NumberFormatException e) {
						
					}
				} else if(arg.startsWith(TIMEOUT_OPTION)) {
					timeout = Integer.parseInt(value);
				} else {
					throw new IllegalArgumentException(Utils.quote(arg) + " is not a valid parameter.");
				}
			} catch(NumberFormatException e) {
				throw new IllegalArgumentException(Utils.quote(args[i]) + " must be an integer.");
			}
		}
		
		if(outputStdout == true && outputFormat.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
			throw new IllegalArgumentException();
		}
		if(logStdout) {
			log.register(new SystemOutLogger());
		}
		if(fileLog != null) {
			log.register(fileLog);
			fileLog.open();
		}
		
		// Default output file name.
		if(outputFile == null && noOutputFile == false) {
			outputFile = new File(TIMESTAMP + "." + outputFormat);
		}
		
		if(outputFormat.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
			connection = JDBCSqliteConnection.toFile(outputFile.getPath(), log);			
		} else if(outputFormat.equals(CSV_OUTPUT_FORMAT_VALUE)) {
			//publisher = new CSVPublisher();
		} else if(outputFormat.equals(TAB_OUTPUT_FORMAT_VALUE)) {
			//publisher = new CSVPublisher();
		} else if(outputFormat.equals(FORM_ENCODED_OUTPUT_FORMAT_VALUE)) {
			//publisher = new FormEncodedPublisher();
		}
		connection.open();
		database = new MultiTableDatabase(connection);
		
		if(!args[0].startsWith("--")) {
			instructionsLocation = new JavaNetJSONLocation(args[0]);
		} else {
			throw new IllegalArgumentException();
		}
		browser = new JavaNetBrowser(log, rateLimit, Browser.DEFAULT_SLEEP_TIME, timeout);
		client = new Client(regexpCompiler,	log, browser, jsonInterface, database);
	}
	
	private static void scrape() throws ClientException, IOException {
		if(input == null) {
			log.i("Scraping using instructions from " + Utils.quote(instructionsLocation.toString()) +
					" and defaults " + Utils.quote(Utils.preview(defaults)));
			client.scrape(instructionsLocation, defaults);
		} else {			
			log.i("Scraping each row of " + Utils.quote(inputPath) + 
					" using instructions from " + Utils.quote(instructionsLocation.toString())  +
					" and defaults " + Utils.quote(Utils.preview(defaults)));
			String[] headers = input.readNext();
			NameValuePair[] lineDefaults = Arrays.copyOf(defaults, defaults.length + headers.length);
			
			String[] values;
			while((values = input.readNext()) != null) {
				for(int i = 0 ; i < values.length ; i ++) {
					lineDefaults[i + defaults.length] = new BasicNameValuePair(headers[i], values[i]);
				}
				client.scrape(instructionsLocation, lineDefaults);
			}
		}
		log.i("Finished scraping from " + Utils.quote(instructionsLocation.toString()));
	}
	
	private static void finish() throws IOException {
		try {
			if(connection != null) {
				connection.close();
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
		if(outputFile != null) {
			print("Output saved to " + Utils.quote(outputFile.getAbsolutePath()));
		}
	}
	
	private static void print(String text) {
		System.out.print(text);
		System.out.println();
	}
}