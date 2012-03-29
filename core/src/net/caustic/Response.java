package net.caustic;

import java.util.Hashtable;

import net.caustic.http.Cookies;
import net.caustic.json.JSONValue;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

public abstract class Response {
	
	public static final int DONE_LOAD = 1;
	public static final int DONE_FIND = 2;
	public static final int WAIT = 3;
	public static final int REFERENCE = 4;
	public static final int MISSING_TAGS = 5;
	public static final int FAILED = 6;
	
	private static final String DONE_LOAD_STRING = "loaded";
	private static final String DONE_FIND_STRING = "found";
	private static final String WAIT_STRING = "wait";
	private static final String REFERENCE_STRING = "reference";
	private static final String MISSING_TAGS_STRING = "missing";
	private static final String FAILED_STRING = "failed";
	
	static JSONArray responseAryToJSON(Response[] responses) throws JSONException {
		JSONArray ary = new JSONArray();
		for(int i = 0 ; i < responses.length ; i ++) {
			ary.put(responses[i].toJSON());
		}
		return ary;
	}

	private static String statusToString(int status) {
		switch(status) {
		case DONE_LOAD:
			return DONE_LOAD_STRING;
		case DONE_FIND:
			return DONE_FIND_STRING;
		case WAIT:
			return WAIT_STRING;
		case REFERENCE:
			return REFERENCE_STRING;
		case MISSING_TAGS:
			return MISSING_TAGS_STRING;
		case FAILED:
			return FAILED_STRING;
		default:
			throw new IllegalArgumentException("Status number " + status + " is invalid.");
		}
	}
	
	private static final String ID = "id";
	private static final String URI = "uri";
	private static final String INSTRUCTION = "instruction";
	private static final String STATUS = "status";

	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	
	private final String id;
	private final String uri;
	private final JSONValue instructionJSON;

	public final String serialize() {
		try {
			return toJSON().toString();
		} catch(JSONException e) {
			throw new RuntimeException("Internal JSON construction exception", e);
		}
	}
		
	/**
	 * 
	 * @return The status of this {@link Response}.
	 * @see #DONE_LOAD
	 * @see #DONE_FIND
	 * @see #REFERENCE
	 * @see #WAIT
	 * @see #MISSING_TAGS
	 * @see #FAILED
	 */
	public abstract int getStatus();
	
	JSONObject toJSON() throws JSONException {
		return new JSONObject()
			.put(ID, id)
			.put(URI, uri)
			.put(INSTRUCTION, instructionJSON.value)
			.put(STATUS, statusToString(getStatus()));
	}

	private Response(String id, String uri, JSONValue instructionJSON) {
		this.id = id;
		this.uri = uri; // URI used to resolve children.
		this.instructionJSON = instructionJSON;
	}
	
	public String getId() { return id; }
	public String getUri() { return uri; }
	public JSONValue getInstructionJSON() { return instructionJSON; }
	
	/*
	public static Response deserialize(String serializedResponse) throws JSONException {
		JSONObject obj = new JSONObject(serializedResponse);
		String[] children = aryFromJSONArray(obj.optJSONArray(CHILDREN));
		//String[] cookies = aryFromJSONArray(obj.optJSONArray(COOKIES));
		final Cookies cookies;
		if(obj.optString(COOKIES) != null) {
			cookies = HashtableCookies.deserialize(obj.optString(COOKIES));
		} else {
			cookies = new HashtableCookies();
		}
		String[] values = aryFromJSONArray(obj.optJSONArray(VALUES));
		String[] missingTags = aryFromJSONArray(obj.optJSONArray(MISSING_KEY));
		return new Response(obj.getString(ID), obj.optString(URI), obj.optString(DESCRIPTION),
				children, obj.optString(CONTENT), cookies, obj.optString(NAME), values,
				missingTags, obj.optString(FAILED_KEY));
	}
	*/
	
	private abstract static class Ready extends Response {

		private static final String CHILDREN = "children";
		
		private final String name;
		private final Hashtable children;
		private final String description;
		
		Ready(String id, String uri, JSONValue instructionJSON, String name, String description, Hashtable children) {
			super(id, uri, instructionJSON);
			this.name = name;
			this.description = description == null ? "" : description;
			this.children = children;
		}

		public final String getName() {
			return name;
		}
		
