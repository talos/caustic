package net.microscraper.console;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
import net.microscraper.instruction.Find;
import net.microscraper.json.JsonDeserializer;
import net.microscraper.json.JsonMEParser;
import net.microscraper.json.JsonParser;
import net.microscraper.log.JavaIOFileLogger;
import net.microscraper.log.Logger;
import net.microscraper.log.MultiLog;
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

	public static final String INSTRUCTION = "instruction";
	private final Option instruction = Option.withoutDefault(INSTRUCTION);
	
	public static final String BATCH_SIZE = "batch-size";
	public static final String BATCH_SIZE_DEFAULT = "100";
	private final Option batchSize = Option.withDefault(BATCH_SIZE, BATCH_SIZE_DEFAULT);
	
	public static final String INPUT = "input";
	public static final String INPUT_DEFAULT = "";
	private final Option input = Option.withDefault(INPUT, INPUT_DEFAULT);
	
	public static final String INPUT_FILE = "input-file";
	private final Option inputFile = Option.withoutDefault(INPUT_FILE);

	public static final char TAB_DELIMITER = '\t';
	public static final char COMMA_DELIMITER = ',';
	
	public static final String INPUT_DELIMITER = "delimiter";
	public static final String INPUT_DELIMITER_DEFAULT = Character.toString(COMMA_DELIMITER);
	private final Option inputDelimiter = Option.withDefault(INPUT_DELIMITER, INPUT_DELIMITER_DEFAULT);	
	
	public static final String LOG_TO_FILE = "log-to-file";
	public static final String LOG_TO_FILE_DEFAULT = TIMESTAMP + ".log";	
	private final Option logToFile = Option.withDefault(LOG_TO_FILE, LOG_TO_FILE_DEFAULT);
	
	public static final String LOG_STDOUT = "log-stdout";
	private final Option logStdout = Option.withoutDefault(LOG_STDOUT);
	
	public static final String MAX_RESPONSE_SIZE = "max-response-size";
	public static final String MAX_RESPONSE_SIZE_DEFAULT = Integer.toString(HttpBrowser.DEFAULT_MAX_RESPONSE_SIZE);
	private final Option maxResponseSize = Option.withDefault(MAX_RESPONSE_SIZE, MAX_RESPONSE_SIZE_DEFAULT);
	
	public static final String ENCODING = "encoding";
	public static final String ENCODING_DEFAULT  = Encoder.UTF_8;
	private final Option encoding = Option.withDefault(ENCODING, ENCODING_DEFAULT);
	
	public static final String CSV_FORMAT = "csv";
	public static final String TAB_FORMAT = "tab";
	public static final String SQLITE_FORMAT = "sqlite";
	
	public static final String FORMAT_DEFAULT = TAB_FORMAT;
	public static final String FORMAT = "format";
	private final Option format = Option.withDefault(FORMAT, FORMAT_DEFAULT);
	public static final List<String> validOutputFormats = Arrays.asList(
			CSV_FORMAT,
			TAB_FORMAT,
			SQLITE_FORMAT
		);
	
	public static final String SAVE_TO_FILE = "save-to-file";
	public static final String SAVE_TO_FILE_DEFAULT = TIMESTAMP;
	private final Option saveToFile = Option.withDefault(SAVE_TO_FILE, TIMESTAMP);
	
	public static final String RATE_LIMIT = "rate-limit";
	public static final String RATE_LIMIT_DEFAULT = Integer.toString(RateLimitManager.DEFAULT_RATE_LIMIT);
	private final Option rateLimit = Option.withDefault(RATE_LIMIT, RATE_LIMIT_DEFAULT);
	
	public static final String SINGLE_TABLE = "single-table";
	private final Option singleTable = Option.withoutDefault(SINGLE_TABLE);
	
	public static final String SOURCE = "source";
	public static final String SOURCE_DEFAULT = "";
	private final Option source = Option.withDefault(SOURCE, SOURCE_DEFAULT);
	
	public static final String THREADS = "threads";
	public static final String THREADS_DEFAULT = "5";
	private final Option threads = Option.withDefault(THREADS, THREADS_DEFAULT);
	
	public static final String TIMEOUT_MILLISECONDS = "timeout";
	private final Option timeoutMilliseconds = Option.withDefault(TIMEOUT_MILLISECONDS, Integer.toString(HttpRequester.DEFAULT_TIMEOUT_MILLISECONDS));
	
	public static final String USAGE = 
