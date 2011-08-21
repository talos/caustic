package net.microscraper.json;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.microscraper.json.JsonParser;
import net.microscraper.json.JsonArray;
import net.microscraper.json.JsonException;
import net.microscraper.json.JsonIterator;
import net.microscraper.json.JsonObject;
import net.microscraper.uri.MalformedUriException;
import net.microscraper.uri.Uri;

public class JsonMEParser implements JsonParser {
	
	/**
	 * How much indentation to use for {@link JSONObject#toString(int)}
	 * and {@link JSONObject#toString(int)}.
	 */
	private static final int INDENT_FACTOR = 2;
	
	/**
	 * The default {@link Uri} to use when resolving references from
	 * a {@link String} of parsed JSON.
	 */
	private final Uri defaultUri;
	
	private class JSONMEObject implements JsonObject {
		private final JSONObject object;
		
		private final Uri uri;
		
		public JSONMEObject(Uri uri, Hashtable hash) throws JsonException {
			this.object = new JSONObject(hash);
			this.uri = uri;
		}
		
		/**
		 * Instantiate {@link JSONMEObject}, following references.
		 * @param initialURI
		 * @param object
		 * @throws MalformedUriException
		 * @throws JsonException
		 * @throws JSONException
		 * @throws IOException
		 */
		public JSONMEObject(Uri initialURI, JSONObject object)
				throws MalformedUriException, JsonException,
				JSONException, IOException {
			Uri uri = initialURI;
			
			while(object.has(REFERENCE_KEY)) {
				uri = uri.resolve(object.getString(REFERENCE_KEY));
				//object = loadJSONObject(location, object.toString());
				try {
					object = new JSONObject(uri.load());
				} catch(InterruptedException e) {
					throw new IOException(e);
				}
			}
			
			if(object.has(EXTENDS)) {
				if(object.optJSONObject(EXTENDS) != null) {
					merge(object, new JSONMEObject(uri, object.getJSONObject(EXTENDS)));
				} else if(object.optJSONArray(EXTENDS) != null) {
					JSONArray extensions = object.getJSONArray(EXTENDS);
					for(int i = 0 ; i < extensions.length() ; i ++) {
						merge(object, new JSONMEObject(uri, extensions.getJSONObject(i)));
					}
				} 
			}
			
			this.object = object;
			this.uri = uri;
			//this.location = location;
		}
		
		private void merge(JSONObject objToBeMerged, JSONMEObject objToMerge) throws org.json.me.JSONException {
			Enumeration enum = objToMerge.object.keys();
			while(enum.hasMoreElements()) {
				String key = (String) enum.nextElement();
				Object value = objToMerge.object.get(key);
				
				objToBeMerged.put(key, value);
			}
		}
		
		public JsonArray getJsonArray(String name)
				throws JsonException, IOException, MalformedUriException {
			try {
				return new JSONMEArray(uri, object.getJSONArray(name));
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}

		public JsonObject getJsonObject(String name)
				throws JsonException, IOException, MalformedUriException {
			try {
				return new JSONMEObject(uri, object.getJSONObject(name));
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

		public JsonIterator keys() {
			return new EnumerationIterator(object.keys());
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
	
	private final class EnumerationIterator implements JsonIterator {
		private final Enumeration enumeration;
		public EnumerationIterator(Enumeration e) {
			enumeration = e;
		}
		
		public boolean hasNext() {
			return enumeration.hasMoreElements();
		}

		public String next() {
			return (String) enumeration.nextElement();
		}
	}
	
	private class JSONMEArray implements JsonArray {
		private final JSONArray array;
		private final Uri uri;
		//public JSONMEArray(JSONLocation location, JSONArray ary) {
		public JSONMEArray(Uri uri, JSONArray array) {
			this.array = array;
			this.uri = uri;
		}
		
		public JsonArray getJsonArray(int index)
				throws JsonException {
			try {
				return new JSONMEArray(uri, array.getJSONArray(index));
			} catch(org.json.me.JSONException e) {
				throw new JsonException(e);
			}
		}
		
		public JsonObject getJsonObject(int index)
				throws JsonException, MalformedUriException, IOException {
			try {
				return new JSONMEObject(uri, array.getJSONObject(index));
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

	/**
	 * Initialize a {@link JsonMEParser}.
	 * @param defaultUri The {@link Uri} to use when following references from 
	 * {@link #parse}.
	 */
	public JsonMEParser(Uri defaultUri) {
		this.defaultUri = defaultUri;
	}
	
	public JsonObject generate(Hashtable map)
			throws JsonException {
		return new JSONMEObject(defaultUri, map);
	}
	
	public JsonObject load(Uri uri)
			throws JsonException, IOException, MalformedUriException {
		try {
			return new JSONMEObject(uri, new JSONObject(uri.load()));
		} catch(org.json.me.JSONException e) {
			throw new JsonException(e);
		} catch(InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	public JsonObject parse(String jsonString)
			throws JsonException, IOException, MalformedUriException {
		try {
			return new JSONMEObject(defaultUri, new JSONObject(jsonString));
		} catch(org.json.me.JSONException e) {
			throw new JsonException(e);
		}
	}
}
