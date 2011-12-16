package net.caustic;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.caustic.util.JSONMap;
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
	public final String[] cookies;
	public final boolean force;
	
	private Request(String id, String instruction, String uri, String input, StringMap tags,
			String[] cookies, boolean force) {
		this.id = id;
		this.uri = uri;
		this.instruction = instruction;
		this.input = input;
		this.tags = tags;
		this.cookies = cookies;
		this.force = force;
	}
	
	public static Request fromJSON(String json) throws JSONException {
		final JSONObject obj = new JSONObject(json);
		
		final String id = obj.getString(ID);
		
		final String uri = obj.has(URI) ? obj.getString(URI) : StringUtils.USER_DIR;
		
		final String instruction = obj.getString(INSTRUCTION);
		
		final String input = obj.optString(INPUT);
		
		final StringMap tags = new JSONMap(obj.optJSONObject(TAGS));
		
		final String[] cookies;
		JSONArray cookiesAry = obj.optJSONArray(COOKIES);
		if(cookiesAry != null) {
			cookies = new String[cookiesAry.length()];
			for(int i = 0 ; i < cookies.length ; i ++) {
				cookies[i] = cookiesAry.getString(i);
			}
		} else {
			cookies = new String[0];
		}
		
		boolean force = obj.optBoolean(FORCE);
		return new Request(id, instruction, uri, input, tags, cookies, force);
	}
}