"usage: microscraper <uri> [<options>]" + newline +
"       microscraper <json> [<options>]" + newline + newline +
"  uri" + newline +
"    A URI that points to microscraper instruction JSON." + newline + newline +
"  json" + newline +
"    Microscraper instruction JSON." + newline + newline +
"  options" + newline +
"    " + BATCH_SIZE + "=<batch-size>" + newline +
"        If saving to SQL, assigns the batch size.  " + newline +
"        Defaults to " + BATCH_SIZE_DEFAULT  + "." + newline + 
"    " + ENCODING + "=<encoding>" + newline +
"        What encoding should be used.  Defaults to " + StringUtils.quote(ENCODING_DEFAULT) + "." + newline +
"    " + INPUT + "=\"<defaults>\"" + newline +
"        A form-encoded string of name value pairs to use as" + newline +
"        a single input during execution." + newline +
"    " + INPUT_FILE + "=<path> [" + INPUT_DELIMITER + "=<delimiter>]" + newline +
"        Path to a file with any number of additional input" + newline +
"        values.  Each row is executed separately.  The first" + newline +
"        row contains column names." + newline +
"        The default column delimiter is "+ StringUtils.quote(INPUT_DELIMITER_DEFAULT) + "." + newline +
"    " + LOG_TO_FILE + "[=<path>]" + newline +
"        Pipe the log to a file." + newline +
"        Path is optional, defaults to " + StringUtils.quote(LOG_TO_FILE_DEFAULT)  + " in the" + newline +
"        current directory." + newline +
"    " + LOG_STDOUT + newline +
"        Pipe the log to stdout." + newline +
"    " + MAX_RESPONSE_SIZE + newline +
"        How many KB of a response to load from a single request " + newline +
"        before cutting off the response.  Defaults to " + MAX_RESPONSE_SIZE_DEFAULT + "KB." + newline +
"    " + FORMAT + "=(" + StringUtils.join(validOutputFormats.toArray(new String[0]), "|") +")" + newline +
"        How to format output.  Defaults to " + StringUtils.quote(FORMAT_DEFAULT) + "." + newline +
"    " + SAVE_TO_FILE + "[=<path>], " + newline +
"        Where to save the output.  Defaults to " + StringUtils.quote(SAVE_TO_FILE_DEFAULT + ".<format>" + newline +
"        in the current directory output." + newline +
"    " + RATE_LIMIT + "=<max-kbps>" + newline +
"        The rate limit, in KBPS, for loading from a single host." + newline +
"        Defaults to " + StringUtils.quote(RATE_LIMIT_DEFAULT) + " KBPS." + newline +
"    " + SINGLE_TABLE + newline +
"        Save all results to a single sqlite table, if using sqlite" + newline +
"    " + SOURCE + "=<source>" + newline +
"        A string to use as source for the instruction." + newline +
"        Only Finds use sources." + newline +
"    " + THREADS + "=<num-threads>" + newline +
"        How many threads to use.  Each thread runs one " + newline +
"        row of input for one instruction." + newline +
"    " + TIMEOUT_MILLISECONDS + "=<timeout>" + newline +
"        How many milliseconds to wait before giving up on a" + newline + 
"        request.  Defaults to " + TIMEOUT_MILLISECONDS + " milliseconds.");
	
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
		if(option.getValue() != null) {
			return option.getValue();
		} else {
			throw new InvalidOptionException("Did not define value for " + StringUtils.quote(option.getName()));
		}
	}
	
	/**
	 * Initialize {@link ConsoleOptions} with an array of strings from a main
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
		
		instruction.setValue(args[0]);
		definedOptions.add(instruction);
		
		for(int i = 1 ; i < args.length ; i ++) {
			String arg = args[i];
			String value = null;
			if(arg.indexOf('=') > -1) {
				value = arg.substring(arg.indexOf('=') + 1);
				arg = arg.substring(0, arg.indexOf('='));
			}
			Option option = Option.retrieve(arg);
			if(value != null) {
				option.setValue(value);
			}
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
		HttpRequester requester = new JavaNetHttpRequester();

		// Set timeout.
		try {
			requester.setTimeout(Integer.parseInt(getValue(timeoutMilliseconds)));
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(TIMEOUT_MILLISECONDS + " must be an integer");
		}
		
		RateLimitManager memory = new RateLimitManager(new JavaNetHttpUtils());

		// Set rate limit.
		try {
			memory.setRateLimit(Integer.parseInt(getValue(this.rateLimit)));
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(RATE_LIMIT + " must be an integer");
		}
		
		HttpBrowser browser = new HttpBrowser(requester, memory, new JavaNetCookieManager());
		
		// Set max response size
		try {
			browser.setMaxResponseSize(Integer.parseInt(getValue(maxResponseSize)));
		} catch (NumberFormatException e) {
			throw new InvalidOptionException(MAX_RESPONSE_SIZE + " must be an integer");
		}
		
		RegexpCompiler compiler = new JavaUtilRegexpCompiler();
		URILoader uriLoader = new JavaNetURILoader(browser, new JavaIOFileLoader());
		UriResolver uriResolver = new JavaNetUriResolver();
		JsonParser parser = new JsonMEParser();
		Encoder encoder = new JavaNetEncoder(getValue(encoding));
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
		String format = getValue(this.format);
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
			batchSize = Integer.parseInt(getValue(this.batchSize));
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(BATCH_SIZE + " must be an integer.");
		}
		
		// Set up output and databases.
		if(isSpecified(saveToFile)) {
			String outputLocation = getValue(saveToFile);
			if(outputLocation.equals(saveToFile)) { // TODO append appropriate format for default
				outputLocation += '.' + format;
			}
			if(format.equals(SQLITE_FORMAT)) {
				Database backing = new HashtableDatabase(new JavaUtilUUIDFactory());
				
				UpdateableConnection connection = JDBCSqliteConnection.toFile(outputLocation, batchSize);
				if(isSpecified(singleTable)) {
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
		
		if(isSpecified(this.batchSize) && !format.equals(SQLITE_FORMAT)) {
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
	 * @return A {@link Logger}.
	 */
	public Logger getLogger() throws InvalidOptionException {
		MultiLog multiLog = new MultiLog();
		if(isSpecified(logToFile)) {
			multiLog.register(new JavaIOFileLogger(getValue(logToFile)));
		}
		if(isSpecified(logStdout)) {
			multiLog.register(new SystemOutLogger());
		}
		return multiLog;
	}
	
	/**
	 * 
	 * @return The serialized instruction {@link String}.
	 * @throws InvalidOptionException
	 */
	public String getInstruction() throws InvalidOptionException {
		return getValue(instruction);
	}
	
	/**
	 * 
	 * @return An {@link Input} whose elements are {@link Hashtable}s that can be used
	 * as input for {@link Scraper}.
	 */
	@SuppressWarnings("unchecked")
	public Input getInput() throws InvalidOptionException, UnsupportedEncodingException {
		String rawInputString = getValue(input);
		if(rawInputString.startsWith("\"") && rawInputString.endsWith("\"")) {
			rawInputString = rawInputString.substring(1, rawInputString.length() - 1);
		}
		Hashtable<String, String> shared =
				HashtableUtils.fromFormEncoded(new JavaNetDecoder(Decoder.UTF_8), rawInputString);
		
		if(isSpecified(inputFile)) {
			char inputColumnDelimiter = getValue(inputDelimiter).charAt(0);
			if(getValue(inputDelimiter).length() > 1) {
				throw new InvalidOptionException(INPUT_DELIMITER + " must be a single character.");
			}
			return Input.fromSharedAndCSV(shared, getValue(inputFile), inputColumnDelimiter);
		} else {
			if(isSpecified(inputDelimiter)) {
				throw new InvalidOptionException("Cannot define " + INPUT_DELIMITER + " without an input file.");
			}
			return Input.fromShared(shared);
		}
	}

	/**
	 * 
	 * @return A {@link String} to be used as the source for the executed {@link Instruction}.
	 * Only {@link Find}s use a source string.
	 */
	public String getSource() throws InvalidOptionException {
		return getValue(source);
	}
	
	public Executor getExecutor() throws InvalidOptionException {
		try {
			return Executors.newFixedThreadPool(Integer.valueOf(getValue(threads)));
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(THREADS + " must be an integer.");
		}
	}
}
