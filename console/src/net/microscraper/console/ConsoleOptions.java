package net.microscraper.console;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

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

public final class ConsoleOptions {
	public static final String newline = System.getProperty("line.separator");
	
	public static final String TIMESTAMP_STR = "yyyyMMddkkmmss";
	public static final String TIMESTAMP = new SimpleDateFormat(TIMESTAMP_STR).format(new Date());

	private final Option INSTRUCTION = Option.withoutDefault("instruction");
	
	private final Option BATCH_SIZE = Option.withDefault("batch-size", "100");
	private final Option INPUT = Option.withDefault("input", "");
	private final Option INPUT_FILE = Option.withoutDefault("input-file");
	private final Option INPUT_DELIMITER = Option.withDefault("delimiter", ",");	
	private final Option LOG_TO_FILE = Option.withDefault("log-to-file", TIMESTAMP + ".log");
	private final Option LOG_STDOUT = Option.withoutDefault("log-stdout");
	private final Option MAX_RESPONSE_SIZE = Option.withDefault("max-response-size", Integer.toString(HttpBrowser.DEFAULT_MAX_RESPONSE_SIZE));
	private final Option ENCODING = Option.withDefault("encoding", Encoder.UTF_8);
	
	public static final String CSV_FORMAT = "csv";
	public static final String TAB_FORMAT = "tab";
	public static final String SQLITE_FORMAT = "sqlite";
	
	private final Option FORMAT = Option.withDefault("format", TAB_FORMAT);
	public static final List<String> validOutputFormats = Arrays.asList(
			CSV_FORMAT,
			TAB_FORMAT,
			SQLITE_FORMAT
		);
	
	public static final char TAB_DELIMITER = '\t';
	public static final char COMMA_DELIMITER = ',';
	
	private final Option SAVE_TO_FILE = Option.withDefault("save-to-file", TIMESTAMP);
	private final Option RATE_LIMIT = Option.withDefault("rate-limit", Integer.toString(RateLimitManager.DEFAULT_RATE_LIMIT));
	private final Option SINGLE_TABLE = Option.withoutDefault("single-table");
	private final Option TIMEOUT_MILLISECONDS = Option.withDefault("timeout", Integer.toString(HttpRequester.DEFAULT_TIMEOUT_MILLISECONDS));
	
	private final String usage = 
"usage: microscraper <uri> [<options>]" + newline +
"       microscraper <json> [<options>]" + newline + newline +
"  uri" + newline +
"    A URI that points to microscraper instruction JSON." + newline + newline +
"  json" + newline +
"    Microscraper instruction JSON." + newline + newline +
"  options" + newline +
"    " + BATCH_SIZE + "=<batch-size>" + newline +
"        If saving to SQL, assigns the batch size.  " + newline +
"        Defaults to " + BATCH_SIZE.getValue()  + "." + newline + 
"    " + ENCODING + "=<encoding>" + newline +
"        What encoding should be used.  Defaults to " + StringUtils.quote(ENCODING.getValue()) + "." + newline +
"    " + INPUT + "=\"<defaults>\"" + newline +
"        A form-encoded string of name value pairs to use as" + newline +
"        a single input during execution." + newline +
"    " + INPUT_FILE + "=<path> [" + INPUT_DELIMITER + "=<delimiter>]" + newline +
"        Path to a file with any number of additional input" + newline +
"        values.  Each row is executed separately.  The first" + newline +
"        row contains column names." + newline +
"        The default column delimiter is "+ StringUtils.quote(INPUT_DELIMITER.getValue()) + "." + newline +
"    " + LOG_TO_FILE + "[=<path>]" + newline +
"        Pipe the log to a file." + newline +
"        Path is optional, defaults to " + StringUtils.quote(LOG_TO_FILE.getValue())  + " in the" + newline +
"        current directory." + newline +
"    " + LOG_STDOUT + newline +
"        Pipe the log to stdout." + newline +
"    " + MAX_RESPONSE_SIZE + newline +
"        How many KB of a response to load from a single request " + newline +
"        before cutting off the response.  Defaults to " + MAX_RESPONSE_SIZE.getValue() + "KB." + newline +
"    " + FORMAT + "=(" + StringUtils.join(validOutputFormats.toArray(new String[0]), "|") +")" + newline +
"        How to format output.  Defaults to " + StringUtils.quote(FORMAT.getValue()) + "." + newline +
"    " + SAVE_TO_FILE + "[=<path>], " + newline +
"        Where to save the output.  Defaults to " + StringUtils.quote(SAVE_TO_FILE.getValue()) + ".<format>" + newline +
"        in the current directory output." + newline +
"    " + RATE_LIMIT + "=<max-kbps>" + newline +
"        The rate limit, in KBPS, for loading from a single host." + newline +
"        Defaults to " + StringUtils.quote(RATE_LIMIT.getValue()) + " KBPS." + newline +
"    " + SINGLE_TABLE + newline +
"        Save all results to a single sqlite table, if using sqlite" + newline +
"    " + TIMEOUT_MILLISECONDS + "=<timeout>" + newline +
"        How many milliseconds to wait before giving up on a" + newline + 
"        request.  Defaults to " + StringUtils.quote(TIMEOUT_MILLISECONDS.getValue()) + " milliseconds.";
	
