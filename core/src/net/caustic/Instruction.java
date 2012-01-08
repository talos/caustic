package net.caustic;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.caustic.http.Cookies;
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
	private final String instruction;
	private final String[] children;
	
	public Instruction(String instruction, String description, String uri, String[] children) {
		this.description = description;
		this.uri = uri;
		this.instruction = instruction;
		this.children = children;
	}
	
	String getDescription() {
		return description;
	}
	String getUri() {
		return uri;
	}
	String getInstruction() {
		return instruction;
	}
	/**
	 * 
	 * @param scraper
	 * @param id
	 * @param name The {@link String} name of the Instruction whose children are being run.
	 * @param inputs An array of {@link String}s to use when running children.  Provides keys for them.
	 * @param tags The {@link StringMap} that was just used to execute the Instruction.
	 * @param cookies
	 * @param childForce Whether to force-load children.  Should always be <code>false</code>.
	 * @return A {@link Hashtable} of children arrays keyed by the input that spawned them.
	 * The array is of 0-length if there were no children.  Repeat inputs don't launch children.
	 * @throws InterruptedException
	 */
	Hashtable runChildren(Scraper scraper, String id, String name, String[] inputs,
			StringMap tags, Cookies cookies, boolean childForce) throws InterruptedException {
		final Hashtable result = new Hashtable();

		for(int i = 0 ; i < inputs.length ; i++) {
			String input = inputs[i];
			if(!result.containsKey(input)) { // don't run children from repetitive key
				Response[] responses = new Response[children.length];
				for(int j = 0; j < children.length ; j ++) {
					String childInstruction = children[j];
					responses[j] = scraper.scrape(
							new Request(id, childInstruction, uri, input,
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