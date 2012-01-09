package net.caustic;

import java.util.Hashtable;

import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.caustic.http.Cookies;
import net.caustic.http.HashtableCookies;
import net.caustic.util.HashtableStringMap;
import net.caustic.util.StringMap;
import net.caustic.util.StringUtils;

public class Request {
	public static final String ID = "id";
	public static final String INSTRUCTION = "instruction";
	public static final String URI = "uri";
	public static final String INPUT = "input";
	public static final String TAGS = "tags";
	public static final String COOKIES = "cookies";
	public static final String FORCE = "force";
	
	public final String id;
	public final String instruction;
	public final String uri;
	public final String input;
	public final StringMap tags;
	public final Cookies cookies;
	public final boolean force;
	
	public Request(String id, String instruction, String uri, String input, StringMap tags,
			Cookies optCookies, boolean force) {
		this.id = id;
		this.uri = uri;
		this.instruction = instruction;
		this.input = input;
		this.tags = tags;
		this.cookies = optCookies;
		this.force = force;
	}
	/*
	public String serialize() throws JSONException {
		JSONObject serialized = new JSONObject();
		serialized.put(ID, id);
		serialized.put(URI, uri);
		serialized.put(INSTRUCTION, instruction);
		serialized.putOpt(key, value)
		serialized.putOpt(INPUT, input);
	}
	*/
	public static Request fromJSON(String json) throws JSONException {
		final JSONObject obj = new JSONObject(json);
		final String id = obj.getString(ID);
		final String uri = obj.has(URI) ? obj.getString(URI) : StringUtils.USER_DIR;
		final String instruction = obj.getString(INSTRUCTION);
		final String input = obj.optString(INPUT);
		final StringMap tags = HashtableStringMap.fromJSON(obj.optJSONObject(TAGS));
		
		final Cookies cookies;
		if(obj.has(COOKIES)) {
			cookies = HashtableCookies.deserialize(obj.getString(COOKIES));
		} else {
			cookies = new HashtableCookies();
		}
		
		boolean force = obj.optBoolean(FORCE);
		return new Request(id, instruction, uri, input, tags, cookies, force);
	}
	
	public String toString() {
		Hashtable map = new Hashtable();
		map.put(ID, id);
		map.put(INSTRUCTION, instruction);
		map.put(URI, uri);
		map.put(FORCE, Boolean.valueOf(force));
		map.put(TAGS, tags.toString());
		if(input != null) {
			map.put(INPUT, input.toString());
		}
		if(cookies != null) {
			map.put(COOKIES, cookies.toString());
		}
		return new JSONObject(map).toString();
	}
}
