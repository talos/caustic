package net.caustic.json;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.caustic.json.JsonArray;
import net.caustic.json.JsonException;
import net.caustic.json.JsonObject;
import net.caustic.json.JsonParser;

public class JsonMEParser implements JsonParser {
	
	/**
	 * How much indentation to use for {@link JSONObject#toString(int)}
	 * and {@link JSONObject#toString(int)}.
	 */
	private static final int INDENT_FACTOR = 1;
	
	private class JSONMEObject implements JsonObject {
		private final JSONObject object;
		
		public JSONMEObject(JSONObject obj) {
			this.object = obj;
		}
		
		public JsonArray getJsonArray(String name) throws JsonException {
			try {
				return new JSONMEArray( object.getJSONArray(name));
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}

		public JsonObject getJsonObject(String name) throws JsonException {
			try {
				return new JSONMEObject( object.getJSONObject(name));
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}
		
		public String getString(String name) throws JsonException {
			try {
				return object.getString(name);
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}

		public int getInt(String name) throws JsonException {
			try {
				return object.getInt(name);
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}

		public boolean getBoolean(String name) throws JsonException {
			try {
				return object.getBoolean(name);
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}
		
		public boolean has(String name) {
			return object.has(name);
		}

		public boolean isNull(String name) {
			return object.isNull(name);
		}

		public Enumeration keys() {
			return object.keys();
		}
		
		public int length() {
			return object.length();
		}

		public boolean isJsonArray(String key) throws JsonException {
			if(object.optJSONArray(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JsonException(new NullPointerException(key));
				}
			}
		}

		public boolean isJsonObject(String key) throws JsonException {
			if(object.optJSONObject(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JsonException(new NullPointerException(key));
				}
			}
		}
		
		public boolean isString(String key) throws JsonException {
			if(object.optString(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JsonException(new NullPointerException(key));
				}
			}
		}

		public boolean isInt(String key) throws JsonException {
			try {
				object.getInt(key);
				return true;
			} catch(org.json.me.JSONException e) {
				if(object.has(key)) {
					return false;
				} else {
					throw new JsonException(e);
				}
			}
		}

		public boolean isBoolean(String key) throws JsonException {
			try {
				object.getBoolean(key);
				return true;
			} catch(org.json.me.JSONException e) {
				if(object.has(key)) {
					return false;
				} else {
					throw new JsonException(e);
				}
			}
		}
		
		public String toString() {
			try {
				return object.toString(INDENT_FACTOR);
			} catch(org.json.me.JSONException e) {
				return object.toString();
			}
		}
	}
	
	private class JSONMEArray implements JsonArray {
		private final JSONArray array;
		
		public JSONMEArray(JSONArray array) {
			this.array = array;
		}
		
		public JsonArray getJsonArray(int index)
				throws JsonException {
			try {
				return new JSONMEArray(array.getJSONArray(index));
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}
		
		public JsonObject getJsonObject(int index) throws JsonException {
			try {
				return new JSONMEObject(array.getJSONObject(index));
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}

		public String getString(int index) throws JsonException {
			try {
				return array.getString(index);
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}

		public int getInt(int index) throws JsonException {
			try {
				return array.getInt(index);
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}
		
		public boolean getBoolean(int index) throws JsonException {
			try {
				return array.getBoolean(index);
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}

		public int length() {
			return array.length();
		}

		public String[] toArray() throws JsonException {
			try {
				String[] stringArray = new String[array.length()];
				for(int i = 0; i < array.length(); i++) {
					stringArray[i] = array.getString(i);
				}
				return stringArray;
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}
		
		public boolean isJsonArray(int index) throws JsonException {
			if(array.optJSONArray(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JsonException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isJsonObject(int index) throws JsonException {
			if(array.optJSONObject(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JsonException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isString(int index) throws JsonException {
			if(array.optString(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JsonException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isInt(int index) throws JsonException {
			try {
				array.getInt(index);
				return true;
			} catch(org.json.me.JSONException e) {
				if(array.length() > index) {
					return false;
				} else {
					throw new JsonException(e);
				}
			}

		}

		public boolean isBoolean(int index) throws JsonException {
			try {
				array.getBoolean(index);
				return true;
			} catch(org.json.me.JSONException e) {
				if(array.length() > index) {
					return false;
				} else {
					throw new JsonException(e);
				}
			}
		}

		public String toString() {
			try {
				return array.toString(INDENT_FACTOR);
			} catch(org.json.me.JSONException e) {
				return array.toString();
			}
		}
	}

	public JsonObject generate(Hashtable map) {
		return new JSONMEObject(new JSONObject(map));
	}
	
	public JsonObject newObject(String jsonString) throws JsonException {
		try {
			return new JSONMEObject(new JSONObject(jsonString));
		} catch(org.json.me.JSONException e) {
			throw new JsonException(e);
		}
	}
	
	public boolean isJsonObject(String string) {
		try {
			new JSONObject(string);
			return true;
		} catch(JSONException e) {
			return false;
		}
	}
	
	public JsonArray newArray(String jsonString) throws JsonException {
		try {
			return new JSONMEArray(new JSONArray(jsonString));
		} catch(org.json.me.JSONException e) {
			throw new JsonException(e);
		}
	}
	
	public boolean isJsonArray(String string) {
		try {
			new JSONArray(string);
			return true;
		} catch(JSONException e) {
			return false;
		}
	}
}
