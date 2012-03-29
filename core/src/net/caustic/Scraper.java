package net.caustic;

import java.util.Enumeration;
import java.util.Vector;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.caustic.http.HttpBrowser;
import net.caustic.json.JSONValue;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.RegexpUtils;
import net.caustic.regexp.StringTemplate;
import net.caustic.template.HashtableTemplate;
import net.caustic.template.StringSubstitution;
import net.caustic.uri.MalformedUriException;
import net.caustic.uri.RemoteToLocalSchemeResolutionException;
import net.caustic.uri.URILoader;
import net.caustic.uri.URILoaderException;
import net.caustic.uri.UriResolver;
import net.caustic.util.StringUtils;

/**
 * An implementation of {@link Deserializer} to create {@link Instruction}s
 * from JSON {@link String}s.
 * @author realest
 *
 */
public class Scraper implements Loggable {
	
	/**
	 * The {@link UriResolver} to use when resolving URIs.
	 */
	private final UriResolver uriResolver;
	
	/**
	 * The {@link URILoader} to use when loading the contents of URIs.
	 */
	private final URILoader uriLoader;
		
	/**
	 * The {@link RegexpCompiler} to use when deserializing {@link Find}s.
	 */
	private final RegexpCompiler compiler;
	
	private final HttpBrowser browser;
	
	private final MultiLog log = new MultiLog();

	/**
	 * 
	 * @param parser
	 * @param compiler
	 * @param browser
	 * @param uriFactory
	 * @param database
	 */
	public Scraper(RegexpCompiler compiler,
			UriResolver uriResolver, URILoader uriLoader, HttpBrowser browser) {
		this.compiler = compiler;
		this.uriResolver = uriResolver;
		this.uriLoader = uriLoader;
		this.browser = browser;
	}
	
	/**
	 * Deserialize using {@link StringTemplate#ENCODED_PATTERN} and
	 * {@link StringTemplate#UNENCODED_PATTERN} by default.
	 */
	public Response scrape(Request request) throws InterruptedException {
		try {
			return scrape(request, request.instructionJSON, request.uri,
				StringTemplate.ENCODED_PATTERN, StringTemplate.UNENCODED_PATTERN);
		} catch (JSONException e) {
			return new Response.BadJSON(request.id, request.uri, request.instructionJSON, e);
		}
	}
	
	public void register(Logger logger) {
		log.register(logger);
		browser.register(logger);
	}

	private Response scrape(Request request, String rawJSON, String uri,
			String encodedPatternString, String notEncodedPatternString) 
					throws JSONException, InterruptedException {
		return scrape(request, JSONValue.deserialize(rawJSON, true), uri, encodedPatternString, notEncodedPatternString);
	}
	
	private Response scrape(Request request, JSONValue instructionJSON, String uri,
			String encodedPatternString, String notEncodedPatternString) 
					throws JSONException, InterruptedException {
		try {
			if(instructionJSON.isObject) {
				return deserializeObject(request, instructionJSON, uri, encodedPatternString, notEncodedPatternString);				
			} else if(instructionJSON.isArray) {
				return scrapeArray(request, instructionJSON, uri, encodedPatternString, notEncodedPatternString);								
			} else if(instructionJSON.isString){
				return deserializeString(request, instructionJSON, uri, encodedPatternString, notEncodedPatternString);
			} else {
				throw new IllegalArgumentException(instructionJSON.toString());
			}
		} catch (MalformedUriException e) {
			return new Response.Failed(request.id, uri, instructionJSON, e.getMessage());
		} catch(URILoaderException e) {
			return new Response.Failed(request.id, uri, instructionJSON, e.getMessage());
		} catch (RemoteToLocalSchemeResolutionException e) {
			return new Response.Failed(request.id, uri, instructionJSON, e.getMessage());
		}
	}
	
	/**
	 * Parse strings as URIs.  We also have to deal here with all invalid JSON.
	 * @param string A {@link String}, with leading and trailing quotes already removed.
	 * @param db
	 * @param scope
	 * @param uri
	 * @param encodedPatternString
	 * @param notEncodedPatternString
	 * @return
	 */
	private Response deserializeString(Request request, JSONValue instructionJSON, String uri,
			String encodedPatternString, String notEncodedPatternString)  
						throws JSONException, RemoteToLocalSchemeResolutionException,
								MalformedUriException, URILoaderException, InterruptedException {		
		StringSubstitution uriSub = compiler.newTemplate((String) instructionJSON.value,
				encodedPatternString, notEncodedPatternString).sub(request.tags);
		final Response result;
		if(!uriSub.isMissingTags()) {
			// perform substitutions upon the URI path itself.
			final String uriPath = uriSub.getSubstituted();
			final String uriToLoad = uriResolver.resolve(uri, uriPath);
			final String loadedJSONString = uriLoader.load(uriToLoad);
			
			result = scrape(request, loadedJSONString, uriToLoad, encodedPatternString, notEncodedPatternString);
		} else {
			result = new Response.MissingTags(request.id, uri, instructionJSON, uriSub.getMissingTags());
		}
		return result;
	}
	