	private final List<Option> definedOptions = new ArrayList<Option>();
	//private final Map<Option, String> optionValues = new HashMap<Option, String>();

	/**
	 * 
	 * @param option The {@link Option} to check.
	 * @return <code>True</code> if the user specified {@link Option}, <code>
	 * false</code> otherwise.
	 */
	private boolean isSpecified(Option option) {
		return definedOptions.contains(option);
	}
	
	/**
	 * 
	 * @param option The {@link Option} whose value should be retrieved.
	 * @return The {@link String} value of the {@link Option}.  If it is
	 * not {@link #isSpecified(Option)}, then {@link Option#getDefault()}
	 * is returned.
	 * @throws InvalidOptionException If the user did not specify this 
	 * {@link Option} and it does not have a default value.
	 */
	private String getValue(Option option) throws InvalidOptionException {
		if(isSpecified(option)) {
			return option.getValue();
		} else {
			throw new InvalidOptionException("Did not define value for " + StringUtils.quote(option));
		}
	}
	
	/**
	 * Instantiate {@link ConsoleOptions} with an array of strings from a main
	 * function.
	 * @param args A {@link String} array.
	 * @throws InvalidOptionException If there were no options passed in <code>
	 * args</code>, or if there was an unknown option passed.
	 */
	public ConsoleOptions(String[] args) throws InvalidOptionException {
		if(args.length == 0) {
			throw new InvalidOptionException(
					"Must provide an instruction as JSON or a link to an " +
					"instruction by URI.");
		}
		
		INSTRUCTION.define(args[0]);
		definedOptions.add(INSTRUCTION);
		
		for(int i = 1 ; i < args.length ; i ++) {
			String arg = args[i];
			String value = null;
			if(arg.indexOf('=') > -1) {
				value = arg.substring(arg.indexOf('=') + 1);
				arg = arg.substring(0, arg.indexOf('='));
			}
			Option.retrieve(arg).define(value);
		}
	}
	
