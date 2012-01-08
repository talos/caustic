package net.caustic;

import net.caustic.http.Cookies;
import net.caustic.util.StringUtils;

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

	private static final String ID = "id";
	private static final String URI = "uri";
	
	public final String id;
	public final String uri;

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
			.put(URI, uri);
	}

	private Response(String id, String uri) {
		this.id = id;
		this.uri = uri; // URI used to resolve children.
	}
	
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
		private static final String NAME = "name";
		private static final String DESCRIPTION = "description";
		private static final String[] EMPTY_ARRAY = new String[] { };
		
		private final String name;
		private final String[] children;
		private final String description;
		
		public final String getName() {
			return name;
		}
		
		/**
		 * 
		 * @return An array of {@link String} children instructions that could be run
		 * with these results.
		 */
		public final String[] getChildren() {
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

		Ready(String id, String uri, String name, String description, String[] children) {
			super(id, uri);
			this.name = name;
			this.description = description == null ? "" : description;
			this.children = children == null ? EMPTY_ARRAY : children;
		}
		
		JSONObject toJSON() throws JSONException {
			return super.toJSON()
					.put(CHILDREN, StringUtils.makeJSONArray(getChildren()))
					.put(DESCRIPTION, description)
					.put(NAME, name);
		}
		
	}
	
	public final static class DoneFind extends Ready {

		private static final String VALUES = "values";
		private final String[] values;
		
		public final String[] getValues() {
			return values;
		}
		
		public final int getStatus() {
			return DONE_FIND;
		}
		
		DoneFind(String id, String uri, String name, String description, String[] children, String[] values) {
			super(id, uri, name, description, children);
			this.values = values;
		}
		
		JSONObject toJSON() throws JSONException {
			JSONObject obj = super.toJSON();
			obj.put(VALUES, StringUtils.makeJSONArray(getValues()));
			return obj;
		}
	}
	
	public final static class DoneLoad extends Ready {

		private static final String CONTENT = "content";
		private static final String COOKIES = "cookies";
		private final String content;
		private final Cookies cookies;

		public String getContent() {
			return content;
		}
		
		public Cookies getCookies() {
			return cookies;
		}
		
		public int getStatus() {
			return DONE_LOAD;
		}
		
		DoneLoad(String id, String uri, String name, String description, String[] children, String content, Cookies cookies) {
			super(id, uri, name,  description, children);
			this.content = content;
			this.cookies = cookies;
		}
		
		JSONObject toJSON() throws JSONException {
			return super.toJSON()
					.put(CONTENT, getContent())
					.put(COOKIES, cookies.toJSON());
		}
	}
	
	public final static class Wait extends Ready {
		private static final String WAIT_KEY = "wait";

		public int getStatus() { return WAIT; }
		
		Wait(String id, String uri, String name, String description, String[] children) {
			super(id, uri, name, description, children);
		}
		
		JSONObject toJSON() throws JSONException {
			return super.toJSON().put(WAIT_KEY, Boolean.TRUE);
		}
	}
	
	public final static class Reference extends Response {
		private final String[] referenced;
		
		public int getStatus() { return REFERENCE; }
		
		public String[] getReferenced() {
			return referenced;
		}
		
		Reference(String id, String uri, String[] referenced) {
			super(id, uri);
			this.referenced = referenced;
		}
	}
	
	public final static class MissingTags extends Response {
		private static final String MISSING_KEY = "missing";
		private final String[] missingTags;
		
		public int getStatus() { return MISSING_TAGS; }
		
		public String[] getMissingTags() {
			return missingTags;
		}
		
		MissingTags(String id, String uri, String[] missingTags) {
			super(id, uri);
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
	
	public final static class Failed extends Response {
		private static final String FAILED_KEY = "failed";
		private final String failedBecause;
		public int getStatus() { return FAILED; }
		
		public String getReason() {
			return failedBecause;
		}
		
		Failed(String id, String uri, String failedBecause) {
			super(id, uri);
			this.failedBecause = failedBecause;
		}

		JSONObject toJSON() throws JSONException {
			return super.toJSON().put(FAILED_KEY, failedBecause);
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
