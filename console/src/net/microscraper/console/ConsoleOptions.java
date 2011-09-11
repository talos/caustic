package net.microscraper.console;

import static net.microscraper.util.StringUtils.NEWLINE;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.microscraper.database.Database;
import net.microscraper.database.NonPersistedDatabase;
import net.microscraper.database.IOConnection;
import net.microscraper.database.MultiTableDatabase;
import net.microscraper.database.SingleTableDatabase;
import net.microscraper.database.csv.CSVConnection;
import net.microscraper.database.sql.JDBCSqliteConnection;
import net.microscraper.deserializer.Deserializer;
import net.microscraper.deserializer.JSONDeserializer;
import net.microscraper.file.JavaIOFileLoader;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.SerializedInstruction;
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
import net.microscraper.util.FormEncodedFormatException;
import net.microscraper.util.JavaNetDecoder;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;
import net.microscraper.util.MapUtils;
import net.microscraper.util.StringUtils;
import net.microscraper.uuid.IntUUIDFactory;
import net.microscraper.uuid.JavaUtilUUIDFactory;
import net.microscraper.uuid.UUIDFactory;

public final class ConsoleOptions {
	public static final String TIMESTAMP_STR = "yyyyMMddkkmmss";
	public static final String TIMESTAMP = new SimpleDateFormat(TIMESTAMP_STR).format(new Date());

	public static final String INSTRUCTION = "--instruction";
	private final Option instruction = Option.withoutDefault(INSTRUCTION);
	
	public static final String BATCH_SIZE = "--batch-size";
	public static final String BATCH_SIZE_DEFAULT = "100";
	private final Option batchSize = Option.withDefault(BATCH_SIZE, BATCH_SIZE_DEFAULT);
	
	public static final String INPUT = "--input";
	public static final String INPUT_DEFAULT = "";
	private final Option input = Option.withDefault(INPUT, INPUT_DEFAULT);
	
	public static final String INPUT_FILE = "--input-file";
	private final Option inputFile = Option.withoutDefault(INPUT_FILE);

	public static final char TAB_DELIMITER = '\t';
	public static final char COMMA_DELIMITER = ',';
	
	public static final String INPUT_DELIMITER = "--delimiter";
	public static final String INPUT_DELIMITER_DEFAULT = Character.toString(COMMA_DELIMITER);
	private final Option inputDelimiter = Option.withDefault(INPUT_DELIMITER, INPUT_DELIMITER_DEFAULT);	
	
	public static final String LOG_TO_FILE = "--log-to-file";
	public static final String LOG_TO_FILE_DEFAULT = TIMESTAMP + ".log";	
	private final Option logToFile = Option.withDefault(LOG_TO_FILE, LOG_TO_FILE_DEFAULT);
	
	public static final String LOG_STDOUT = "--log-stdout";
	private final Option logStdout = Option.withoutDefault(LOG_STDOUT);
	
	public static final String MAX_RESPONSE_SIZE = "--max-response-size";
	public static final String MAX_RESPONSE_SIZE_DEFAULT = Integer.toString(HttpBrowser.DEFAULT_MAX_RESPONSE_SIZE);
	private final Option maxResponseSize = Option.withDefault(MAX_RESPONSE_SIZE, MAX_RESPONSE_SIZE_DEFAULT);
	
	public static final String ENCODING = "--encoding";
	public static final String ENCODING_DEFAULT  = Encoder.UTF_8;
	private final Option encoding = Option.withDefault(ENCODING, ENCODING_DEFAULT);
	
	public static final String CSV_FORMAT = "csv";
	public static final String TAB_FORMAT = "tab";
	public static final String SQLITE_FORMAT = "sqlite";
	
	public static final String FORMAT_DEFAULT = TAB_FORMAT;
	public static final String FORMAT = "--format";
	private final Option format = Option.withDefault(FORMAT, FORMAT_DEFAULT);
	public static final List<String> validOutputFormats = Arrays.asList(
			CSV_FORMAT,
			TAB_FORMAT,
			SQLITE_FORMAT
		);
	