	/**
	 * Scrape an array of instructions.  The force parameter is switched off to prevent
	 * the possible synchronous scraping of multiple Loads.
	 * @param request
	 * @param instruction
	 * @param uri
	 * @param encodedPatternString
	 * @param notEncodedPatternString
	 * @return
	 * @throws InterruptedException
	 * @throws JSONException
	 */
	private Response scrapeArray(Request request, JSONValue instructionJSON, String uri,
			String encodedPatternString, String notEncodedPatternString)
					throws InterruptedException, JSONException {
		JSONArray jsonAry = (JSONArray) instructionJSON.value;
		Response[] responses = new Response[jsonAry.length()];
		for(int i = 0 ; i < responses.length ; i ++) {
			responses[i] = scrape(
					// Request is re-generated to eliminate force.
					new Request(request.id, request.instructionJSON, request.uri,
							request.input, request.tags, request.cookies, false),
					jsonAry.getString(i),
					uri,
					encodedPatternString,
					notEncodedPatternString);
		}
		
		return new Response.Reference(request.id, uri, instructionJSON, responses);
	}
	
	private Response deserializeObject(Request request, JSONValue instructionJSON, String uri,
			String encodedPatternString, String notEncodedPatternString)
					throws JSONException, URILoaderException,
					RemoteToLocalSchemeResolutionException, MalformedUriException,
					InterruptedException {
		JSONObject initialObj = (JSONObject) instructionJSON.value;
		
		// populated for all instructions
		Vector children = new Vector(); // Vector of instructions
		
		// populated for Load action
		StringTemplate url = null;
		String method = Load.METHOD_DEFAULT;
		StringTemplate postData = null;
		HashtableTemplate posts = new HashtableTemplate();
		HashtableTemplate cookies = new HashtableTemplate();
		HashtableTemplate headers = new HashtableTemplate();
		
		// Populated for Find action
		StringTemplate pattern = null;
		StringTemplate name = null;
		boolean isCaseInsensitive = Find.IS_CASE_INSENSITIVE_DEFAULT;
		boolean isMultiline = Find.IS_MULTILINE_DEFAULT;
		boolean doesDotMatchNewline = Find.DOES_DOT_MATCH_ALL_DEFAULT;
		StringTemplate replace = Find.REPLACE_DEFAULT;
		int min = Find.MIN_MATCH_DEFAULT;
		int max = Find.MAX_MATCH_DEFAULT;
		Integer match = null;
		String description = null;
		
		// This vector expands if EXTENDS objects are specified.
		Vector jsonObjects = new Vector();
		jsonObjects.add(initialObj);
		
		for(int i = 0 ; i < jsonObjects.size() ; i ++) {
			JSONObject obj = (JSONObject) jsonObjects.get(i);
			Enumeration e = obj.keys();
			
			// want to get description if possible
			if(obj.has(Instruction.DESCRIPTION)) {
				description = obj.getString(Instruction.DESCRIPTION);
			}
			
			// Case-insensitive loop over key names.
			while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				
				/** Attributes for Instruction. **/
				if(key.equalsIgnoreCase(Instruction.EXTENDS)) {
					Vector extendsStrings = new Vector();
					Vector extendsObjects = new Vector();
					
					if(obj.optJSONObject(key) != null) {
						extendsObjects.add(obj.getJSONObject(key));
					} else if(obj.optJSONArray(key) != null) {
						JSONArray array = obj.getJSONArray(key);
						
						
						for(int j = 0 ; j < array.length(); j ++) {
							if(array.optJSONObject(j) != null) {
								extendsObjects.add(array.getJSONObject(j));
							} else if(array.optJSONArray(j) != null) {
								return new Response.Failed(request.id, uri, instructionJSON,
										Instruction.EXTENDS +
										" array elements must be strings or objects."); // premature return
							} else {
								extendsStrings.add(array.getString(j));
							}						
						}
					} else {
						extendsStrings.add(obj.getString(key));
					}
					
					for(int j = 0 ; j < extendsObjects.size() ; j ++) {
						jsonObjects.add(extendsObjects.elementAt(j));
					}
					for(int j = 0 ; j < extendsStrings.size() ; j ++) {
						//String uri = uriResolver.resolve(baseUri, (String) extendsStrings.elementAt(j));
						//jsonObjects.add(parser.parse(uriLoader.load(uri)));
						StringTemplate extendsUriTemplate = compiler.newTemplate(obj.getString(key), encodedPatternString, notEncodedPatternString);
						StringSubstitution uriSubstitution = extendsUriTemplate.sub(request.tags);
						if(!uriSubstitution.isMissingTags()) {
							String uriPath = uriSubstitution.getSubstituted();
							String uriToLoad = uriResolver.resolve(uri, uriPath);
							String loadedJSONString = uriLoader.load(uriToLoad);
							
							jsonObjects.add(new JSONObject(loadedJSONString));
						} else {
							// can't substitute uri to load EXTENDS reference, missing-variable out.
							return new Response.MissingTags(request.id, uri, instructionJSON,
									uriSubstitution.getMissingTags());
						}
					}
				} else if(key.equalsIgnoreCase(Instruction.THEN)) {
					//Vector thenStrings = new Vector();
					//Vector thenObjects = new Vector(); // Strings are added to this too -- their deserialization is handled later.
					
					JSONValue thenValue = JSONValue.deserialize(obj.getString(key), true);
					if(thenValue.isArray) {
						JSONArray array = (JSONArray) thenValue.value;
						for(int j = 0 ; j < array.length() ; j ++) {
							JSONValue thenElement = JSONValue.deserialize(array.getString(j), true);
							if(thenElement.isArray) {
								return new Response.Failed(request.id, uri, instructionJSON,
										Instruction.THEN +
										" array elements must be strings or objects, but " + 
										StringUtils.quote(array.getString(j)) + " is an array.");																
							} else {
								children.add(thenElement);
							}
						}
					} else {
						children.add(thenValue);
					}
				} else if(key.equalsIgnoreCase(Find.NAME)) {
					name = compiler.newTemplate(obj.getString(key), encodedPatternString, notEncodedPatternString);
					
				/** Load-only attributes. **/
				} else if(key.equalsIgnoreCase(Load.LOAD)) {
					url = compiler.newTemplate(obj.getString(key), encodedPatternString, notEncodedPatternString);
				} else if(key.equalsIgnoreCase(Load.METHOD)) {
					method = obj.getString(key);
				} else if(key.equalsIgnoreCase(Load.POSTS)) {
					
					// can't extend preexisting post data.
					if(postData != null) {
						return new Response.Failed(request.id, uri, instructionJSON,
								"Post data was already defined, cannot overwrite " +
								StringUtils.quote(postData.toString()) +
								" with " + StringUtils.quote(obj.getString(key)));
					}
					if(obj.optJSONObject(key) != null) {
						
						posts.extend(
								deserializeHashtableTemplate(
										obj.getJSONObject(key),
										encodedPatternString,
										notEncodedPatternString),
								false); // precedence is given to the original object
					} else if(obj.optJSONArray(key) != null) {
						return new Response.Failed(request.id, uri, instructionJSON, StringUtils.quote(key) +
								" must be a String with post data or an object with name-value-pairs.");
						
					} else {
						if(posts.size() > 0) {
							return new Response.Failed(request.id, uri, instructionJSON,
									"Post data was already defined as a hash, cannot overwrite " +
									" with string " + StringUtils.quote(obj.getString(key)));
						}
						postData = compiler.newTemplate(obj.getString(key), encodedPatternString, notEncodedPatternString);
					}
				} else if(key.equalsIgnoreCase(Load.COOKIES)) {
					cookies.extend(
							deserializeHashtableTemplate(
									obj.getJSONObject(key),
									encodedPatternString,
									notEncodedPatternString),
							false); // precedence is given to the original object
				} else if(key.equalsIgnoreCase(Load.HEADERS)) {
					headers.extend(deserializeHashtableTemplate(obj.getJSONObject(key), encodedPatternString, notEncodedPatternString), false);
					
				/** Pattern attributes. **/
				} else if(key.equalsIgnoreCase(Find.FIND)) {
					pattern = compiler.newTemplate(obj.getString(key), encodedPatternString, notEncodedPatternString);
				} else if(key.equalsIgnoreCase(Find.IS_CASE_INSENSITIVE)) {
					isCaseInsensitive = obj.getBoolean(key);
				} else if(key.equalsIgnoreCase(Find.DOES_DOT_MATCH_ALL)) {
					doesDotMatchNewline = obj.getBoolean(key);
				} else if(key.equalsIgnoreCase(Find.IS_MULTILINE)) {
					isMultiline = obj.getBoolean(key);
					
					
				/** Find-only attributes. **/
				} else if(key.equalsIgnoreCase(Find.REPLACE)) {
					replace = compiler.newTemplate(obj.getString(key), encodedPatternString, notEncodedPatternString);
				} else if(key.equalsIgnoreCase(Find.MIN_MATCH)) {
					min = obj.getInt(key);
				} else if(key.equalsIgnoreCase(Find.MAX_MATCH)) {
					max = obj.getInt(key);
				} else if(key.equalsIgnoreCase(Find.MATCH)) {
					match = Integer.valueOf(obj.getInt(key));
					
				/** OK for all. **/
				} else if(key.equalsIgnoreCase(Instruction.DESCRIPTION)) {
					
				} else {
					// break out early
					//return DeserializerResult.failure(StringUtils.quote(key) + " is not a valid key.");
					return new Response.Failed(request.id, uri, instructionJSON,
							StringUtils.quote(key) + " is not a valid key.");
				}
			}
		}
		
