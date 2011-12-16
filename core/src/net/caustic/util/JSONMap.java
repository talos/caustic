package net.caustic.util;

import org.json.me.JSONObject;

public class JSONMap implements StringMap {
	private final JSONObject obj;
	
	public JSONMap(JSONObject obj) {
		this.obj = obj;
	}
	
	public String get(String key) {
		if(obj != null) {
			return obj.optString(key);
		} else {
			return null;
		}
	}

}
