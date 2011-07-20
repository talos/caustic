package net.microscraper.utility;

import java.io.File;
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
import net.microscraper.Log;
import net.microscraper.NameValuePair;
import net.microscraper.Utils;
import net.microscraper.impl.browser.JavaNetBrowser;
import net.microscraper.impl.file.FileLogInterface;
import net.microscraper.impl.json.JSONME;
import net.microscraper.impl.log.SystemLogInterface;
import net.microscraper.impl.publisher.JDBCSQLite;
import net.microscraper.impl.publisher.SQLPublisher;
import net.microscraper.impl.publisher.SQLInterface.SQLInterfaceException;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.impl.regexp.JavaNetInterface;
import net.microscraper.interfaces.NetInterface;
import net.microscraper.interfaces.URIInterface;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.log.Logger;
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
	
	private static String LOG_FILE_OPTION = "--log-file";
	private static File logFile = null;
	
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
	//private static String outputFileName = null;
	
	private static String NO_OUTPUT_FILE_OPTION = "--no-output-file";
	private static boolean noOutputFile = false;
	
	private static String OUTPUT_STDOUT_OPTION = "--output-stdout";
	private static boolean outputStdout = false;
	
	private static final int sqlBatchSize = 1;
	
	private static String instructionsUri;
	
	private static final Log log = new Log();
	private static final Browser browser = new JavaNetBrowser(log, Browser.MAX_KBPS_FROM_HOST);
	private static final NetInterface netInterface = new JavaNetInterface(browser);
	private static final FileLoader uriLoader = new UtilityURILoader(netInterface);
	private static final JSONInterface jsonInterface = new JSONME(uriLoader);
	private static final RegexpCompiler regexpCompiler = new JakartaRegexpCompiler();
	private static SQLPublisher publisher;
	private static Client client;
	
	public static void main (String[] args) {
		try {
			initialize(args);
			scrape();
			publish();
		} catch(IllegalArgumentException e) {
			displayUsage();
		}
		finish();
	}
	
	private static void initialize(String[] args) throws IllegalArgumentException {
		for(int i = 0 ; i < args.length ; i ++) {
			String arg = args[i];
			if(arg.startsWith("--")) {
				String value = null;
				if(arg.indexOf('=') > -1) {
					value = arg.substring(arg.indexOf('='));
				}
				if(arg.equals(DEFAULTS_OPTION)) {
					defaults = Utils.formEncodedDataToNameValuePairs(value, ENCODING);
				} else if(arg.equals(INPUT_OPTION)) {
					input = new CSVReader(new FileReader(value));
				} else if(arg.equals(LOG_FILE_OPTION)) {
					logFile = new File(value);
				} else if(arg.equals(LOG_STDOUT_OPTION)) {
					logStdout = true;
				} else if(arg.equals(OUTPUT_FORMAT_OPTION)) {
					if(validOutputFormats.contains(value)) {
						outputFormat = value;
					} else {
						throw new IllegalArgumentException();
					}
				} else if(arg.equals(OUTPUT_FILE_OPTION)) {
					outputFile = new File(value);
				} else if(arg.equals(NO_OUTPUT_FILE_OPTION)) {
					noOutputFile = true;
				} else if(arg.equals(OUTPUT_STDOUT_OPTION)) {
					outputStdout = true;
				}
			}
		}
		
		
		if(outputStdout == true && outputFormat.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
			throw new IllegalArgumentException();
		}
		if(logStdout) {
			log.register(new SystemLogInterface());
		}
		if(logFile != null) {
			log.register(new FileLogInterface(logFile));
		}
		if(outputFormat.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
			publisher = new SQLPublisher(
					new JDBCSQLite(outputFile, log),
					sqlBatchSize);
		} else if(outputFormat.equals(CSV_OUTPUT_FORMAT_VALUE)) {
			//publisher = new CSVPublisher();
		} else if(outputFormat.equals(TAB_OUTPUT_FORMAT_VALUE)) {
			//publisher = new CSVPublisher();
		} else if(outputFormat.equals(FORM_ENCODED_OUTPUT_FORMAT_VALUE)) {
			//publisher = new FormEncodedPublisher();
		}
		client = new Client(regexpCompiler,	
				log,
				netInterface,
				jsonInterface, ENCODING);
	}
	
	private static void scrape() {
		client.scrape(instructionsUri, defaults, publisher);
	}
	
	private static void publish() {
		publisher.forceCommit();
	}
	
	private static void displayUsage() {
		System.out.print(usage);
	}
	
	private static void finish() {
		
	}
}