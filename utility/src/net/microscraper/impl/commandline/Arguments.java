package net.microscraper.impl.commandline;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import au.com.bytecode.opencsv.CSVReader;

import net.microscraper.client.Deserializer;
import net.microscraper.database.Database;
import net.microscraper.database.DelimitedConnection;
import net.microscraper.database.HashtableDatabase;
import net.microscraper.database.JDBCSqliteConnection;
import net.microscraper.database.MultiTableDatabase;
import net.microscraper.database.SingleTableDatabase;
import net.microscraper.database.UpdateableConnection;
import net.microscraper.file.JavaIOFileLoader;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.json.JsonDeserializer;
import net.microscraper.json.JsonMEParser;
import net.microscraper.json.JsonParser;
import net.microscraper.log.JavaIOFileLogger;
import net.microscraper.log.Logger;
import net.microscraper.log.SystemOutLogger;
import net.microscraper.regexp.JavaUtilRegexpCompiler;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.JavaNetURILoader;
import net.microscraper.uri.JavaNetUriResolver;
import net.microscraper.uri.URILoader;
import net.microscraper.uri.UriResolver;
import net.microscraper.util.Decoder;
import net.microscraper.util.Encoder;
import net.microscraper.util.HashtableUtils;
import net.microscraper.util.IntUUIDFactory;
import net.microscraper.util.JavaNetDecoder;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;
import net.microscraper.util.JavaUtilUUIDFactory;
import net.microscraper.util.StringUtils;

public final class Arguments {
	public static final String newline = System.getProperty("line.separator");
	
	public static final String TIMESTAMP = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());

	public static final Option INSTRUCTION = Option.withoutDefault("instruction");
	
	public static final Option BATCH_SIZE = Option.withDefault("batch-size", "100");
	public static final Option INPUT = Option.withDefault("input", "");
	public static final Option INPUT_FILE = Option.withoutDefault("input-file");
	public static final Option INPUT_COLUMN_DELIMITER = Option.withDefault("column-delimiter", ",");	
	public static final Option LOG_TO_FILE = Option.withDefault("log-to-file", TIMESTAMP + ".log");
	public static final Option LOG_STDOUT = Option.withoutDefault("log-stdout");
	public static final Option MAX_RESPONSE_SIZE = Option.withDefault("max-response-size", Integer.toString(HttpBrowser.DEFAULT_MAX_RESPONSE_SIZE));

	public static final String CSV_OUTPUT_FORMAT_VALUE = "csv";
	public static final String TAB_OUTPUT_FORMAT_VALUE = "tab";
	public static final String SQLITE_OUTPUT_FORMAT_VALUE = "sqlite";
	
	public static final Option OUTPUT_FORMAT_OPTION = Option.withDefault("output-format", TAB_OUTPUT_FORMAT_VALUE);
	//public static final String DEFAULT_FILE_OUTPUT_FORMAT = SQLITE_OUTPUT_FORMAT_VALUE;
	//public static final String DEFAULT_STDOUT_OUTPUT_FORMAT = TAB_OUTPUT_FORMAT_VALUE;
	public static final List<String> validOutputFormats = Arrays.asList(
			CSV_OUTPUT_FORMAT_VALUE,
			TAB_OUTPUT_FORMAT_VALUE,
			SQLITE_OUTPUT_FORMAT_VALUE
			);
	
	public static final char TAB_OUTPUT_COLUMN_DELIMITER = '\t';
	public static final char CSV_OUTPUT_COLUMN_DELIMITER = ',';
	
	public static final Option SAVE_TO_FILE = Option.withDefault("save-to-file", TIMESTAMP);
	public static final Option RATE_LIMIT = Option.withDefault("rate-limit", Integer.toString(RateLimitManager.DEFAULT_RATE_LIMIT));
	public static final Option SINGLE_TABLE = Option.withoutDefault("single-table");
	public static final Option TIMEOUT_MILLISECONDS = Option.withDefault("timeout", Integer.toString(HttpRequester.DEFAULT_TIMEOUT_MILLISECONDS));
	
	public static final String USAGE = 