	public static final String SAVE_TO_FILE = "--save-to-file";
	public static final String SAVE_TO_FILE_DEFAULT = TIMESTAMP;
	private final Option saveToFile = Option.withDefault(SAVE_TO_FILE, TIMESTAMP);
	
	public static final String RATE_LIMIT = "--rate-limit";
	public static final String RATE_LIMIT_DEFAULT = Integer.toString(RateLimitManager.DEFAULT_RATE_LIMIT);
	private final Option rateLimit = Option.withDefault(RATE_LIMIT, RATE_LIMIT_DEFAULT);
	
	public static final String REQUEST_WAIT = "--request-wait";
	public static final String REQUEST_WAIT_DEFAULT = Integer.toString(RateLimitManager.DEFAULT_REQUEST_WAIT);
	private final Option requestWait = Option.withDefault(REQUEST_WAIT, REQUEST_WAIT_DEFAULT);
	
	public static final String SINGLE_TABLE = "--single-table";
	private final Option singleTable = Option.withoutDefault(SINGLE_TABLE);
	
	public static final String SOURCE = "--source";
	public static final String SOURCE_DEFAULT = "";
	private final Option source = Option.withDefault(SOURCE, SOURCE_DEFAULT);
	
	public static final String THREADS = "--threads";
	public static final String THREADS_DEFAULT = "3";
	private final Option threads = Option.withDefault(THREADS, THREADS_DEFAULT);
	
	public static final String TIMEOUT_MILLISECONDS = "--timeout";
	private final Option timeoutMilliseconds = Option.withDefault(TIMEOUT_MILLISECONDS, Integer.toString(HttpRequester.DEFAULT_TIMEOUT_MILLISECONDS));
	
	public static final String USAGE = 
"usage: microscraper <uri> [<options>]" + NEWLINE +
"       microscraper <json> [<options>]" + NEWLINE + NEWLINE +
"  uri" + NEWLINE +
"    A URI that points to microscraper instruction JSON." + NEWLINE + NEWLINE +
"  json" + NEWLINE +
"    Microscraper instruction JSON." + NEWLINE + NEWLINE +
"  options" + NEWLINE +
"    " + BATCH_SIZE + "=<batch-size>" + NEWLINE +
"        If saving to SQL, assigns the batch size.  " + NEWLINE +
"        Defaults to " + BATCH_SIZE_DEFAULT  + "." + NEWLINE + 
"    " + ENCODING + "=<encoding>" + NEWLINE +
"        What encoding should be used.  Defaults to " + StringUtils.quote(ENCODING_DEFAULT) + "." + NEWLINE +
"    " + INPUT + "=\"<form-encoded-name-value-pairs>\"" + NEWLINE +
"        A form-encoded string of name value pairs to use as" + NEWLINE +
"        a single input during execution." + NEWLINE +
"    " + INPUT_FILE + "=<path> [" + INPUT_DELIMITER + "=<delimiter>]" + NEWLINE +
"        Path to a file with any number of additional input" + NEWLINE +
"        values.  Each row is executed separately.  The first" + NEWLINE +
"        row contains column names." + NEWLINE +
"        The default column delimiter is "+ StringUtils.quote(INPUT_DELIMITER_DEFAULT) + "." + NEWLINE +
"    " + LOG_TO_FILE + "[=<path>]" + NEWLINE +
"        Pipe the log to a file." + NEWLINE +
"        Path is optional, defaults to " + StringUtils.quote(LOG_TO_FILE_DEFAULT)  + " in the" + NEWLINE +
"        current directory." + NEWLINE +
"    " + LOG_STDOUT + NEWLINE +
"        Pipe the log to stdout." + NEWLINE +
"    " + MAX_RESPONSE_SIZE + NEWLINE +
"        How many KB of a response to load from a single request " + NEWLINE +
"        before cutting off the response.  Defaults to " + MAX_RESPONSE_SIZE_DEFAULT + "KB." + NEWLINE +
"    " + FORMAT + "=(" + StringUtils.join(validOutputFormats.toArray(new String[0]), "|") +")" + NEWLINE +
"        How to format output.  Defaults to " + StringUtils.quote(FORMAT_DEFAULT) + "." + NEWLINE +
"    " + SAVE_TO_FILE + "[=<path>], " + NEWLINE +
"        Where to save the output.  Defaults to " + StringUtils.quote(SAVE_TO_FILE_DEFAULT + ".<format>" + NEWLINE +
"        in the current directory output." + NEWLINE +
"    " + RATE_LIMIT + "=<max-kbps>" + NEWLINE +
"        The rate limit, in KBPS, for loading from a single host." + NEWLINE +
"        Defaults to " + StringUtils.quote(RATE_LIMIT_DEFAULT) + " KBPS." + NEWLINE +
"    " + REQUEST_WAIT + "=<request-wait-milliseconds>" + NEWLINE +
"        How many milliseconds to wait before placing a second " + NEWLINE +
"        request with a single host.  Defaults to " + REQUEST_WAIT_DEFAULT + "ms." + NEWLINE +
"    " + SINGLE_TABLE + NEWLINE +
"        Save all results to a single sqlite table, if using sqlite" + NEWLINE +
"    " + SOURCE + "=<source>" + NEWLINE +
"        A string to use as source for the instruction." + NEWLINE +
"        Only Finds use sources." + NEWLINE +
"    " + THREADS + "=<num-threads>" + NEWLINE +
"        How many threads to use.  Each thread runs one " + NEWLINE +
"        row of input for one instruction." + NEWLINE +
"    " + TIMEOUT_MILLISECONDS + "=<timeout>" + NEWLINE +
"        How many milliseconds to wait before giving up on a" + NEWLINE + 
"        request.  Defaults to " + TIMEOUT_MILLISECONDS + " milliseconds.");
	