		final JSONValue[] childrenAry = new JSONValue[children.size()];
		children.copyInto(childrenAry);
		if(url != null && pattern != null) {
			// Can't define two actions.
			return new Response.Failed(request.id, uri, instructionJSON, 
					"Cannot define both " + Find.FIND + " and " + Load.LOAD);
			//return DeserializerResult.failure("Cannot define both " + FIND + " and " + LOAD);
		} else if(url != null) {
			// We have a Load

			// prevent non-post post data.
			if((posts.size() > 0 || postData != null) && !method.equalsIgnoreCase(HttpBrowser.POST)) {
				method = HttpBrowser.POST;
			}
			
			// use URL for name if none was supplied
			name = name == null ? url : name;
			final Load load;
			if(posts.size() > 0) {
				load = new Load(instructionJSON, description, uri, name, url, childrenAry, method,
						cookies, headers, posts);
			} else {
				load = new Load(instructionJSON, description, uri, name, url, childrenAry, method,
						cookies, headers, postData);
			}
			return load.execute(this, request.id, request.tags, request.cookies, browser, request.force);
		} else if(pattern != null) {
			// We have a Find
			
			// if name was defined, use it; otherwise use the regex as a name
			//boolean hasName = name    == null ? false : true;
			//name            = hasName == true ? name : pattern;
			name = name == null ? pattern : name;
			
			min     = match  == null ? min : match.intValue(); // if match was defined, use it.
			max     = match  == null ? max : match.intValue();
			if(RegexpUtils.isValidRange(min, max) == false) {
				return new Response.Failed(request.id, uri, instructionJSON,
						"Range " + StringUtils.quote(min) + " to " +
						StringUtils.quote(max) + " is not valid for " + Find.FIND);
			}
			
			final Find find = new Find(instructionJSON, description, uri, compiler, name, pattern,
					replace, min, max, isCaseInsensitive,
					isMultiline, doesDotMatchNewline, childrenAry);
			return find.execute(this, request.id, request.input, request.tags, request.cookies);
		} else {
			return new Response.Failed(request.id, uri, instructionJSON, "Must define " + Find.FIND + " or " + Load.LOAD);
		}
	}
	
	
	/**
	 * Deserialize a {@link HashtableTemplate} from a {@link JsonObject} hash.
	 * @param jsonObject Input {@link JsonObject} hash.
	 * @param encodedPatternString
	 * @param notEncodedPatternString
	 * @return A {@link HashtableTemplate}.
	 * @throws JsonException If there was a problem parsing the JSON.
	 * @throws TemplateCompilationException If a {@link StringTemplate} could not be compiled.
	 */
	private HashtableTemplate deserializeHashtableTemplate(JSONObject jsonObject, String encodedPatternString,
			String notEncodedPatternString) throws JSONException {
		HashtableTemplate result = new HashtableTemplate();
		Enumeration e = jsonObject.keys();
		while(e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = jsonObject.getString(key);
			result.put(compiler.newTemplate(key, encodedPatternString, notEncodedPatternString),
					compiler.newTemplate(value, encodedPatternString, notEncodedPatternString));
		}
		return result;
	}
	
}
