package net.microscraper.json;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.Browser;
import net.microscraper.client.DeserializationException;
import net.microscraper.client.Deserializer;
import net.microscraper.instruction.Action;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionPromise;
import net.microscraper.instruction.Load;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpUtils;
import net.microscraper.template.NameValuePairTemplate;
import net.microscraper.template.Template;
import net.microscraper.template.TemplateCompilationException;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.uri.RemoteToLocalSchemeResolutionException;
import net.microscraper.uri.URILoader;
import net.microscraper.uri.UriResolver;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;
import net.microscraper.util.VectorUtils;

public class JsonDeserializer implements Deserializer {
	
	/**
	 * The {@link UriResolver} to use when resolving URIs.
	 */
	private final UriResolver uriResolver;
	
	/**
	 * The {@link URILoader} to use when loading the contents of URIs.
	 */
	private final URILoader uriLoader;
	
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
	
	private Execution deserialize(String jsonString, Variables variables, String uri,
			String openTagString, String closeTagString)
			throws DeserializationException, JsonException, TemplateCompilationException,
			IOException, MalformedUriException, InterruptedException, RemoteToLocalSchemeResolutionException {
		final Execution result;
		
		// Parse non-objects as URIs after substitution with variables
		if(!parser.isJsonObject(jsonString)) {
			Execution uriSub = Template.compile(jsonString, openTagString, closeTagString)
					.subEncoded(variables, encoder, Browser.UTF_8);
			if(uriSub.isSuccessful()) {
				String uriPath = (String) uriSub.getExecuted();
				//Uri uriToLoad = uriFactory.fromString(uriString);
				String uriToLoad = uriResolver.resolve(uri, uriPath);
				String loadedJSONString = uriLoader.load(uriToLoad);
				
				result = deserialize(loadedJSONString, variables, uriToLoad, openTagString, closeTagString);
			} else {
				result = uriSub;
			}
		} else {
			
			JsonObject initialObj = parser.parse(jsonString);
			
			Template name = null;
			Boolean shouldPersistValue = null;
			
			// InstructionPromise children.
			Vector children = new Vector();
			
			// populated for Load action
			Template url = null;
			String method = null;
			Template postData = null;
			Vector posts = new Vector();
			Vector cookies = new Vector();
			Vector headers = new Vector();
			
			// Populated for Find action
			Template pattern = null;
			Boolean isCaseInsensitive = null;
			Boolean isMultiline = null;
			Boolean doesDotMatchNewline = null;
			Template replace = null;
			Integer min = null;
			Integer max = null;
			Integer match = null;
			
			// This vector expands if EXTENDS objects are specified.
			Vector jsonObjects = new Vector();
			jsonObjects.add(initialObj);
			
			for(int i = 0 ; i < jsonObjects.size() ; i ++) {
				JsonObject obj = (JsonObject) jsonObjects.get(i);
				JsonIterator iterator = obj.keys();
				
				// Case-insensitive loop over key names.
				while(iterator.hasNext()) {
					String key = iterator.next();
					
					/** Attributes for Instruction. **/
					if(key.equalsIgnoreCase(EXTENDS)) {
						Vector extendsStrings = new Vector();
						Vector extendsObjects = new Vector();
						
						if(obj.isJsonObject(key)) {
							extendsObjects.add(obj.getJsonObject(key));
						} else if(obj.isJsonArray(key)) {
							JsonArray array = obj.getJsonArray(key);
							for(int j = 0 ; j < array.length(); j ++) {
								if(array.isJsonObject(j)) {
									extendsObjects.add(array.getJsonObject(j));
								} else if(array.isString(j)) {
									extendsStrings.add(array.getString(j));
								} else {
									throw new DeserializationException(EXTENDS + " array elements must be strings or objects.");
								}
							}
						} else if(obj.isString(key)) {
							extendsStrings.add(obj.getString(key));
						}
						
						for(int j = 0 ; j < extendsObjects.size() ; j ++) {
							jsonObjects.add(extendsObjects.elementAt(j));
						}
						for(int j = 0 ; j < extendsStrings.size() ; j ++) {
							Template extendsUriTemplate = Template.compile(obj.getString(key), openTagString, closeTagString);
							Execution uriSubstitution = extendsUriTemplate.subEncoded(variables, encoder, Browser.UTF_8);
							if(uriSubstitution.isSuccessful()) {
								String uriPath = (String) uriSubstitution.getExecuted();
								String uriToLoad = uriResolver.resolve(uri, uriPath);
								String loadedJSONString = uriLoader.load(uriToLoad);
								
								jsonObjects.add(parser.parse(loadedJSONString));
							} else {
								return uriSubstitution; // can't substitute uri to load EXTENDS reference, missing-variable out.
							}
						}
					} else if(key.equalsIgnoreCase(THEN)) {
						if(obj.isJsonObject(key)) {
							children.add(new InstructionPromise(this, obj.getString(key), uri.toString()));
						} else if (obj.isJsonArray(key)) {
							JsonArray array = obj.getJsonArray(key);
							for(int j = 0 ; j < array.length() ; j ++) {
								children.add(new InstructionPromise(this, array.getString(j), uri.toString()));
							}
						} else if(obj.isString(key)) {
							if(obj.getString(key).equalsIgnoreCase(SELF)) {
								children.add(new InstructionPromise(this, jsonString, uri.toString()));
							} else {
								children.add(new InstructionPromise(this, obj.getString(key), uri.toString()));
							}
						} else {
							throw new DeserializationException(StringUtils.quote(key) +
									" must be a String reference to another " +
									" instruction, an object with another instruction, or an array with any number " +
									" of both.");
						}
					} else if(key.equalsIgnoreCase(NAME)) {
						name = Template.compile(obj.getString(key), openTagString, closeTagString);
					} else if(key.equalsIgnoreCase(SAVE)) {
						shouldPersistValue = Boolean.valueOf(obj.getBoolean(key));
						
					/** Load-only attributes. **/
					} else if(key.equalsIgnoreCase(LOAD)) {
						url = Template.compile(obj.getString(key), openTagString, closeTagString);
					} else if(key.equalsIgnoreCase(METHOD)) {
						method = obj.getString(key);
					} else if(key.equalsIgnoreCase(POSTS)) {
						if(obj.isJsonObject(key)) {
							VectorUtils.arrayIntoVector(
									deserializeNameValuePairTemplate(obj.getJsonObject(key), openTagString, closeTagString),
									posts);
						} else if(obj.isString(key)) {
							postData = Template.compile(obj.getString(key), openTagString, closeTagString);
						} else {
							throw new DeserializationException(StringUtils.quote(key) +
									" must be a String with post data or an object with name-value-pairs.");				
						}
					} else if(key.equalsIgnoreCase(COOKIES)) {
						VectorUtils.arrayIntoVector(
								deserializeNameValuePairTemplate(obj.getJsonObject(key), openTagString, closeTagString),
								cookies);
					} else if(key.equalsIgnoreCase(HEADERS)) {
						VectorUtils.arrayIntoVector(
								deserializeNameValuePairTemplate(obj.getJsonObject(key), openTagString, closeTagString),
								headers);
						
					/** Pattern attributes. **/
					} else if(key.equalsIgnoreCase(FIND)) {
						pattern = Template.compile(obj.getString(key), openTagString, closeTagString);
					} else if(key.equalsIgnoreCase(IS_CASE_INSENSITIVE)) {
						isCaseInsensitive = Boolean.valueOf(obj.getBoolean(key));
					} else if(key.equalsIgnoreCase(DOES_DOT_MATCH_ALL)) {
						doesDotMatchNewline = Boolean.valueOf(obj.getBoolean(key));
					} else if(key.equalsIgnoreCase(IS_MULTILINE)) {
						isMultiline = Boolean.valueOf(obj.getBoolean(key));
						
						
					/** Find-only attributes. **/
					} else if(key.equalsIgnoreCase(REPLACE)) {
						replace = Template.compile(obj.getString(key), openTagString, closeTagString);
					} else if(key.equalsIgnoreCase(MIN_MATCH)) {
						min = Integer.valueOf(obj.getInt(key));
					} else if(key.equalsIgnoreCase(MAX_MATCH)) {
						max = Integer.valueOf(obj.getInt(key));
					} else if(key.equalsIgnoreCase(MATCH)) {
						match = Integer.valueOf(obj.getInt(key));
						
					/** OK for all. **/
					} else if(key.equalsIgnoreCase(DESCRIPTION)) {
						
					} else {
						throw new DeserializationException(StringUtils.quote(key) + " is not a valid key.");
					}
				}
			}
			
			Action action = null;
			if(url != null && pattern != null) {
				// Can't define two actions.
				throw new DeserializationException("Cannot define both " + FIND + " and " + LOAD);
			} else if(url != null) {
				// We have a Load
				Load load = new Load(browser, encoder, url);
				action = load;
				
				if(method != null) {
					load.setMethod(method);
				}
				if(postData != null) {
					load.setPostData(postData);
				}
				for(int i = 0 ; i < posts.size() ; i ++) {
					load.addPostNameValuePair((NameValuePairTemplate) posts.elementAt(i));
				}
				for(int i = 0 ; i < cookies.size() ; i ++) {
					load.addCookie((NameValuePairTemplate) cookies.elementAt(i));
				}
				for(int i = 0 ; i < headers.size() ; i ++) {
					load.addHeader((NameValuePairTemplate) headers.elementAt(i));
				}
			} else if(pattern != null) {
				// We have a Find
				Find find = new Find(compiler, pattern);
				action = find;
				
				if(replace != null) {
					find.setReplacement(replace);
				}
				if(match != null) {
					if(min != null || max != null) {
						throw new DeserializationException("Cannot define " + MIN_MATCH + " or " + MAX_MATCH + 
								" in addition to " + MATCH + " in " + FIND);
					}
					find.setMaxMatch(match.intValue());
					find.setMinMatch(match.intValue());
				}
				if(min != null) {
					find.setMinMatch(min.intValue());
				}
				if(max != null) {
					find.setMaxMatch(max.intValue());
				}
				if(min != null && max != null) {
					if(RegexpUtils.isValidRange(min.intValue(), max.intValue()) == false) {
						throw new DeserializationException("Range " + StringUtils.quote(min) + " to " +
								StringUtils.quote(max) + " is not valid for " + FIND);
					}
				}
				if(isCaseInsensitive != null) {
					find.setIsCaseInsensitive(isCaseInsensitive.booleanValue());
				}
				if(doesDotMatchNewline != null) {
					find.setDoesDotMatchAll(doesDotMatchNewline.booleanValue());
				}
				if(isMultiline != null) {
					find.setIsMultiline(isMultiline.booleanValue());
				}
			}
			
			if(action != null) {
				Instruction instruction = new Instruction(action);
				if(shouldPersistValue != null) {
					instruction.setShouldPersistValue(shouldPersistValue.booleanValue());
				}
				if(name != null) {
					instruction.setName(name);
				}
				for(int i = 0 ; i < children.size(); i ++ ) {
					instruction.addChild((InstructionPromise) children.elementAt(i));
				}
				
				result = Execution.success(instruction);
			} else {
				throw new DeserializationException("Must define " + FIND + " or " + LOAD);
			}
		}
		return result;
	}
	