	public static final String INSTRUCTION_MISSING_ERROR =
			"Must provide an instruction as JSON or a link to an " +
			"instruction by URI.";
	
	private final List<Option> specifiedOptions = new ArrayList<Option>();
	
	/**
	 * 
	 * @param option The {@link Option} to check.
	 * @return <code>True</code> if the user specified {@link Option}, <code>
	 * false</code> otherwise.
	 */
	private boolean isSpecified(Option option) {
		return specifiedOptions.contains(option);
	}
	
	/**
	 * 
	 * @param option The {@link Option} whose value should be retrieved.
	 * @return The {@link String} value of the {@link Option}.
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
			throw new InvalidOptionException(INSTRUCTION_MISSING_ERROR);
		}
		
		instruction.setValue(args[0]);
		specifiedOptions.add(instruction);
		
		for(int i = 1 ; i < args.length ; i ++) {
			String arg = args[i];
			String value = null;
			if(arg.indexOf('=') > -1) {
				value = arg.substring(arg.indexOf('=') + 1);
				arg = arg.substring(0, arg.indexOf('='));
			}
			Option option = Option.retrieve(arg);
			specifiedOptions.add(option);
			if(value != null) {
				option.setValue(value);
			}
		}
	}
	
	/**
	 * 
	 * @return A {@link PersistedDatabase} based off the user-passed {@link ConsoleOptions}.
	 * @throws InvalidOptionException if the user specified a {@link PersistedDatabase} related
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
		
		 // TODO: reimplement saving files!
		 
		if(isSpecified(saveToFile)) {
			String outputLocation = getValue(saveToFile);
			if(outputLocation.equals(saveToFile.getDefault())) { 
				outputLocation += '.' + format;
			}
			if(format.equals(SQLITE_FORMAT)) {				
				IOConnection connection = JDBCSqliteConnection.toFile(outputLocation, batchSize);
				UUIDFactory idFactory = new JavaUtilUUIDFactory();
				if(isSpecified(singleTable)) {
					result = new SingleTableDatabase(connection, idFactory);
				} else {
					result = new MultiTableDatabase(connection, idFactory);
				}
				throw new InvalidOptionException("SQL temporarily disabled");
				
			} else {
				result = new NonPersistedDatabase(
						CSVConnection.toFile(outputLocation, delimiter), new IntUUIDFactory());
				
			}
		} else { // output to STDOUT
			result = new NonPersistedDatabase(
					CSVConnection.toSystemOut(delimiter), new IntUUIDFactory());
		}
		
		if(isSpecified(this.batchSize) && !format.equals(SQLITE_FORMAT)) {
			throw new InvalidOptionException("Should only specify " + BATCH_SIZE + " when " +
					" outputting to " + SQLITE_FORMAT);
		}
		return result;
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
	 * @throws UnsupportedEncodingException 
	 */
	public Instruction getInstruction() throws InvalidOptionException, UnsupportedEncodingException {
		HttpRequester requester = new JavaNetHttpRequester();

		// Set timeout.
		try {
			int timeout = Integer.parseInt(getValue(timeoutMilliseconds));
			if(timeout < 1) {
				throw new InvalidOptionException(TIMEOUT_MILLISECONDS + " must be greater than 0.");
			}
			requester.setTimeout(timeout);
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(TIMEOUT_MILLISECONDS + " must be an integer");
		}
		
		RateLimitManager memory = new RateLimitManager(new JavaNetHttpUtils());

		// Set rate limit.
		try {
			int limit = Integer.parseInt(getValue(rateLimit));
			if(limit < 1) {
				throw new InvalidOptionException(RATE_LIMIT + " must be greater than 0.");
			}
			memory.setRateLimit(limit);
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(RATE_LIMIT + " must be an integer");
		}
		
		// Set request wait.
		try {
			int wait = Integer.parseInt(getValue(requestWait));
			if(wait < 0) {
				throw new InvalidOptionException(REQUEST_WAIT + " must be greater or equal to 0.");
			}
			memory.setMinRequestWait(wait);
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(REQUEST_WAIT + " must be an integer");
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
		Deserializer deserializer = new JSONDeserializer(parser, compiler, browser, encoder, uriResolver, uriLoader);
		
		String executionDir = new File(StringUtils.USER_DIR).toURI().toString();
		if(!executionDir.endsWith("/")) {
			executionDir += "/";
		}
		
		return new SerializedInstruction(getValue(instruction), deserializer, executionDir);
	}
	
	/**
	 * 
	 * @return An {@link Input} whose elements are {@link Hashtable}s that can be used
	 * as input for {@link Scraper}.
	 */
	public Input getInput() throws InvalidOptionException, UnsupportedEncodingException {
		String rawInputString = getValue(input);
		if(rawInputString.startsWith("\"") && rawInputString.endsWith("\"")) {
			rawInputString = rawInputString.substring(1, rawInputString.length() - 1);
		}
		
		try {
			Map<String, String> shared =
					MapUtils.fromFormEncoded(new JavaNetDecoder(Decoder.UTF_8), rawInputString);
			
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
		} catch(FormEncodedFormatException e) {
			throw new InvalidOptionException(e.getMessage());
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
	
	/**
	 * 
	 * @return An fixed thread pool {@link ExecutorService} with a user-defined number of threads.
	 * @throws InvalidOptionException if an invalid {@link #threads} option was passed.
	 */
	public ScraperRunner getScraperRunner() throws InvalidOptionException {
		try {
			int numThreads = Integer.valueOf(getValue(threads));
			if(numThreads < 1) {
				throw new InvalidOptionException("Must have at least one thread.");
			}
			return new ScraperRunner(Executors.newFixedThreadPool(numThreads));
		} catch(NumberFormatException e) {
			throw new InvalidOptionException(THREADS + " must be an integer.");
		}
	}
}
