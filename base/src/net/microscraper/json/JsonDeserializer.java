package net.microscraper.json;

import java.io.IOException;

import net.microscraper.client.Browser;
import net.microscraper.client.DeserializationException;
import net.microscraper.client.Deserializer;
import net.microscraper.instruction.Action;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionPromise;
import net.microscraper.instruction.Load;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpUtils;
import net.microscraper.template.NameValuePairTemplate;
import net.microscraper.template.PatternTemplate;
import net.microscraper.template.Template;
import net.microscraper.template.TemplateCompilationException;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.util.Encoder;
import net.microscraper.util.StringUtils;

public class JsonDeserializer implements Deserializer {
	
	/**
	 * The default {@link Uri} to use when resolving local JSON.
	 */
	private final Uri defaultUri;
	
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
	 * The {@link Encoder} to use when deserializing {@link Load}s.
	 */
	private final Encoder encoder;
	
	private boolean isLoad(JsonObject jsonObject) {
		if(jsonObject.has(LOAD) && !jsonObject.has(FIND)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isFind(JsonObject jsonObject) {
		if(jsonObject.has(FIND) && !jsonObject.has(LOAD)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Deserialize an {@link Load} from a {@link JsonObject}.
	 * @param jsonObject
	 * @return A {@link Load}.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws MalformedUriException If a reference was not formatted properly.
	 * @throws IOException if a reference could not be loaded.
	 * @throws DeserializationException If the {@link Method} for {@link Load} cannot be recognized.
	 * @throws TemplateCompilationException If a {@link Template} could not be compiled.
	 */
	private Load deserializeLoad(JsonObject jsonObject) throws JsonException, DeserializationException,
			TemplateCompilationException, IOException, MalformedUriException {
		final Template url;
		final Template postData;
		final NameValuePairTemplate[] postNameValuePairs, cookies, headers;
		final PatternTemplate[] stops;
		
		url = Template.compile(jsonObject.getString(LOAD));
		
		cookies = jsonObject.has(COOKIES) ?
				deserializeNameValuePairTemplate(jsonObject.getJsonObject(COOKIES)) :
				new NameValuePairTemplate[] {};
		headers = jsonObject.has(HEADERS) ?
				deserializeNameValuePairTemplate(jsonObject.getJsonObject(HEADERS)) :
				new NameValuePairTemplate[] {};
		
		if(jsonObject.has(STOP)) {
			JsonArray stopJsonArray = jsonObject.getJsonArray(STOP);
			stops = new PatternTemplate[stopJsonArray.length()];
			for(int i = 0 ; i < stops.length ; i++) {
				stops[i] = deserializePatternTemplate(stopJsonArray.getJsonObject(i));
			}
		} else {
			stops = new PatternTemplate[] {};
		}
		
		if(jsonObject.has(POSTS)) {
			if(jsonObject.isJsonObject(POSTS)) {
				postNameValuePairs = deserializeNameValuePairTemplate(jsonObject.getJsonObject(POSTS));
				return Load.post(browser, encoder, url, postNameValuePairs, headers, cookies, stops);
			} else {
				postData = Template.compile(jsonObject.getString(POSTS));
				return Load.post(browser, encoder, url, postData, headers, cookies, stops);
			}
		}
		
		if(jsonObject.has(METHOD)) {
			String method = jsonObject.getString(METHOD);
			if(method.equalsIgnoreCase(Browser.POST)) {
				postData = Template.compile("");
				return Load.post(browser, encoder, url, postData, headers, cookies, stops);
			} else if(method.equalsIgnoreCase(Browser.HEAD)) {
				return Load.get(browser, encoder, url, headers, cookies, stops);
			}
		}
		return Load.get(browser, encoder, url, headers, cookies, stops);
	}

	/**
	 * Deserialize a {@link Find} from a {@link JsonObject}.
	 * @param jsonObject
	 * @return A {@link Find}.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws MalformedUriException If a reference was not formatted properly.
	 * @throws IOException if a reference could not be loaded.
	 * @throws DeserializationException if there is a problem setting the match range.
	 * @throws TemplateCompilationException If a {@link Template} could not be compiled.
	 */
	private Find deserializeFind(JsonObject jsonObject) throws JsonException, DeserializationException,
				TemplateCompilationException, MalformedUriException, IOException {
		final PatternTemplate pattern;
		final PatternTemplate[] tests;
		final Template replacement;
		final int minMatch;
		final int maxMatch;
		
		pattern = deserializePatternTemplate(jsonObject);
		if(jsonObject.has(TESTS)) {
			JsonArray testsJsonArray = jsonObject.getJsonArray(TESTS);
			tests = new PatternTemplate[testsJsonArray.length()];
			for(int i = 0 ; i < tests.length ; i ++) {
				tests[i] = deserializePatternTemplate(testsJsonArray.getJsonObject(i));
			}
		} else {
			tests = new PatternTemplate[] {};
		}
		
		replacement = jsonObject.has(REPLACE) ?
				Template.compile(jsonObject.getString(REPLACE)) :
				Template.compile(Find.ENTIRE_MATCH);
		
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
	 * @throws IOException if a reference could not be loaded.
	 * @throws DeserializationException If there was a problem deserializing.
	 */
	private Instruction deserializeInstruction(JsonObject jsonObject)
				throws DeserializationException, IOException {
		try {
			final Action action;
			final InstructionPromise[] children;
			final Instruction result;
			
			if(isFind(jsonObject)) {
				action = deserializeFind(jsonObject);
			} else if(isLoad(jsonObject)) {
				action = deserializeLoad(jsonObject);
			} else {
				throw new DeserializationException("There is no load or find action in the instruction.");
			}
			
			if(jsonObject.has(THEN)) {
				// If the key refers directly to an object, it is considered
				// an array of 1.
				/*if(jsonObject.isJsonObject(THEN)) {
					children = new Instruction[] {
						deserializeInstruction(jsonObject.getJsonObject(THEN))
					};
				} else {
					JsonArray array = jsonObject.getJsonArray(THEN);
					children = new Instruction[array.length()];
					for(int i = 0 ; i < array.length() ; i ++) {
						children[i] = deserializeInstruction(array.getJsonObject(i));
					}
				}*/
				if(jsonObject.isJsonObject(THEN)) {
					children = new InstructionPromise[] {
						new InstructionPromise(this, jsonObject.getString(THEN))	
					};
				} else {
					JsonArray array = jsonObject.getJsonArray(THEN);
					children = new InstructionPromise[array.length()];
					for(int i = 0 ; i < array.length() ; i ++) {
						children[i] = new InstructionPromise(this, array.getString(i));
					}
				}
			} else {
				children = new InstructionPromise[] {};
			}
	
			if(jsonObject.has(NAME)) {
				Template name = Template.compile(jsonObject.getString(NAME));
				if(jsonObject.has(SAVE)) {
					boolean shouldPersistValue = jsonObject.getBoolean(SAVE);
					result = new Instruction(action, children, shouldPersistValue, name);
				} else {
					result = new Instruction(action, children, name);
					
				}
			} else {
				if(jsonObject.has(SAVE)) {
					boolean shouldPersistValue = jsonObject.getBoolean(SAVE);
					result = new Instruction(action, children, shouldPersistValue);
				} else {
					result = new Instruction(action, children);
				}
			}
			return result;
		} catch (JsonException e) {
			throw new DeserializationException(e);
		} catch (TemplateCompilationException e) {
			throw new DeserializationException(e);
		} catch (MalformedUriException e) {
			throw new DeserializationException(e);
		}

	}

	/**
	 * Deserialize a {@link PatternTemplate} from a {@link JsonObject}.
	 * @param jsonObject Input {@link JsonObject} object.
	 * @return A {@link PatternTemplate} instance.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws TemplateCompilationException If a {@link Template} could not be compiled.
	 */
	private PatternTemplate deserializePatternTemplate(JsonObject jsonObject) throws TemplateCompilationException,
			JsonException{
		Template pattern = Template.compile(jsonObject.getString(FIND));
		boolean isCaseSensitive = jsonObject.has(IS_CASE_INSENSITIVE) ? jsonObject.getBoolean(IS_CASE_INSENSITIVE) : IS_CASE_INSENSITIVE_DEFAULT;
		boolean isMultiline = jsonObject.has(IS_MULTILINE) ? jsonObject.getBoolean(IS_MULTILINE) : IS_MULTILINE_DEFAULT;
		boolean doesDotMatchNewline = jsonObject.has(DOES_DOT_MATCH_ALL) ? jsonObject.getBoolean(DOES_DOT_MATCH_ALL) : DOES_DOT_MATCH_ALL_DEFAULT;
		return new PatternTemplate(compiler, pattern, isCaseSensitive, isMultiline, doesDotMatchNewline);
	}
	
	/**
	 * Deserialize a {@link NameValuePairs} from a {@link JsonObject} hash.
	 * @param jsonObject Input {@link JsonObject} hash.
	 * @return A {@link NameValuePairs} instance.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws TemplateCompilationException If a {@link Template} could not be compiled.
	 */
	private NameValuePairTemplate[] deserializeNameValuePairTemplate(JsonObject jsonObject)
				throws JsonException, TemplateCompilationException {
		NameValuePairTemplate[] pairs = new NameValuePairTemplate[jsonObject.length()];
		JsonIterator iter = jsonObject.keys();
		int i = 0;
		while(iter.hasNext()) {
			String key = (String) iter.next();
			String value = jsonObject.getString(key);
			pairs[i] = new NameValuePairTemplate(
					Template.compile(key),
					Template.compile(value));
			i++;
		}
		return pairs;
	}

	/**
	 * Key for {@link Find#replacement} value deserializing from JSON.
	 */
	public static final String REPLACE = "replace";
	
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
	 * Key for {@link Load#stops} when deserializing.
	 */
	public static final String STOP = "stop";
		
	/**
	 * Key for {@link Load#posts} when deserializing. 
	 */
	public static final String POSTS = "posts";

	/**
	 * Key for {@link Load#url} when deserializing.
	 */
	public static final String LOAD = "load";
	
	/**
	 * Key for {@link Load#getMethod()} when deserializing. Default is {@link #DEFAULT_METHOD},
	 */
	public static final String METHOD = "method";
	
	/**
	 * Key for {@link Load#cookies} when deserializing. Default is {@link #DEFAULT_COOKIES}.
	 */
	public static final String COOKIES = "cookies";
	
	/**
	 * Key for {@link Instruction#children} when deserializing from JSON.
	 */
	public static final String THEN = "then";
	
	/**
	 * Key for {@link Instruction#name} value when deserializing {@link Instruction} from JSON.
	 */
	public static final String NAME = "name";
	
	/**
	 * Key for {@link Instruction#shouldSaveValue} value when deserializing from JSON.
	 */
	public static final String SAVE = "save";
	
	/**
	 * Key for deserializing {@link PatternTemplate#pattern}.
	 */
	public static final String FIND = "find";
	
	/**
	 * Key for a reference that will replace the current object.
	 */
	public static final String REF = "$ref";
	
	/**
	 * Key for an object that will extend the current object.
	 */
	public static final String EXTENDS = "extends";
	
	/**
	 * Key for a self-reference.
	 */
	public static final String SELF = "$this";
	
	/**
	 * Key for deserializing {@link PatternTemplate#isCaseInsensitive}.
	 */
	public static final String IS_CASE_INSENSITIVE = "case_insensitive";
	public static final boolean IS_CASE_INSENSITIVE_DEFAULT = false;
	
	/**
	 * Key for deserializing {@link PatternTemplate#isMultiline}.
	 */
	public static final String IS_MULTILINE = "multiline";
	public static final boolean IS_MULTILINE_DEFAULT = false;
	
	/** 
	 * Key for deserializing {@link PatternTemplate#doesDotMatchNewline}.
	 */
	public static final String DOES_DOT_MATCH_ALL = "dot_matches_all";
	public static final boolean DOES_DOT_MATCH_ALL_DEFAULT = true;

	public JsonDeserializer(JsonParser parser, RegexpCompiler compiler, Browser browser, Encoder encoder) {
		this.compiler = compiler;
		this.parser = parser;
		this.browser = browser;
		this.encoder = encoder;
	}
	
	public Instruction deserializeString(String serializedString)
			throws DeserializationException, IOException {
		try {
			return deserializeInstruction(parser.parse(serializedString));
		} catch(MalformedUriException e) {
			throw new DeserializationException(e);
		} catch(JsonException e) {
			throw new DeserializationException(e);			
		}
	}
	
	/*
	public Instruction deserializeUri(String uriString)
			throws DeserializationException, IOException {
		try {
			return deserializeInstruction(parser.load(uriString));
		} catch(MalformedUriException e) {
			throw new DeserializationException(e);
		} catch(JsonException e) {
			throw new DeserializationException(e);			
		}
	}
	*/
}
