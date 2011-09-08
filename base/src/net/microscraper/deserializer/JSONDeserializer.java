package net.microscraper.deserializer;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.DeserializedInstruction;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Load;
import net.microscraper.instruction.SerializedInstruction;
import net.microscraper.json.JsonArray;
import net.microscraper.json.JsonException;
import net.microscraper.json.JsonIterator;
import net.microscraper.json.JsonObject;
import net.microscraper.json.JsonParser;
import net.microscraper.log.Logger;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.RegexpUtils;
import net.microscraper.template.HashtableTemplate;
import net.microscraper.template.StringSubstitution;
import net.microscraper.template.StringTemplate;
import net.microscraper.template.TemplateCompilationException;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.uri.RemoteToLocalSchemeResolutionException;
import net.microscraper.uri.URILoader;
import net.microscraper.uri.UriResolver;
import net.microscraper.util.Encoder;
import net.microscraper.util.StringMap;
import net.microscraper.util.StringUtils;

public class JSONDeserializer implements Deserializer {
	
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
	 * The {@link HttpBrowser} to use when deserializing {@link Load}s.
	 */
	private final HttpBrowser browser;
	
	/**
	 * The {@link Encoder} to use when deserializing {@link Load}s.
	 */
	private final Encoder encoder;
	
	private DeserializerResult deserialize(String jsonString, StringMap input, String uri, String openTagString, String closeTagString)
			throws JsonException, TemplateCompilationException,
			IOException, MalformedUriException, InterruptedException, RemoteToLocalSchemeResolutionException {
		final DeserializerResult result;
		
		// Parse non-objects as URIs.  Any substitution should have been done beforehand.
		if(!parser.isJsonObject(jsonString)) {
			StringSubstitution uriSub = new StringTemplate(jsonString, openTagString, closeTagString)
					.subEncoded(input, encoder);
			if(!uriSub.isMissingTags()) {
				String uriPath = uriSub.getSubstituted();
				String uriToLoad = uriResolver.resolve(uri, uriPath);
				String loadedJSONString = uriLoader.load(uriToLoad);
				
				result = deserialize(loadedJSONString, input, uriToLoad, openTagString, closeTagString);
			} else {
				result = DeserializerResult.missingTags(uriSub.getMissingTags());
			}			
		} else {
			
			JsonObject initialObj = parser.parse(jsonString);
			
			StringTemplate name = null;
			//Boolean shouldPersistValue = null;
			
			// JSONSerializedInstruction children.
			Vector children = new Vector();
			
			// populated for Load action
			StringTemplate url = null;
			String method = null;
			StringTemplate postData = null;
			HashtableTemplate posts = new HashtableTemplate();
			HashtableTemplate cookies = new HashtableTemplate();
			HashtableTemplate headers = new HashtableTemplate();
			
			// Populated for Find action
			StringTemplate pattern = null;
			Boolean isCaseInsensitive = null;
			Boolean isMultiline = null;
			Boolean doesDotMatchNewline = null;
			StringTemplate replace = null;
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
									return DeserializerResult.failure(EXTENDS + " array elements must be strings or objects.");
								}
							}
						} else if(obj.isString(key)) {
							extendsStrings.add(obj.getString(key));
						}
						
						for(int j = 0 ; j < extendsObjects.size() ; j ++) {
							jsonObjects.add(extendsObjects.elementAt(j));
						}
						for(int j = 0 ; j < extendsStrings.size() ; j ++) {
							//String uri = uriResolver.resolve(baseUri, (String) extendsStrings.elementAt(j));
							//jsonObjects.add(parser.parse(uriLoader.load(uri)));
							StringTemplate extendsUriTemplate = new StringTemplate(obj.getString(key), openTagString, closeTagString);
							StringSubstitution uriSubstitution = extendsUriTemplate.subEncoded(input, encoder);
							if(!uriSubstitution.isMissingTags()) {
								String uriPath = uriSubstitution.getSubstituted();
								String uriToLoad = uriResolver.resolve(uri, uriPath);
								String loadedJSONString = uriLoader.load(uriToLoad);
								
								jsonObjects.add(parser.parse(loadedJSONString));
							} else {
								return DeserializerResult.missingTags(uriSubstitution.getMissingTags()); // can't substitute uri to load EXTENDS reference, missing-variable out.
							}
						}
					} else if(key.equalsIgnoreCase(THEN)) {
						Vector thenStrings = new Vector();
						Vector thenObjects = new Vector(); // Strings are added to this too -- their deserialization is handled later.
						
						if(obj.isJsonObject(key)) {
							thenObjects.add(obj.getString(key));
						} else if (obj.isJsonArray(key)) {
							JsonArray array = obj.getJsonArray(key);
							for(int j = 0 ; j < array.length() ; j ++) {
								if(array.isJsonObject(j)) {
									thenObjects.add(array.getString(j));
								} else if(array.isString(j)) {
									thenStrings.add(array.getString(j));
								} else {
									return DeserializerResult.failure(THEN + " array elements must be strings or objects.");
								}
							}
						} else if(obj.isString(key)) {
							thenStrings.add(obj.getString(key));
						} else {
							return DeserializerResult.failure(StringUtils.quote(key) +
									" must be a String reference to another " +
									" instruction, an object with another instruction, or an array with any number " +
									" of both.");
						}
						
						for(int j = 0 ; j < thenStrings.size(); j ++) {
							String thenString = (String) thenStrings.elementAt(j);
							
							if(thenString.equalsIgnoreCase(SELF)) {
								children.add(new SerializedInstruction(jsonString, this, uri));
							} else {
								children.add(new SerializedInstruction(thenString, this, uri));
							}
						}
						for(int j = 0 ; j < thenObjects.size(); j ++ ) {
							String thenObjectAsString = (String) thenObjects.elementAt(j);
							children.add(new SerializedInstruction(thenObjectAsString, this, uri));
						}
					} else if(key.equalsIgnoreCase(NAME)) {
						name = new StringTemplate(obj.getString(key), openTagString, closeTagString);
						
					/** Load-only attributes. **/
					} else if(key.equalsIgnoreCase(LOAD)) {
						url = new StringTemplate(obj.getString(key), openTagString, closeTagString);
					} else if(key.equalsIgnoreCase(METHOD)) {
						method = obj.getString(key);
					} else if(key.equalsIgnoreCase(POSTS)) {
						if(obj.isJsonObject(key)) {
							posts.merge(deserializeHashtableTemplate(obj.getJsonObject(key), openTagString, closeTagString));
						} else if(obj.isString(key)) {
							postData = new StringTemplate(obj.getString(key), openTagString, closeTagString);
						} else {
							return DeserializerResult.failure(StringUtils.quote(key) +
									" must be a String with post data or an object with name-value-pairs.");				
						}
					} else if(key.equalsIgnoreCase(COOKIES)) {
						cookies.merge(deserializeHashtableTemplate(obj.getJsonObject(key), openTagString, closeTagString));
					} else if(key.equalsIgnoreCase(HEADERS)) {
						headers.merge(deserializeHashtableTemplate(obj.getJsonObject(key), openTagString, closeTagString));
						
					/** Pattern attributes. **/
					} else if(key.equalsIgnoreCase(FIND)) {
						pattern = new StringTemplate(obj.getString(key), openTagString, closeTagString);
					} else if(key.equalsIgnoreCase(IS_CASE_INSENSITIVE)) {
						isCaseInsensitive = Boolean.valueOf(obj.getBoolean(key));
					} else if(key.equalsIgnoreCase(DOES_DOT_MATCH_ALL)) {
						doesDotMatchNewline = Boolean.valueOf(obj.getBoolean(key));
					} else if(key.equalsIgnoreCase(IS_MULTILINE)) {
						isMultiline = Boolean.valueOf(obj.getBoolean(key));
						
						
					/** Find-only attributes. **/
					} else if(key.equalsIgnoreCase(REPLACE)) {
						replace = new StringTemplate(obj.getString(key), openTagString, closeTagString);
					} else if(key.equalsIgnoreCase(MIN_MATCH)) {
						min = Integer.valueOf(obj.getInt(key));
					} else if(key.equalsIgnoreCase(MAX_MATCH)) {
						max = Integer.valueOf(obj.getInt(key));
					} else if(key.equalsIgnoreCase(MATCH)) {
						match = Integer.valueOf(obj.getInt(key));
						
					/** OK for all. **/
					} else if(key.equalsIgnoreCase(DESCRIPTION)) {
						
					} else {
						return DeserializerResult.failure(StringUtils.quote(key) + " is not a valid key.");
					}
				}
			}
			
			Find find = null;
			Load load = null;
			if(url != null && pattern != null) {
				// Can't define two actions.
				return DeserializerResult.failure("Cannot define both " + FIND + " and " + LOAD);
			} else if(url != null) {
				// We have a Load
				load = new Load(browser, encoder, url);
				
				if(method != null) {
					load.setMethod(method);
				}
				if(postData != null) {
					load.setPostData(postData);
				} else {
					load.addPosts(posts);
				}
				load.addCookies(cookies);
				load.addHeaders(headers);
			} else if(pattern != null) {
				// We have a Find
				find = new Find(compiler, pattern);
				
				if(replace != null) {
					find.setReplacement(replace);
				}
				if(match != null) {
					if(min != null || max != null) {
						return DeserializerResult.failure("Cannot define " + MIN_MATCH + " or " + MAX_MATCH + 
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
						return DeserializerResult.failure("Range " + StringUtils.quote(min) + " to " +
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
			
			DeserializedInstruction instruction;
			if(find != null) {
				instruction = new DeserializedInstruction(name, find);
			} else if(load != null) {
				instruction = new DeserializedInstruction(name, load);
				
			} else {
				return DeserializerResult.failure("Must define " + FIND + " or " + LOAD);
			}
			
			for(int i = 0 ; i < children.size(); i ++ ) {
				instruction.addChild((Instruction) children.elementAt(i));
			}
			result = DeserializerResult.success(instruction);
		}
		return result;
	}
	
	/**
	 * Deserialize a {@link HashtableTemplate} from a {@link JsonObject} hash.
	 * @param jsonObject Input {@link JsonObject} hash.
	 * @param database
	 * @param openTagString
	 * @param closeTagString
	 * @return A {@link HashtableTemplate}.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws TemplateCompilationException If a {@link StringTemplate} could not be compiled.
	 */
	private HashtableTemplate deserializeHashtableTemplate(JsonObject jsonObject, String openTagString, String closeTagString)
				throws JsonException, TemplateCompilationException {
		HashtableTemplate result = new HashtableTemplate();
		JsonIterator iter = jsonObject.keys();
		while(iter.hasNext()) {
			String key = (String) iter.next();
			String value = jsonObject.getString(key);
			result.put(new StringTemplate(key, openTagString, closeTagString),
					new StringTemplate(value, openTagString, closeTagString));
		}
		return result;
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
	//public static final String SAVE = "save";
	
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
	 * @param database
	 */
	public JSONDeserializer(JsonParser parser, RegexpCompiler compiler,
			HttpBrowser browser, Encoder encoder, UriResolver uriResolver, URILoader uriLoader) {
		this.compiler = compiler;
		this.parser = parser;
		this.browser = browser;
		this.encoder = encoder;
		this.uriResolver = uriResolver;
		this.uriLoader = uriLoader;
	}
	
	public DeserializerResult deserialize(String serializedString, StringMap input, String uri) 
			throws InterruptedException {
		try {
			return deserialize(serializedString, input, uri,
					StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		} catch(JsonException e) {
			return DeserializerResult.failure(e.getMessage());
		} catch (MalformedUriException e) {
			return DeserializerResult.failure(e.getMessage());
		} catch(IOException e) {
			return DeserializerResult.failure(e.getMessage());
		} catch(TemplateCompilationException e) {
			return DeserializerResult.failure(e.getMessage());
		} catch (RemoteToLocalSchemeResolutionException e) {
			return DeserializerResult.failure(e.getMessage());
		}
	}

	public void register(Logger logger) {
		browser.register(logger);
	}
}
