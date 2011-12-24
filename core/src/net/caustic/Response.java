package net.caustic;

import net.caustic.util.StringUtils;

import org.json.me.JSONException;
import org.json.me.JSONObject;

public final class Response {

	public static final String ID = "id";
	public static final String DESCRIPTION = "description";
	public final String id;
	public final String description;
	
	public static final String CHILDREN = "children";
	public static final String URI = "uri";
	public final String[] children;
	public final String uri;
	
	public static final String NAME = "name";
	public static final String VALUES = "values";
	public final String name;
	public final String[] values;
	
	public static final String CONTENT = "content";
	public static final String COOKIES = "cookies";
	public final String content;
	public final String[] cookies;
	
	public static final String MISSING = "missing";
	public final String[] missingTags;
	
	public static final String FAILED = "failed";
	public final String failedBecause;
	
	public static final String WAIT = "wait";
	public final boolean wait;
	
	private final String serialized;
		
	private Response(String id, String uri, String description, String[] children, String content, String[] cookies,
			String name, String[] values, String[] missingTags, String failedBecause) {
		try {
			this.id = id;
			this.uri = uri;
			this.description = description;
			this.children = children;
			this.content = content;
			this.cookies = cookies;
			this.name = name;
			this.values = values;
			this.missingTags = missingTags;
			this.failedBecause = failedBecause;
			
			final JSONObject obj = new JSONObject();
			obj.put(ID, id);
			obj.put(URI, uri);
			
			if(description != null) {
				obj.put(DESCRIPTION, description);
			}
			
			if(children != null) { // success
				obj.put(CHILDREN, StringUtils.makeJSONArray(children));
				if(name != null) { // find
					obj.put(NAME, name);
					obj.put(VALUES, StringUtils.makeJSONArray(values));
				} else if(content != null || cookies != null) { // load
					obj.put(CONTENT, content);
					obj.put(COOKIES, StringUtils.makeJSONArray(cookies));
				}
				this.wait = false;
			} else if(missingTags != null) { // missing tags
				obj.put(MISSING, StringUtils.makeJSONArray(missingTags));
				this.wait = false;
			} else if(failedBecause != null) { // failed
				obj.put(FAILED, failedBecause);
				this.wait = false;
			} else { // wait
				obj.put(WAIT, true);
				this.wait = true;
			}
			this.serialized = obj.toString();
		} catch(JSONException e) {
			throw new RuntimeException("Internal JSON construction exception", e);
		}
	}
	
	public String serialize() {
		return serialized;
	}
	
	static Response DoneArray(String id, String uri, String[] children) {
		return new Response(id, uri, null, children, null, null, null, null, null, null);
	}

	static Response DoneLoad(String id, String uri, String description, String[] children, String content, String[] cookies) {
		return new Response(id, uri, description, children, content, cookies, null, null, null, null);
	}
	
	static Response DoneFind(String id, String uri, String description, String[] children, String name, String[] values) {
		return new Response(id, uri, description, children, null, null, name, values, null, null);
	}
	
	static Response Missing(String id, String uri, String description, String[] missingTags) {
		return new Response(id, uri, description, null, null, null, null, null, missingTags, null);
	}
	
	static Response Failed(String id, String uri, String description, String failedBecause) {
		return new Response(id, uri, description, null, null, null, null, null, null, failedBecause);
	}
	
	static Response Wait(String id, String uri, String description) {
		return new Response(id, uri, description, null, null, null, null, null, null, null);
	}
}
