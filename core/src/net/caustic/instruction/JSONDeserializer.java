package net.caustic.instruction;

import java.util.Vector;

import net.caustic.database.DatabaseException;
import net.caustic.database.Database;
import net.caustic.http.HttpBrowser;
import net.caustic.json.JsonArray;
import net.caustic.json.JsonException;
import net.caustic.json.JsonIterator;
import net.caustic.json.JsonObject;
import net.caustic.json.JsonParser;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.RegexpUtils;
import net.caustic.regexp.StringTemplate;
import net.caustic.scope.Scope;
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
	 * Deserialize using {@link StringTemplate#ENCODED_PATTERN} and
	 * {@link StringTemplate#UNENCODED_PATTERN} by default.
	 */
	public void deserialize(String instruction, Database db, Scope scope, String uri,
			String source) throws DatabaseException, InterruptedException {
		deserialize(instruction, db, scope, uri, source,
			StringTemplate.ENCODED_PATTERN, StringTemplate.UNENCODED_PATTERN);
	}

	private void deserialize(String instruction, Database db, Scope scope, String uri,
			String source, String encodedPatternString, String notEncodedPatternString) 
					throws DatabaseException, InterruptedException {
		//	throws JsonException,
		//	MalformedUriException, InterruptedException, RemoteToLocalSchemeResolutionException,
		//	DatabaseException, URILoaderException {
		
		// We determine the type of this deserialization from the first character.
		
		final char firstChar = instruction.charAt(0);
		
		try {
			
			switch(firstChar) {
			case '[':
				deserializeArray(instruction, db, scope, uri, source,
						encodedPatternString, notEncodedPatternString);
				break;
			case '{':
				deserializeObject(instruction, db, scope, uri, source,
						encodedPatternString, notEncodedPatternString);
				break;
			case '"':
				int len = instruction.length();
				if(instruction.charAt(len - 1) == '"') {
					// clip the quotes before sending to deserializeString
					deserializeString(instruction.substring(1, len - 1), db,
							scope, uri, source, encodedPatternString,
							notEncodedPatternString);
				} else {
					db.putFailed(scope, source, instruction, uri,
							"String is missing quote at end.");
					return;
				}
				break;
			default:
				db.putFailed(scope, source, instruction, uri,
						"Valid serialized instructions must begin with '\"', '{', or '['.");
				return;
			}
			
		} catch(JsonException e) {
			db.putFailed(scope, source, instruction, uri, e.getMessage());
			return;
		} catch (MalformedUriException e) {
			db.putFailed(scope, source, instruction, uri, e.getMessage());
			return;
		} catch(URILoaderException e) {
			db.putFailed(scope, source, instruction, uri, e.getMessage());
			return;
		} catch (RemoteToLocalSchemeResolutionException e) {
			db.putFailed(scope, source, instruction, uri, e.getMessage());
			return;
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
	private void deserializeString(String instruction, Database db, Scope scope, String uri,
			String source, String encodedPatternString, String notEncodedPatternString)  
						throws DatabaseException, RemoteToLocalSchemeResolutionException,
								MalformedUriException, URILoaderException, InterruptedException {
		StringSubstitution uriSub = compiler.newTemplate(instruction,
				encodedPatternString, notEncodedPatternString).sub(db, scope);
		if(!uriSub.isMissingTags()) {
			// perform substitutions upon the URI path itself.
			String uriPath = uriSub.getSubstituted();
			String uriToLoad = uriResolver.resolve(uri, uriPath);
			String loadedJSONString = uriLoader.load(uriToLoad);
			
			deserialize(loadedJSONString, db, scope, uriToLoad, source, 
					encodedPatternString, notEncodedPatternString);
		} else {
			db.putMissing(scope, source, instruction, uri, uriSub.getMissingTags());
			return;
		}
	}
	
	/**
	 * @param string
	 * @param db
	 * @param scope
	 * @param uri
	 * @param encodedPatternString
	 * @param notEncodedPatternString
	 * @throws JsonException
	 */
	private void deserializeArray(String instruction, Database db, Scope scope, String uri,
			String source, String encodedPatternString, String notEncodedPatternString)
					throws DatabaseException, JsonException {
		String[] instructions = parser.newArray(instruction).toArray();
		
		for(int i = 0 ; i < instructions.length ; i ++) {
			db.putInstruction(scope, source, instruction, uri);
		}
	}
	
	private void deserializeObject(String instruction, Database db, Scope scope, String uri,
			String source, String encodedPatternString, String notEncodedPatternString)
					throws JsonException, DatabaseException, URILoaderException,
					RemoteToLocalSchemeResolutionException, MalformedUriException,
					InterruptedException {
		
		JsonObject initialObj = parser.newObject(instruction);
		
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
				if(key.equalsIgnoreCase(Instruction.EXTENDS)) {
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
								db.putFailed(scope, source, instruction, uri,
										Instruction.EXTENDS + " array elements must be strings or objects.");
								return; // premature return
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
						StringTemplate extendsUriTemplate = compiler.newTemplate(obj.getString(key), encodedPatternString, notEncodedPatternString);
						StringSubstitution uriSubstitution = extendsUriTemplate.sub(db, scope);
						if(!uriSubstitution.isMissingTags()) {
							String uriPath = uriSubstitution.getSubstituted();
							String uriToLoad = uriResolver.resolve(uri, uriPath);
							String loadedJSONString = uriLoader.load(uriToLoad);
							
							jsonObjects.add(parser.newObject(loadedJSONString));
						} else {
							db.putMissing(scope, source, instruction, uri,
									uriSubstitution.getMissingTags());// can't substitute uri to load EXTENDS reference, missing-variable out.
							return;
						}
					}
				} else if(key.equalsIgnoreCase(Instruction.THEN)) {
					Vector thenStrings = new Vector();
					Vector thenObjects = new Vector(); // Strings are added to this too -- their deserialization is handled later.
					
					if(obj.isJsonObject(key)) {
						thenObjects.add(obj.getString(key));
					} else if (obj.isJsonArray(key)) {
						//thenStrings.add(obj.getString(key));
						JsonArray array = obj.getJsonArray(key);
						
						for(int j = 0 ; j < array.length() ; j ++) {
							if(array.isJsonObject(j)) {
								thenObjects.add(array.getString(j));
							} else if(array.isString(j)) {
								// have to quote these for them to deserialize properly.
								thenStrings.add(StringUtils.quote(array.getString(j)));
							} else {
								db.putFailed(scope, source, instruction, uri,
										Instruction.THEN + " array elements " +
										"must be strings or objects.");
								return;
							}
						}
					} else if(obj.isString(key)) {
						thenStrings.add(StringUtils.quote(obj.getString(key)));
					} else {
						db.putFailed(scope, source, instruction, uri,
								StringUtils.quote(key) +
								" must be a String reference to another " +
								" instruction, an object with another instruction," +
								" or an array with any number " +
								" of both.");
						return;
					}
					
					for(int j = 0 ; j < thenStrings.size(); j ++) {
						String thenString = (String) thenStrings.elementAt(j);
						
						if(thenString.equalsIgnoreCase(StringUtils.quote(Instruction.SELF))) {
							//children.add(new Instruction(jsonString, this, uri));
							// to lazy-evaluate self, pass the original uri in again.
							
							// uri would be USER_DIR in inline execution
							/*if(uri.equals(StringUtils.USER_DIR)) {
								throw new JsonException("Cannot use " + StringUtils.quote(Instruction.SELF)
										+ " in inline json instruction.");
							}*/
							// TODO have to quote these for them to deserialize properly?
							children.add(instruction);
						} else {
							// already quoted
							children.add(thenString);
						}
					}
					for(int j = 0 ; j < thenObjects.size(); j ++ ) {
						// serialized instruction will handle the object as a string
						children.add((String) thenObjects.elementAt(j));
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
						db.putFailed(scope, source, instruction, uri,
								"Post data was already defined, cannot overwrite " +
								StringUtils.quote(postData.toString()) +
								" with " + StringUtils.quote(obj.getString(key)));
						return;
					}
					if(obj.isJsonObject(key)) {
						
						posts.extend(
								deserializeHashtableTemplate(
										obj.getJsonObject(key),
										encodedPatternString,
										notEncodedPatternString),
								false); // precedence is given to the original object
													
					} else if(obj.isString(key)) {
						if(posts.size() > 0) {
							db.putFailed(scope, source, instruction, uri,
									"Post data was already defined as a hash, cannot overwrite " +
									" with string " + StringUtils.quote(obj.getString(key)));
							return;
						}
						postData = compiler.newTemplate(obj.getString(key), encodedPatternString, notEncodedPatternString);
					} else {
						db.putFailed(scope, source, instruction, uri,
								StringUtils.quote(key) +
								" must be a String with post data or an object with name-value-pairs.");
						return;
						//return DeserializerResult.failure(StringUtils.quote(key) +
						//		" must be a String with post data or an object with name-value-pairs.");				
					}
				} else if(key.equalsIgnoreCase(Load.COOKIES)) {
					cookies.extend(
							deserializeHashtableTemplate(
									obj.getJsonObject(key),
									encodedPatternString,
									notEncodedPatternString),
							false); // precedence is given to the original object
				} else if(key.equalsIgnoreCase(Load.HEADERS)) {
					headers.extend(deserializeHashtableTemplate(obj.getJsonObject(key), encodedPatternString, notEncodedPatternString), false);
					
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
					db.putFailed(scope, source, instruction, uri,
							StringUtils.quote(key) + " is not a valid key.");
					return;
				}
			}
		}
		
		final String[] childrenAry = new String[children.size()];
		children.copyInto(childrenAry);
		if(url != null && pattern != null) {
			// Can't define two actions.
			db.putFailed(scope, source, instruction, uri,
					"Cannot define both " + Find.FIND + " and " + Load.LOAD);
			return;
			//return DeserializerResult.failure("Cannot define both " + FIND + " and " + LOAD);
		} else if(url != null) {
			// We have a Load

			// prevent non-post post data.
			if((posts.size() > 0 || postData != null) && !method.equalsIgnoreCase(HttpBrowser.POST)) {
				method = HttpBrowser.POST;
			}
			if(posts.size() > 0) {
				db.putLoad(scope, source, new Load(instruction, uri, url, childrenAry, method,
						cookies, headers, posts));
			} else {
				db.putLoad(scope, source, new Load(instruction, uri, url, childrenAry, method,
					cookies, headers, postData));
			}
			return;
		} else if(pattern != null) {
			// We have a Find
			
			// if name was defined, use it; otherwise use the regex as a name
			boolean hasName = name    == null ? false : true;
			name            = hasName == true ? name : pattern;
			
			min     = match  == null ? min : match.intValue(); // if match was defined, use it.
			max     = match  == null ? max : match.intValue();
			if(RegexpUtils.isValidRange(min, max) == false) {
				db.putFailed(scope, source, instruction, uri, 
						"Range " + StringUtils.quote(min) + " to " +
						StringUtils.quote(max) + " is not valid for " + Find.FIND);
			}
			db.putFind(scope, source, new Find(instruction, uri, compiler, name, hasName, pattern,
					replace, min, max, isCaseInsensitive,
					isMultiline, doesDotMatchNewline, childrenAry));
			return;
		} else {
			db.putFailed(scope, source, instruction, uri,
					"Must define " + Find.FIND + " or " + Load.LOAD);
			
			return;
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
	private HashtableTemplate deserializeHashtableTemplate(JsonObject jsonObject, String encodedPatternString,
			String notEncodedPatternString) throws JsonException {
		HashtableTemplate result = new HashtableTemplate();
		JsonIterator iter = jsonObject.keys();
		while(iter.hasNext()) {
			String key = (String) iter.next();
			String value = jsonObject.getString(key);
			result.put(compiler.newTemplate(key, encodedPatternString, notEncodedPatternString),
					compiler.newTemplate(value, encodedPatternString, notEncodedPatternString));
		}
		return result;
	}
	
	/**
	 * 
	 * @param parser
	 * @param compiler
	 * @param browser
	 * @param uriFactory
	 * @param database
	 */
	public JSONDeserializer(JsonParser parser, RegexpCompiler compiler,
			UriResolver uriResolver, URILoader uriLoader) {
		this.compiler = compiler;
		this.parser = parser;
		this.uriResolver = uriResolver;
		this.uriLoader = uriLoader;
	}
	
}
