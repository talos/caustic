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
	private static final String usage = 
"usage: microscraper <uri> [<options>]" + newline +
"" + newline +
"uri" + newline +
"	A URI that points to microscraper instructions." + newline +
"options:" + newline +
"	--defaults=<defaults>" + newline +
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
"		sqlite.";

	private static final String TIMESTAMP = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());
	private static final String ENCODING = "UTF-8";
	
	private static String DEFAULTS_OPTION = "--defaults";
	private static NameValuePair[] defaults = new NameValuePair[0];
	
	private static String INPUT_OPTION = "--input";
	private static CSVReader input = null;
	
	private static String COLUMN_DELIMITER_OPTION = "--column-delimiter";
	private static char columnDelimiter = ',';
	
	private static String LOG_FILE_OPTION = "--log-file";
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
		
	private static JSONLocation instructionsLocation;
	
	private static final Log log = new Log();
	private static final Browser browser = new JavaNetBrowser(log, Browser.DEFAULT_MAX_KBPS_FROM_HOST, Browser.DEFAULT_SLEEP_TIME);
	private static final FileLoader fileLoader = new JavaIOFileLoader();
	private static final JSONInterface jsonInterface = new JSONME(fileLoader, browser);
	private static final RegexpCompiler regexpCompiler = new JakartaRegexpCompiler();
	private static Connection connection;
	private static Database database;
	private static Client client;
	
	public static void main (String[] args) {
		try {
			initialize(args);
			try {
				scrape();
			} catch(ClientException e) {
				// Error scraping
			} catch(IOException e) {
				// Error reading input file or writing to output file (log or output)
			}
		} catch(IllegalArgumentException e) {
			// Error with args provided
			print(e.getMessage());
			print(usage);
		} catch(FileNotFoundException e) {
			// Could not find the input file
		} catch(SQLConnectionException e) {
			// Could not open connection to SQL
		} catch(DatabaseException e) {
			// Could not set up database.
		} catch(UnsupportedEncodingException e) {
			// Encoding not supported on this system.
		} catch(JSONLocationException e) {
			// Invalid location for instructions.
		} catch(IOException e) {
			// Could not open log file
		}
		try {
			finish();
		} catch(IOException e) {
			// Could not close a file
		}
	}
	
	private static void initialize(String[] args) throws IllegalArgumentException,
					FileNotFoundException, SQLConnectionException, JSONLocationException,
					DatabaseException, IOException,
					UnsupportedEncodingException {
		if(args.length == 0)
			throw new IllegalArgumentException("You must specify the URI of scraper instructions.");
		
		for(int i = 1 ; i < args.length ; i ++) {
			String arg = args[i];
			String value = null;
			if(arg.indexOf('=') > -1) {
				value = arg.substring(arg.indexOf('=') + 1);
			}
			if(arg.startsWith(DEFAULTS_OPTION)) {
				defaults = Utils.formEncodedDataToNameValuePairs(value, ENCODING);
			} else if(arg.startsWith(INPUT_OPTION)) {
				input = new CSVReader(new FileReader(value), columnDelimiter);
			} else if(arg.startsWith(COLUMN_DELIMITER_OPTION)) {
				if(value.length() > 1) {
					throw new IllegalArgumentException("Column delimiter must be a single character.");
				}
				columnDelimiter = value.charAt(0);
			} else if(arg.startsWith(LOG_FILE_OPTION)) {
				fileLog = new JavaIOFileLogger(new File(value));
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
			} else {
				throw new IllegalArgumentException(Utils.quote(arg) + " is not a valid parameter.");
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
			/*database = new SQLMultiTableDatabase(
					new JDBCSQLite(outputFile.getPath(), log));*/
			
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
		client = new Client(regexpCompiler,	log, browser, jsonInterface, database);
	}
	
	private static void scrape() throws ClientException, IOException {
		if(input == null) {
			client.scrape(instructionsLocation, defaults);
		} else {			
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
		}
	}
	
	private static void print(String text) {
		System.out.print(text);
		System.out.println();
	}
}