"usage: microscraper <uri> [<options>]" + newline +
"		microscraper <json> [<options>]" + newline +
"" + newline +
"uri" + newline +
"	A URI that points to microscraper instruction JSON." + newline +
"json" + newline +
"	Microscraper instruction JSON." + newline +
"options:" + newline +
"	" + BATCH_SIZE + "=<batch-size>" + newline +
"		If saving to SQL, assigns the batch size.  " + newline +
"		Defaults to " + StringUtils.quote(BATCH_SIZE.getDefault()) + newline +
"	" + INPUT + "=\"<defaults>\"" + newline +
"		A form-encoded string of name value pairs to use as" + newline +
"		a single input during execution." + newline +
"	" + INPUT_FILE + "=<path> [" + INPUT_COLUMN_DELIMITER + "=<delimiter>]" + newline +
"		Path to a file with any number of additional input" + newline +
"		values.  Each row is executed separately.  The first" + newline +
"		row contains column names." + newline +
"		The default column delimiter is "+ StringUtils.quote(INPUT_FILE.getDefault()) + "." + newline +
"	" + LOG_TO_FILE + "[=<path>]" + newline +
"		Pipe the log to a file." + newline +
"		Path is optional, defaults to " + StringUtils.quote(LOG_TO_FILE.getDefault())  + " in the" + newline +
"		current directory." + newline +
"	" + LOG_STDOUT + newline +
"		Pipe the log to stdout." + newline +
"	" + MAX_RESPONSE_SIZE + newline +
"		How many KB of a response to load from a single request before " + newline +
"		cutting off the response.  Defaults to " + StringUtils.quote(MAX_RESPONSE_SIZE.getDefault()) + "KB." + newline +
"	" + OUTPUT_FORMAT_OPTION + "=(" + StringUtils.join(validOutputFormats.toArray(new String[0]), "|") +")" + newline +
"		How to format output.  Defaults to " + StringUtils.quote(OUTPUT_FORMAT_OPTION.getDefault()) + "." + newline +
"	" + SAVE_TO_FILE + "[=<path>], " + newline +
"		Where to save the output.  Defaults to " +
		StringUtils.quote(SAVE_TO_FILE.getDefault()) + ".<format> in" + newline +
