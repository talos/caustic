package net.microscraper.json;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.client.DeserializationException;
import net.microscraper.client.Deserializer;
import net.microscraper.instruction.Action;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Load;
import net.microscraper.mustache.MustacheCompilationException;
import net.microscraper.mustache.MustacheNameValuePair;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.mustache.MustacheTemplate;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpUtils;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.util.StringUtils;

public class JsonDeserializer implements Deserializer {
	
	/**
	 * The {@link JsonParser} used to parse JSON objects.
	 */
	private final JsonParser parser;
	
	/**
	 * The {@link RegexpCompiler} to use when deserializing {@link Find}s.
	 */
	private final RegexpCompiler compiler;
	
	/**
	 * The {@link Browser} to use when deserializing {@link Load}s.
	 */
	private final Browser browser;
	
	/**
	 * Deserialize an {@link Load} from a {@link JsonObject}.
	 * @param jsonObject
	 * @return A {@link Load}.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws MalformedUriException If a reference was not formatted properly.
	 * @throws IOException if a reference could not be loaded.
	 * @throws DeserializationException If the {@link Method} for {@link Load} cannot be recognized.
	 * @throws MustacheCompilationException If a {@link MustacheTemplate} could not be compiled.
	 */
	private Load deserializeLoad(JsonObject jsonObject) throws JsonException, DeserializationException,
			MustacheCompilationException, IOException, MalformedUriException {
		final MustacheTemplate url;
		final Load[] preload;
		final MustacheTemplate postData;
		final MustacheNameValuePair[] postNameValuePairs, cookies, headers;
		final MustachePattern[] stops;
		
		url = MustacheTemplate.compile(jsonObject.getString(URL));
		
		cookies = jsonObject.has(COOKIES) ?
				deserializeMustacheNameValuePairArray(jsonObject.getJsonObject(COOKIES)) :
				new MustacheNameValuePair[] {};
		headers = jsonObject.has(HEADERS) ?
				deserializeMustacheNameValuePairArray(jsonObject.getJsonObject(HEADERS)) :
				new MustacheNameValuePair[] {};
		
		if(jsonObject.has(PRELOAD)) {
			JsonArray preloadJsonArray = jsonObject.getJsonArray(PRELOAD);
			preload = new Load[preloadJsonArray.length()];
			for(int i = 0 ; i < preload.length ; i++) {
				preload[i] = deserializeLoad(preloadJsonArray.getJsonObject(i));
			}
		} else {
			preload = new Load[] {};
		}
		
		if(jsonObject.has(STOPS)) {
			JsonArray stopJsonArray = jsonObject.getJsonArray(STOPS);
			stops = new MustachePattern[stopJsonArray.length()];
			for(int i = 0 ; i < stops.length ; i++) {
				stops[i] = deserializeMustachePattern(stopJsonArray.getJsonObject(i));
			}
		} else {
			stops = new MustachePattern[] {};
		}
		
		if(jsonObject.has(POSTS)) {
			if(jsonObject.isJsonObject(POSTS)) {
				postNameValuePairs = deserializeMustacheNameValuePairArray(jsonObject.getJsonObject(POSTS));
				return Load.post(browser, url, postNameValuePairs, headers, cookies, preload, stops);
			} else {
				postData = MustacheTemplate.compile(jsonObject.getString(POSTS));
				return Load.post(browser, url, postData, headers, cookies, preload, stops);
			}
		}
		
		if(jsonObject.has(METHOD)) {
			String method = jsonObject.getString(METHOD);
			if(method.equalsIgnoreCase(Browser.POST)) {
				postData = MustacheTemplate.compile("");
				return Load.post(browser, url, postData, headers, cookies, preload, stops);
			} else if(method.equalsIgnoreCase(Browser.HEAD)) {
				return Load.get(browser, url, headers, cookies, preload, stops);
			}
		}
		return Load.get(browser, url, headers, cookies, preload, stops);
	}