	/**
	 * Deserialize a {@link NameValuePairTemplate} array from a {@link JsonObject} hash.
	 * @param jsonObject Input {@link JsonObject} hash.
	 * @param openTagString
	 * @param closeTagString
	 * @return A {@link NameValuePairTemplate} array.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws TemplateCompilationException If a {@link Template} could not be compiled.
	 */
	private NameValuePairTemplate[] deserializeNameValuePairTemplate(JsonObject jsonObject,
			String openTagString, String closeTagString)
				throws JsonException, TemplateCompilationException {
		NameValuePairTemplate[] pairs = new NameValuePairTemplate[jsonObject.length()];
		JsonIterator iter = jsonObject.keys();
		int i = 0;
		while(iter.hasNext()) {
			String key = (String) iter.next();
			String value = jsonObject.getString(key);
			pairs[i] = new NameValuePairTemplate(
					Template.compile(key, openTagString, closeTagString),
					Template.compile(value, openTagString, closeTagString));
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
	//public static final String TESTS = "tests";

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
	//public static final String STOP = "stop";
		
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
	 * Key for an object that will extend the current object.
	 */
	public static final String EXTENDS = "extends";
	
	/**
	 * Key for a self-reference.
	 */
	public static final String SELF = "$this";
	
	/**
	 * Key for description.
	 */
	public static final String DESCRIPTION = "description";
	
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

	/**
	 * 
	 * @param parser
	 * @param compiler
	 * @param browser
	 * @param encoder
	 * @param uriFactory
	 */
	public JsonDeserializer(JsonParser parser, RegexpCompiler compiler, Browser browser,
			Encoder encoder, UriResolver uriResolver, URILoader uriLoader) throws MalformedUriException {
		this.compiler = compiler;
		this.parser = parser;
		this.browser = browser;
		this.encoder = encoder;
		this.uriResolver = uriResolver;
		this.uriLoader = uriLoader;
	}
	
	public Execution deserializeString(String serializedString, Variables variables, String uri) {
		
		try {
			return deserialize(serializedString, variables, uri,
					Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG);
		} catch(JsonException e) {
			return Execution.deserializationException(new DeserializationException(e));
		} catch (MalformedUriException e) {
			return Execution.deserializationException(new DeserializationException(e));
		} catch(IOException e) {
			return Execution.deserializationException(new DeserializationException(e));
		} catch(TemplateCompilationException e) {
			return Execution.deserializationException(new DeserializationException(e));
		} catch(InterruptedException e) {
			return Execution.deserializationException(new DeserializationException(e));
		} catch (RemoteToLocalSchemeResolutionException e) {
			return Execution.deserializationException(new DeserializationException(e));
		} catch (DeserializationException e) {
			return Execution.deserializationException(e);
		}
	}
}