		/**
		 * 
		 * @return A {@link Hashtable} mapping input values to an array of the children responses
		 * they spawned.  If they spawned no children, the array is empty.
		 */
		public final Hashtable getChildren() {
			return children;
		}

		/**
		 * 
		 * @return The {@link String} description, if one was available.  Returns
		 * an empty {@link String} if none was available.
		 */
		public final String getDescription() {
			return description;
		}

		JSONObject toJSON() throws JSONException {
			return super.toJSON()
					.put(CHILDREN, Instruction.childrenResponsesToJSON(children))
					.put(DESCRIPTION, description)
					.put(NAME, name);
		}
		
	}
	
	public final static class DoneFind extends Ready {

		public final int getStatus() {
			return DONE_FIND;
		}
		
		DoneFind(String id, String uri, JSONValue instructionJSON, String name,
				String description, Hashtable children) {
			super(id, uri, instructionJSON, name, description, children);
		}
	}
	
	public final static class DoneLoad extends Ready {

		private static final String COOKIES = "cookies";
		private final Cookies cookies;

		DoneLoad(String id, String uri, JSONValue instructionJSON,
				String name, String description, Hashtable children, Cookies cookies) {
			super(id, uri, instructionJSON, name,  description, children);
			this.cookies = cookies;
		}

		public Cookies getCookies() {
			return cookies;
		}
		
		public int getStatus() {
			return DONE_LOAD;
		}
		
		JSONObject toJSON() throws JSONException {
			return super.toJSON()
					.put(COOKIES, cookies.toJSON());
		}
	}
	
	public final static class Wait extends Response {
		private final String name;
		private final String description;
		
		Wait(String id, String uri, JSONValue instructionJSON, String name, String description) {
			super(id, uri, instructionJSON);
			this.name = name;
			this.description = description;
		}

		public int getStatus() { return WAIT; }
		
		public String getName() { return name; }
		public String getDescription() { return description; }
		
		JSONObject toJSON() throws JSONException {
			return super.toJSON().put(NAME, name)
								.put(DESCRIPTION, description);
		}
		
	}
	
	public final static class Reference extends Response {
		private static final String REFERENCED = "referenced";
		private final Response[] referenced;
		
		Reference(String id, String uri, JSONValue instructionJSON, Response[] referenced) {
			super(id, uri, instructionJSON);
			this.referenced = referenced;
		}
		
		public int getStatus() { return REFERENCE; }
		
		public Response[] getReferenced() {
			return referenced;
		}
		
		JSONObject toJSON() throws JSONException {
			JSONArray ary = new JSONArray();
			for(int i = 0 ; i < referenced.length ; i ++) {
				ary.put(referenced[i].toJSON());
			}
			return super.toJSON().put(REFERENCED, ary);
		}
		
	}
	
	public final static class MissingTags extends Response {
		private static final String MISSING_KEY = "missing";
		private final String[] missingTags;
		
		public int getStatus() { return MISSING_TAGS; }
		
		public String[] getMissingTags() {
			return missingTags;
		}
		
		MissingTags(String id, String uri, JSONValue instructionJSON, String[] missingTags) {
			super(id, uri, instructionJSON);
			this.missingTags = missingTags;
		}
		
		JSONObject toJSON() throws JSONException {
			JSONArray ary = new JSONArray();
			for(int i = 0 ; i < missingTags.length ; i ++) {
				ary.put(missingTags[i]);
			}
			return super.toJSON().put(MISSING_KEY, ary);
		}
	}
	
	public static class Failed extends Response {
		private static final String FAILED_KEY = "failed";
		private final String failedBecause;
		public int getStatus() { return FAILED; }
		
		public String getReason() {
			return failedBecause;
		}
		
		Failed(String id, String uri, JSONValue instructionJSON, String failedBecause) {
			super(id, uri, instructionJSON);
			this.failedBecause = failedBecause;
		}

		JSONObject toJSON() throws JSONException {
			return super.toJSON().put(FAILED_KEY, failedBecause);
		}
	}

	public final static class BadJSON extends Failed {
		BadJSON(String id, String uri, JSONValue instructionJSON, JSONException e) {
			super(id, uri, instructionJSON, e.getMessage());
		}
	}
	/*
	private static String[] aryFromJSONArray(JSONArray jsonArray) throws JSONException {
		String[] ary = new String[jsonArray.length()];
		for(int i = 0 ; i < ary.length ; i ++) {
			ary[i] = jsonArray.getString(i);
		}
		return ary;
	}*/
}