	/**
	 * Deserialize a {@link Find} from a {@link JsonObject}.
	 * @param jsonObject
	 * @return A {@link Find}.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws MalformedUriException If a reference was not formatted properly.
	 * @throws IOException if a reference could not be loaded.
	 * @throws DeserializationException if there is a problem setting the match range.
	 * @throws MustacheCompilationException If a {@link MustacheTemplate} could not be compiled.
	 */
	private Find deserializeFind(JsonObject jsonObject) throws JsonException, DeserializationException,
				MustacheCompilationException, MalformedUriException, IOException {
		final MustachePattern pattern;
		final MustachePattern[] tests;
		final MustacheTemplate replacement;
		final int minMatch;
		final int maxMatch;
		
		pattern = deserializeMustachePattern(jsonObject);
		if(jsonObject.has(TESTS)) {
			JsonArray testsJsonArray = jsonObject.getJsonArray(TESTS);
			tests = new MustachePattern[testsJsonArray.length()];
			for(int i = 0 ; i < tests.length ; i ++) {
				tests[i] = deserializeMustachePattern(testsJsonArray.getJsonObject(i));
			}
		} else {
			tests = new MustachePattern[] {};
		}
		
		replacement = jsonObject.has(REPLACEMENT) ?
				MustacheTemplate.compile(jsonObject.getString(REPLACEMENT)) :
				MustacheTemplate.compile(Find.ENTIRE_MATCH);
		
		if(jsonObject.has(MATCH)) {
			if(jsonObject.has(MIN_MATCH) || jsonObject.has(MAX_MATCH)) {
				throw new DeserializationException("Cannot define max or min when defining a match.");
			}
			minMatch = jsonObject.getInt(MATCH);
			maxMatch = jsonObject.getInt(MATCH);
		} else {
			minMatch = jsonObject.has(MIN_MATCH) ? jsonObject.getInt(MIN_MATCH) : Pattern.FIRST_MATCH;
			maxMatch = jsonObject.has(MAX_MATCH) ? jsonObject.getInt(MAX_MATCH) : Pattern.LAST_MATCH;
		}
		
		if(!RegexpUtils.isValidRange(minMatch, maxMatch)) {
			throw new DeserializationException(StringUtils.quote(minMatch) + " and " + StringUtils.quote(maxMatch) +
					" form invalid range.");
		}
		
		return new Find(pattern, replacement, minMatch, maxMatch, tests);
	}

	/**
	 * Deserialize an {@link Instruction} from a {@link JsonObject}.
	 * @param jsonObject
	 * @param defaultName
	 * @param defaultShouldSaveValue
	 * @return
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws MalformedUriException If a reference was not formatted properly.
	 * @throws IOException if a reference could not be loaded.
	 * @throws DeserializationException If there was a problem parsing a child {@link Find}.
	 * @throws MustacheCompilationException If a {@link MustacheTemplate} could not be compiled.
	 */
	private Instruction deserializeInstruction(JsonObject jsonObject, Action action,
			MustacheTemplate defaultName,
			boolean defaultShouldSaveValue)
				throws MustacheCompilationException, DeserializationException, JsonException,
				MalformedUriException, IOException {
		final MustacheTemplate name;
		final boolean shouldSaveValue;
		final Find[] finds;
		final Load[] load;
		
		if(jsonObject.has(NAME)) {
			name = MustacheTemplate.compile(jsonObject.getString(NAME));
		} else {
			name = defaultName;
		}
		if(jsonObject.has(SAVE)) {
			shouldSaveValue = jsonObject.getBoolean(SAVE);
		} else {
			shouldSaveValue = defaultShouldSaveValue;
		}
		
		if(jsonObject.has(FIND)) {
			// If the key refers directly to an object, it is considered
			// an array of 1.
			if(jsonObject.isJsonObject(FIND)) {
				//children.add(new Find(jsonObject.getJSONObject(FIND)));
				finds = new Find[] { deserializeFind(jsonObject.getJsonObject(FIND)) };
			} else {
				JsonArray array = jsonObject.getJsonArray(FIND);
				finds = new Find[array.length()];
				for(int i = 0 ; i < array.length() ; i ++) {
					finds[i] = deserializeFind(array.getJsonObject(i));
				}
			}
		} else {
			finds = new Find[] {};
		}
		
		if(jsonObject.has(LOAD)) {
			// If the key refers directly to an object, it is considered
			// an array of 1.
			if(jsonObject.isJsonObject(LOAD)) {
				load = new Load[] { deserializeLoad(jsonObject.getJsonObject(LOAD)) };
			} else {
				JsonArray array = jsonObject.getJsonArray(LOAD);
				load = new Load[array.length()];
				for(int i = 0 ; i < array.length() ; i ++) {
					load[i] = deserializeLoad(array.getJsonObject(i));
				}
			}
		} else {
			load = new Load[] {};
		}
		
		return new Instruction(shouldSaveValue, name, action, children);
	}

	/**
	 * Deserialize a {@link MustachePattern} from a {@link JsonObject}.
	 * @param jsonObject Input {@link JsonObject} object.
	 * @return A {@link MustachePattern} instance.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws MustacheCompilationException If a {@link MustacheTemplate} could not be compiled.
	 */
	private MustachePattern deserializeMustachePattern(JsonObject jsonObject) throws MustacheCompilationException,
			JsonException{
		MustacheTemplate pattern = MustacheTemplate.compile(jsonObject.getString(PATTERN));
		boolean isCaseSensitive = jsonObject.has(IS_CASE_SENSITIVE) ? jsonObject.getBoolean(IS_CASE_SENSITIVE) : IS_CASE_SENSITIVE_DEFAULT;
		boolean isMultiline = jsonObject.has(IS_MULTILINE) ? jsonObject.getBoolean(IS_MULTILINE) : IS_MULTILINE_DEFAULT;
		boolean doesDotMatchNewline = jsonObject.has(DOES_DOT_MATCH_ALL) ? jsonObject.getBoolean(DOES_DOT_MATCH_ALL) : DOES_DOT_MATCH_ALL_DEFAULT;
		return new MustachePattern(compiler, pattern, isCaseSensitive, isMultiline, doesDotMatchNewline);
	}
	
