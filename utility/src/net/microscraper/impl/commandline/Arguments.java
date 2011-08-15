package net.microscraper.impl.commandline;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.microscraper.client.Browser;
import net.microscraper.util.Utils;

public final class Arguments {
	private static final Map<String, Option> validOptions = new HashMap<String, Option>();
	public static class Option  {
		private static final String PREPEND = "--";
		private final String defaultValue;
		private final String name;
		private Option(String nonPrependedName, String defaultValue) {
			this.name = PREPEND + nonPrependedName;
			this.defaultValue = defaultValue;
			validOptions.put(this.name, this);
		}
		private static Option withoutDefault(String nonPrependedName) {
			return new Option(nonPrependedName, null);
		}
		private static Option withDefault(String nonPrependedName, String defaultValue) {
			return new Option(nonPrependedName, defaultValue);
		}
		private static boolean exists(String name) {
			return validOptions.containsKey(name);
		}
		private static Option retrieve(String name) throws IllegalArgumentException {
			if(exists(name)) {
				return validOptions.get(name);
			} else {
				throw new IllegalArgumentException(name + " is not a valid option.");
			}
		}
		public String toString() {
			return name;
		}
		public boolean hasDefault() {
			return defaultValue != null;
		}
		public String getDefault() {
			return defaultValue;
		}
	}
	
	public static final String newline = System.getProperty("line.separator");
	
	public static final String TIMESTAMP = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());

	public static final Option URI_INSTRUCTION = Option.withoutDefault("uri");
	public static final Option JSON_INSTRUCTION = Option.withoutDefault("json");
	public static final Option BATCH_SIZE = Option.withDefault("batch-size", "100");
	public static final Option DEFAULTS = Option.withDefault("defaults", "");
	public static final Option INPUT = Option.withoutDefault("input");
	public static final Option INPUT_COLUMN_DELIMITER = Option.withDefault("column-delimiter", ",");	
	public static final Option LOG_FILE = Option.withoutDefault("log-file");	
	public static final Option LOG_STDOUT = Option.withoutDefault("log-stdout");
	public static final Option MAX_RESPONSE_SIZE = Option.withDefault("max-response-size", Integer.toString(Browser.DEFAULT_MAX_RESPONSE_SIZE));

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
	
	public static final Option OUTPUT_TO_FILE = Option.withoutDefault("output-to-file");
	public static final Option RATE_LIMIT = Option.withDefault("rate-limit", Integer.toString(Browser.DEFAULT_RATE_LIMIT));
	public static final Option SINGLE_TABLE = Option.withoutDefault("single-table");
	public static final Option TIMEOUT = Option.withDefault("timeout", Integer.toString(Browser.DEFAULT_TIMEOUT));
	
	public static final String USAGE = 
"usage: microscraper <uri> [<options>]" + newline +
"		microscraper (" + JSON_INSTRUCTION + "=\"<json>\"|" + URI_INSTRUCTION + "<uri>) [<options>]" + newline +
"" + newline +
"uri" + newline +
"	A URI that points to microscraper instruction JSON." + newline +
"json" + newline +
"	Microscraper instruction JSON." + newline +
"options:" + newline +
"	" + BATCH_SIZE + "=<batch-size>" + newline +
"		If saving to SQL, assigns the batch size.  " + newline +
"		Defaults to " + BATCH_SIZE.getDefault() + newline +
"	" + DEFAULTS + "=\"<defaults>\"" + newline +
"		A form-encoded string of name value pairs to use as" + newline +
"		defaults during execution." + newline +
"	" + INPUT + "=<path> [" + INPUT_COLUMN_DELIMITER + "=<delimiter>]" + newline +
"		Path to a file with any number of additional default" + newline +
"		values.  Each row is executed separately.  The first" + newline +
"		row contains column names." + newline +
"		The default column delimiter is '"+ INPUT.getDefault() + "'." + newline +
"	" + LOG_FILE + "[=<path>]" + newline +
"		Pipe the log to a file." + newline +
"		Path is optional, defaults to 'yyyyMMddkkmmss.log' in the" + newline +
"		current directory." + newline +
"	" + LOG_STDOUT + newline +
"		Pipe the log to stdout." + newline +
"	" + MAX_RESPONSE_SIZE + newline +
"		How many KB of a response to load from a single request before " + newline +
"		cutting off the response.  Defaults to " + MAX_RESPONSE_SIZE.getDefault() + "KB." + newline +
"	" + OUTPUT_FORMAT_OPTION + "=(" + Utils.join(validOutputFormats.toArray(new String[0]), "|") +")" + newline +
"		How to format output.  Defaults to " + OUTPUT_FORMAT_OPTION.getDefault() + "." + newline +
"	" + OUTPUT_TO_FILE + "[=<path>], " + newline +
"		Where to save the output.  Defaults to 'yyyyMMddkkmmss.<format>' in" + newline +
"		the current directory output." + newline +
"	" + RATE_LIMIT + "=<max-kbps>" + newline +
"		The rate limit, in KBPS, for loading from a single host." + newline +
"		Defaults to " + RATE_LIMIT.getDefault() + " KBPS." + newline +
"	" + SINGLE_TABLE + newline +
"		Save all results to a single sqlite table, if using sqlite" + newline +
"	" + TIMEOUT + "=<timeout>" + newline +
"		How many seconds to wait before giving up on a request." + newline + 
"		Defaults to " + TIMEOUT.getDefault() + " seconds.";
	
	private final Map<Option, String> arguments = new HashMap<Option, String>();
	
	public Arguments(String[] args) throws IllegalArgumentException {
		if(args.length == 0) {
			throw new IllegalArgumentException("");
		}
		
		for(int i = 0 ; i < args.length ; i ++) {
			String arg = args[i];
			String value = null;
			if(arg.indexOf('=') > -1) {
				value = arg.substring(arg.indexOf('=') + 1);
				arg = arg.substring(0, arg.indexOf('='));
			}
			if(!Option.exists(arg) && i == 0) { // assume invalid first arg is a URI
				arguments.put(URI_INSTRUCTION, args[i]);
				continue; 
			}
			arguments.put(Option.retrieve(arg), value);
		}
		
		// Fix quotations on default values.
		String possiblyQuotedDefaults = get(DEFAULTS);
		if(possiblyQuotedDefaults.startsWith("\"") && possiblyQuotedDefaults.endsWith("\"")) {
			arguments.put(DEFAULTS, possiblyQuotedDefaults.substring(1, possiblyQuotedDefaults.length() - 2));
		}
	}
	
	public boolean has(Option option) {
		if(option.hasDefault()) {
			throw new IllegalArgumentException(option + " has a default value, never need to question its existence.");
		}
		return arguments.containsKey(option);
	}
	
	public String get(Option option) {
		if(option.hasDefault() && !arguments.containsKey(option)) {
			return option.getDefault();
		} else {
			return arguments.get(option);
		}
	}
}
