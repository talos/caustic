package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.caustic.http.Cookies;
import net.caustic.json.JSONValue;
import net.caustic.util.StringMap;

/**
 * An {@link Instruction} that has yet to be deserialized.  This allows for lazy evaluation
 * of children, in particular where a child cannot be deserialized until a variable is available
 * in the database.
 * @author talos
 *
 */
abstract class Instruction {

	/**
	 * Key for {@link Instruction#children} when deserializing from JSON.
	 */
	public static final String THEN = "then";
	
	/**
	 * Key for an object that will extend the current object.
	 */
	public static final String EXTENDS = "extends";
	
	/**
	 * Key for metadata.
	 */
	public static final String DESCRIPTION = "description";
	
	private final String description;
	private final String uri;
	private final JSONValue instructionJSON;
	private final JSONValue[] children;
	
	public Instruction(JSONValue instructionJSON, String description, String uri, JSONValue[] children) {
		this.description = description;
		this.uri = uri;
		this.instructionJSON = instructionJSON;
		this.children = children;
	}
	
	String getDescription() {
		return description;
	}
	String getUri() {
		return uri;
	}
	JSONValue getInstructionJSON() {
		return instructionJSON;
	}
	/**
	 * 
	 * @param scraper
	 * @param name The {@link String} name of the Instruction whose children are being run.
	 * @param inputs An array of {@link String}s to use when running children.  Provides keys for them.
	 * @param tags The {@link StringMap} that was just used to execute the Instruction.
	 * @param cookies
	 * @param childForce Whether to force-load children.  Should always be <code>false</code>.
	 * @return A {@link Hashtable} of children arrays keyed by the input that spawned them.
	 * The array is of 0-length if there were no children.  Repeat inputs don't launch children.
	 * @throws InterruptedException
	 */
	Hashtable runChildren(Scraper scraper, String name, String[] inputs,
			StringMap tags, Cookies cookies, boolean childForce) throws InterruptedException {
		final Hashtable result = new Hashtable();
		//final boolean isBranch = inputs.length > 1 ? true : false;
		
		for(int i = 0 ; i < inputs.length ; i++) {
			String input = inputs[i];
			if(!result.containsKey(input)) { // don't run children from repetitive key
				Response[] responses = new Response[children.length];
				for(int j = 0; j < children.length ; j ++) {
					final JSONValue childInstructionJSON = children[j];
					
					responses[j] = scraper.scrape(
							new Request(UUID.randomUUID().toString(), childInstructionJSON, uri, input,
									tags.extend(name, input),
									cookies, childForce));
				}
				result.put(input, responses);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param childrenResponses
	 * @return A {@link JSONObject} of jsonified {@link Response} arrays.
	 */
	static JSONObject childrenResponsesToJSON(Hashtable childrenResponses) throws JSONException {
		Enumeration e = childrenResponses.keys();
		JSONObject result = new JSONObject();
		while(e.hasMoreElements()) {
			String key = (String) e.nextElement();
			Response[] responses = (Response[]) childrenResponses.get(key);
			result.put(key, Response.responseAryToJSON(responses));
		}
		return result;
	}
}