	/**
	 * Deserialize a {@link NameValuePairs} from a {@link JsonObject} hash.
	 * @param jsonObject Input {@link JsonObject} hash.
	 * @return A {@link NameValuePairs} instance.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws MustacheCompilationException If a {@link MustacheTemplate} could not be compiled.
	 */
	private MustacheNameValuePair[] deserializeMustacheNameValuePairArray(JsonObject jsonObject)
				throws JsonException, MustacheCompilationException {
		MustacheNameValuePair[] pairs = new MustacheNameValuePair[jsonObject.length()];
		JsonIterator iter = jsonObject.keys();
		int i = 0;
		while(iter.hasNext()) {
			String key = (String) iter.next();
			String value = jsonObject.getString(key);
			pairs[i] = new MustacheNameValuePair(
					MustacheTemplate.compile(key),
					MustacheTemplate.compile(value));
			i++;
		}
		return pairs;
	}

	/**
	 * Key for {@link Find#replacement} value deserializing from JSON.
	 */
	public static final String REPLACEMENT = "replacement";
	
	/**
	 * Key for {@link Find#tests} value deserializing from JSON.
	 */
	public static final String TESTS = "tests";

	/**
	 * Conveniently deserialize {@link Find#minMatch} and {@link Find#maxMatch}.
	 * If this exists in an object, both {@link #maxMatch} and {@link #minMatch} are its
	 * value.<p>
	 */
	public static final String MATCH = "match";

	/**
	 * Key for {@link Find#minMatch} value when deserializing from JSON.
	 */
	public static final String MIN_MATCH = "min";

	/**
	 * Key for {@link Find#maxMatch} value when deserializing from JSON.
	 */
	public static final String MAX_MATCH = "max";
	
	/**
	 * Key for {@link Load#headers} when deserializing.
	 */
	public static final String HEADERS = "headers";
	
	/**
	 * Key for {@link Load#preload} when deserializing.
	 */
	public static final String PRELOAD = "preload";
	
	/**
	 * Key for {@link Load#stops} when deserializing.
	 */
	public static final String STOPS = "stop";
		
	/**
	 * Key for {@link Load#posts} when deserializing. 
	 */
	public static final String POSTS = "posts";

	/**
	 * Key for {@link Load#url} when deserializing.
	 */
	public static final String URL = "url";
	
	/**
	 * Key for {@link Load#getMethod()} when deserializing. Default is {@link #DEFAULT_METHOD},
	 */
	public static final String METHOD = "method";
	
	/**
	 * Key for {@link Load#cookies} when deserializing. Default is {@link #DEFAULT_COOKIES}.
	 */
	public static final String COOKIES = "cookies";
	
	/**
	 * Key for {@link Instruction#finds} when deserializing from JSON.
	 */
	public static final String FIND = "find";

	/**
	 * Key for {@link Instruction#pages} when deserializing from JSON.
	 */
	public static final String LOAD = "load";
	
	/**
	 * Key for {@link Instruction#name} value when deserializing {@link Instruction} from JSON.
	 */
	public static final String NAME = "name";
	
	/**
	 * Key for {@link Instruction#shouldSaveValue} value when deserializing from JSON.
	 */
	public static final String SAVE = "save";
	
	/**
	 * Key for deserializing {@link MustachePattern#pattern}.
	 */
	public static final String PATTERN = "pattern";
	
	/**
	 * Key for deserializing {@link MustachePattern#isCaseSensitive}.
	 */
	public static final String IS_CASE_SENSITIVE = "case_sensitive";
	public static final boolean IS_CASE_SENSITIVE_DEFAULT = false;
	
	/**
	 * Key for deserializing {@link MustachePattern#isMultiline}.
	 */
	public static final String IS_MULTILINE = "multiline";
	public static final boolean IS_MULTILINE_DEFAULT = false;
	
	/** 
	 * Key for deserializing {@link MustachePattern#doesDotMatchNewline}.
	 */
	public static final String DOES_DOT_MATCH_ALL = "dot_matches_all";
	public static final boolean DOES_DOT_MATCH_ALL_DEFAULT = true;

	public JsonDeserializer(JsonParser parser, RegexpCompiler compiler, Browser browser) {
		this.compiler = compiler;
		this.parser = parser;
		this.browser = browser;
	}
	
	public Load deserializeLoad(String serializedString)
			throws DeserializationException, IOException {
		try {
			return deserializeLoad(parser.parse(serializedString));
		} catch (JsonException e) {
			throw new DeserializationException(e);
		} catch (MustacheCompilationException e) {
			throw new DeserializationException(e);
		} catch (MalformedUriException e) {
			throw new DeserializationException(e);
		}
	}

	public Find deserializeFind(String serializedString)
			throws DeserializationException, IOException {
		try {
			return deserializeFind(parser.parse(serializedString));
		} catch (JsonException e) {
			throw new DeserializationException(e);
		} catch (MustacheCompilationException e) {
			throw new DeserializationException(e);
		} catch (MalformedUriException e) {
			throw new DeserializationException(e);
		}
	}
}