	/**
	 * 
	 * @return A {@link Deserializer} based off the user-passed {@link ConsoleOptions}.
	 * @throws InvalidOptionException if the user specified a {@link Deserializer} related
	 * option that is invalid.
	 * @throws UnsupportedEncodingException if the specified {@link #ENCODING} is
	 * not supported.
	 */
	public Deserializer getDeserializer()
				throws InvalidOptionException, UnsupportedEncodingException {				
		final int rateLimit;
		final int timeout;
		
		// Set rate limit.
		try {
			rateLimit = Integer.parseInt(getValue(RATE_LIMIT));
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(RATE_LIMIT + " must be an integer");
		}
		
		// Set timeout.
		try {
			timeout = Integer.parseInt(getValue(TIMEOUT_MILLISECONDS));
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(TIMEOUT_MILLISECONDS + " must be an integer");
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
	
	/**
	 * 
	 * @return A {@link Database} based off the user-passed {@link ConsoleOptions}.
	 * @throws InvalidOptionException if the user specified a {@link Database} related
	 * option that is invalid.
	 */
	public Database getDatabase() throws InvalidOptionException {
		final Database result;
		
		// Determine format.
		String format;
		format = getValue(FORMAT);
		if(!validOutputFormats.contains(format)) {
			throw new InvalidOptionException(StringUtils.quote(format)
					+ " is not a valid output format.");
		}
			
		// Determine delimiter.
		char delimiter;
		if(format.equals(CSV_FORMAT)) {
			delimiter = COMMA_DELIMITER;
		} else { // (format.equals(TAB_OUTPUT_COLUMN_DELIMITER)) {
			delimiter = TAB_DELIMITER;
		}
		
		// Determine batch size.
		int batchSize;
		try {
			batchSize = Integer.parseInt(getValue(BATCH_SIZE));
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(BATCH_SIZE + " must be an integer.");
		}
		
		// Set up output and databases.
		if(isSpecified(SAVE_TO_FILE)) {
			String outputLocation = getValue(SAVE_TO_FILE);
			if(outputLocation.equals(SAVE_TO_FILE.getValue())) { // TODO append appropriate format for default
				outputLocation += '.' + format;
			}
			if(format.equals(SQLITE_FORMAT)) {
				Database backing = new HashtableDatabase(new JavaUtilUUIDFactory());
				
				UpdateableConnection connection = JDBCSqliteConnection.toFile(outputLocation, batchSize);
				if(isSpecified(SINGLE_TABLE)) {
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
		
		if(isSpecified(BATCH_SIZE) && !format.equals(SQLITE_FORMAT)) {
			throw new InvalidOptionException("Should only specify " + BATCH_SIZE + " when " +
					" outputting to " + SQLITE_FORMAT);
		}
		return result;
	}
	
	/**
	 * 
	 * @return The {@link String} path to the directory where the user is executing.
	 */
	public String getExecutionDir() throws InvalidOptionException {
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
	public List<Logger> getLoggers() throws InvalidOptionException {
		List<Logger> loggers = new ArrayList<Logger>();
		if(isSpecified(LOG_TO_FILE)) {
			loggers.add(new JavaIOFileLogger(getValue(LOG_TO_FILE)));
		}
		if(isSpecified(LOG_STDOUT)) {
			loggers.add(new SystemOutLogger());
		}
		return loggers;
	}
	
	/**
	 * 
	 * @return The serialized instruction {@link String}.
	 * @throws InvalidOptionException
	 */
	public String getInstruction() throws InvalidOptionException {
		return getValue(INSTRUCTION);
	}
	
	/**
	 * 
	 * @return An {@link Input} whose elements are {@link Hashtable}s that can be used
	 * as input for {@link Microscraper}.
	 */
	public Input getInput() throws InvalidOptionException, UnsupportedEncodingException {
		@SuppressWarnings("unchecked")
		String rawInputString = getValue(INPUT);
		if(rawInputString.startsWith("\"") && rawInputString.endsWith("\"")) {
			rawInputString = rawInputString.substring(1, rawInputString.length() - 1);
		}
		Hashtable<String, String> shared =
				HashtableUtils.fromFormEncoded(new JavaNetDecoder(Decoder.UTF_8), rawInputString);
		
		if(isSpecified(INPUT_FILE)) {
			char inputColumnDelimiter = getValue(INPUT_DELIMITER).charAt(0);
			if(getValue(INPUT_DELIMITER).length() > 1) {
				throw new InvalidOptionException(INPUT_DELIMITER + " must be a single character.");
			}
			return Input.fromSharedAndCSV(shared, getValue(INPUT_FILE), inputColumnDelimiter);
		} else {
			if(isSpecified(INPUT_DELIMITER)) {
				throw new InvalidOptionException("Cannot define " + INPUT_DELIMITER + " without an input file.");
			}
			return Input.fromShared(shared);
		}
	}

	public String getUsage() {
		return usage;
	}
}