"		the current directory output." + newline +
"	" + RATE_LIMIT + "=<max-kbps>" + newline +
"		The rate limit, in KBPS, for loading from a single host." + newline +
"		Defaults to " + StringUtils.quote(RATE_LIMIT.getDefault()) + " KBPS." + newline +
"	" + SINGLE_TABLE + newline +
"		Save all results to a single sqlite table, if using sqlite" + newline +
"	" + TIMEOUT_MILLISECONDS + "=<timeout>" + newline +
"		How many milliseconds to wait before giving up on a request." + newline + 
"		Defaults to " + StringUtils.quote(TIMEOUT_MILLISECONDS.getDefault()) + " milliseconds.";
	
	private final Map<Option, String> arguments = new HashMap<Option, String>();

	
	private boolean has(Option option) {
		return arguments.containsKey(option);
	}
	
	private String get(Option option) throws ArgumentsException {
		if(option.hasDefault() && arguments.get(option) == null) {
			return option.getDefault();
		} else if(arguments.get(option) != null) {
			return arguments.get(option);
		} else {
			throw new ArgumentsException("Did not define value for " + StringUtils.quote(option));
		}
	}
	
	public Arguments(String[] args) throws ArgumentsException {
		if(args.length == 0) {
			throw new ArgumentsException("Must have at least one argument.");
		}
		
		arguments.put(INSTRUCTION, args[0]);
		
		for(int i = 1 ; i < args.length ; i ++) {
			String arg = args[i];
			String value = null;
			if(arg.indexOf('=') > -1) {
				value = arg.substring(arg.indexOf('=') + 1);
				arg = arg.substring(0, arg.indexOf('='));
			}
			arguments.put(Option.retrieve(arg), value);
		}
		
		// Fix quotations on default values.
		String possiblyQuotedDefaults = get(INPUT);
		if(possiblyQuotedDefaults.startsWith("\"") && possiblyQuotedDefaults.endsWith("\"")) {
			arguments.put(INPUT, possiblyQuotedDefaults.substring(1, possiblyQuotedDefaults.length() - 2));
		}
	}
	
	public Deserializer getDeserializer() throws ArgumentsException, UnsupportedEncodingException {				
		//this.args = args;
		final int rateLimit;
		final int timeout;
		
		// Set rate limit.
		try {
			rateLimit = Integer.parseInt(get(RATE_LIMIT));
		} catch(NumberFormatException e) {
			throw new ArgumentsException(RATE_LIMIT + " must be an integer");
		}
		
		// Set timeout.
		try {
			timeout = Integer.parseInt(get(TIMEOUT_MILLISECONDS));
		} catch(NumberFormatException e) {
			throw new ArgumentsException(TIMEOUT_MILLISECONDS + " must be an integer");
		}
		
		HttpRequester requester = new JavaNetHttpRequester();
		requester.setTimeout(timeout);
		
		RateLimitManager memory = new RateLimitManager(new JavaNetHttpUtils(), rateLimit);
		
		HttpBrowser browser = new HttpBrowser(new JavaNetHttpRequester(),
				memory, new JavaNetCookieManager());
		
		
		RegexpCompiler compiler = new JavaUtilRegexpCompiler();
		URILoader uriLoader = new JavaNetURILoader(browser, new JavaIOFileLoader());
		UriResolver uriResolver = new JavaNetUriResolver();
		JsonParser parser = new JsonMEParser();
		Encoder encoder = new JavaNetEncoder(Encoder.UTF_8);
		Deserializer deserializer = new JsonDeserializer(parser, compiler, browser, encoder, uriResolver, uriLoader);
		
		return deserializer;
	
	}
	
	public Database getDatabase() throws ArgumentsException {
		final Database result;
		
		// Determine format.
		String format;
		format = get(OUTPUT_FORMAT_OPTION);
		if(!validOutputFormats.contains(format)) {
			throw new ArgumentsException(StringUtils.quote(format)
					+ " is not a valid output format.");
		}
			
		// Determine delimiter.
		char delimiter;
		if(format.equals(CSV_OUTPUT_FORMAT_VALUE)) {
			delimiter = CSV_OUTPUT_COLUMN_DELIMITER;
		} else { // (format.equals(TAB_OUTPUT_COLUMN_DELIMITER)) {
			delimiter = TAB_OUTPUT_COLUMN_DELIMITER;
		}
		
		// Set up output and databases.
		if(has(SAVE_TO_FILE)) {
			String outputLocation = get(SAVE_TO_FILE);
			if(outputLocation.equals(SAVE_TO_FILE.getDefault())) { // append appropriate format for default
				outputLocation += '.' + format;
			}
			if(format.equals(SQLITE_OUTPUT_FORMAT_VALUE)) {
				
				int batchSize = Integer.parseInt(get(BATCH_SIZE));
				Database backing = new HashtableDatabase(new JavaUtilUUIDFactory());
				
				UpdateableConnection connection = JDBCSqliteConnection.toFile(outputLocation, batchSize);
				if(has(SINGLE_TABLE)) {
					result = new SingleTableDatabase(backing, connection);
				} else {
					result = new MultiTableDatabase(backing, connection);
				}
				
			} else {
				result = new SingleTableDatabase(
						new HashtableDatabase(new IntUUIDFactory()), DelimitedConnection.toFile(outputLocation, delimiter));
			}
			
		} else { // output to STDOUT
			result = new SingleTableDatabase(new HashtableDatabase(new IntUUIDFactory()), DelimitedConnection.toSystemOut(delimiter));
		}
		return result;
	}
	
	/**
	 * 
	 * @return The {@link String} path to the directory where the user is executing.
	 */
	public String getExecutionDir() throws ArgumentsException {
		String executionDir = new File(System.getProperty("user.dir")).toURI().toString();
		if(!executionDir.endsWith("/")) {
			executionDir += "/";
		}
		return executionDir;
	}
	
	
	/**
	 * 
	 * @return A {@link List} of loggers that should be used.
	 */
	public List<Logger> getLoggers() throws ArgumentsException {
		List<Logger> loggers = new ArrayList<Logger>();
		if(has(LOG_TO_FILE)) {
			loggers.add(new JavaIOFileLogger(get(LOG_TO_FILE)));
		}
		if(has(LOG_STDOUT)) {
			loggers.add(new SystemOutLogger());
		}
		return loggers;
	}
	
	/**
	 * 
	 * @return The serialized instruction {@link String}.
	 * @throws ArgumentsException
	 */
	public String getInstruction() throws ArgumentsException {
		return get(INSTRUCTION);
	}
	
	/**
	 * 
	 * @return An {@link Input} whose elements are {@link Hashtable}s that can be used
	 * as input for {@link Microscraper}.
	 */
	public Input getInput() throws ArgumentsException, UnsupportedEncodingException {
		Hashtable<String, String> shared =
				HashtableUtils.fromFormEncoded(new JavaNetDecoder(Decoder.UTF_8), get(INPUT));
		
		if(has(INPUT_FILE)) {
			if(get(INPUT_COLUMN_DELIMITER).length() > 1) {
				throw new ArgumentsException(INPUT_COLUMN_DELIMITER + " must be a single character.");
			}
			char inputColumnDelimiter = get(INPUT_COLUMN_DELIMITER).charAt(0);
			return new Input(shared, get(INPUT_FILE), inputColumnDelimiter);
		} else {
			return new Input(shared);
		}
	}
}
