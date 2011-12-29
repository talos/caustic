package net.caustic;

import net.caustic.http.Cookies;
import net.caustic.util.StringUtils;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

public abstract class Response {
	
	public static final int DONE = 1;
	public static final int WAIT = 2;
	public static final int MISSING_TAGS = 3;
	public static final int FAILED = 4;

	public final String id;
	public final String uri;
	
	private final String description;
	private static final String ID = "id";
	private static final String DESCRIPTION = "description";
	private static final String URI = "uri";

	public final String serialize() {
		try {
			return toJSON().toString();
		} catch(JSONException e) {
			throw new RuntimeException("Internal JSON construction exception", e);
		}
	}

	public String getDescription() {
		return description;
	}
	
	public boolean hasDescription() {
		return description == null;
	}
	
	/**
	 * 
	 * @return The status of this {@link Response}.
	 * @see #DONE
	 * @see #WAIT
	 * @see #MISSING_TAGS
	 * @see #FAILED
	 */
	public abstract int getStatus();
	
	JSONObject toJSON() throws JSONException {
		return new JSONObject()
			.put(ID, id)
			.put(URI, uri)
			.putOpt(DESCRIPTION, description);
	}

	private Response(String id, String uri, String description) {
		this.id = id;
		this.uri = uri; // URI used to resolve children.
		this.description = description;
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
	
	public static class Done extends Response {

		private static final String CHILDREN = "children";
		private final String[] children;
		
		public int getStatus() { return DONE; }
		
		public boolean isLoad() { return false; }
		public boolean isFind() { return false; }
		
		public final String[] getChildren() {
			return children;
		}

		Done(String id, String uri, String description, String[] children) {
			super(id, uri, description);
			this.children = children;
		}
		
		JSONObject toJSON() throws JSONException {
			return super.toJSON().putOpt(CHILDREN, StringUtils.makeJSONArray(getChildren()));
		}
	}
	
	public final static class DoneFind extends Done {

		private static final String NAME = "name";
		private static final String VALUES = "values";
		private final String name;
		private final String[] values;
		
		public boolean isLoad() { return false; }
		public boolean isFind() { return true; }

		public final String getName() {
			return name;
		}
		
		public final String[] getValues() {
			return values;
		}
		
		DoneFind(String id, String uri, String description, String[] children, String name, String[] values) {
			super(id, uri, description, children);
			this.name = name;
			this.values = values;
		}
		
		JSONObject toJSON() throws JSONException {
			JSONObject obj = super.toJSON();
			obj.put(NAME, getName());
			obj.put(VALUES, StringUtils.makeJSONArray(getValues()));
			return obj;
		}
	}
	
	public final static class DoneLoad extends Done {

		private static final String CONTENT = "content";
		private static final String COOKIES = "cookies";
		private final String content;
		private final Cookies cookies;

		public boolean isLoad() { return true; }
		public boolean isFind() { return false; }
		
		public String getContent() {
			return content;
		}
		
		public Cookies getCookies() {
			return cookies;
		}
		
		DoneLoad(String id, String uri, String description, String[] children, String content, Cookies cookies) {
			super(id, uri, description, children);
			this.content = content;
			this.cookies = cookies;
		}
		
		JSONObject toJSON() throws JSONException {
			return super.toJSON()
					.put(CONTENT, getContent())
					.put(COOKIES, cookies.toJSON());
		}
	}
	
	public final static class Wait extends Response {
		private static final String WAIT_KEY = "wait";

		public int getStatus() { return WAIT; }
		
		Wait(String id, String uri, String description) {
			super(id, uri, description);
		}
		
		JSONObject toJSON() throws JSONException {
			return super.toJSON().put(WAIT_KEY, Boolean.TRUE);
		}
	}
	
	public final static class MissingTags extends Response {
		private static final String MISSING_KEY = "missing";
		private final String[] missingTags;
		
		public int getStatus() { return MISSING_TAGS; }
		
		public String[] getMissingTags() {
			return missingTags;
		}
		
		MissingTags(String id, String uri, String description, String[] missingTags) {
			super(id, uri, description);
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
		private String failedBecause;
		public int getStatus() { return FAILED; }
		
		public String getReason() {
			return failedBecause;
		}
		
		Failed(String id, String uri, String description, String failedBecause) {
			super(id, uri, description);
		}

		JSONObject toJSON() throws JSONException {
			return super.toJSON().put(FAILED_KEY, failedBecause);
		}
	}
	
	private static String[] aryFromJSONArray(JSONArray jsonArray) throws JSONException {
		String[] ary = new String[jsonArray.length()];
		for(int i = 0 ; i < ary.length ; i ++) {
			ary[i] = jsonArray.getString(i);
		}
		return ary;
	}
}
