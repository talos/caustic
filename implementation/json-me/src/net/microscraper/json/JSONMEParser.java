package net.microscraper.json;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

import net.microscraper.json.JSONParser;
import net.microscraper.json.JSONArrayInterface;
import net.microscraper.json.JSONParserException;
import net.microscraper.json.JSONIterator;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.uri.URIInterface;
import net.microscraper.uri.URIInterfaceException;

public class JSONMEParser implements JSONParser {
		
	public JSONObjectInterface load(URIInterface uri)
			throws JSONParserException, URIInterfaceException {
		try {
			return new JSONMEObject(uri, new JSONObject(uri.load()));
		} catch(org.json.me.JSONException e) {
			throw new JSONParserException(e);
		} catch(IOException e) {
			throw new JSONParserException(e);
		}
	}
	
	public JSONObjectInterface parse(URIInterface uri, String jsonString)
			throws JSONParserException, URIInterfaceException {
		try {
			return new JSONMEObject(null, new JSONObject(jsonString));
		} catch(org.json.me.JSONException e) {
			throw new JSONParserException(e);
		} catch(IOException e) {
			throw new JSONParserException(e);
		}
	}
	
	private class JSONMEObject implements JSONObjectInterface {
		private final JSONObject object;
		//private final JSONInterfaceObject[] extensions;
		
		private final URIInterface uri;
		
		public JSONMEObject(Hashtable hash) throws JSONParserException {
			this.object = new JSONObject(hash);
			this.uri = null;
		}
		
		/**
		 * Instantiate {@link JSONMEObject}, following references.
		 * @param initialURI
		 * @param object
		 * @throws URIInterfaceException
		 * @throws JSONParserException
		 * @throws JSONException
		 * @throws IOException
		 */
		public JSONMEObject(URIInterface initialURI, JSONObject object)
				throws URIInterfaceException, JSONParserException,
				JSONException, IOException {
			URIInterface uri = initialURI;
			
			while(object.has(REFERENCE_KEY)) {
				uri = uri.resolve(object.getString(REFERENCE_KEY));
				//object = loadJSONObject(location, object.toString());
				object = new JSONObject(uri.load());
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
		
		public JSONArrayInterface getJSONArray(String name)
				throws JSONParserException {
			try {
				return new JSONMEArray(uri, object.getJSONArray(name));
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}
		}

		public JSONObjectInterface getJSONObject(String name)
				throws JSONParserException {
			try {
				return new JSONMEObject(uri, object.getJSONObject(name));
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			} catch(IOException e) {
				throw new JSONParserException(e);
			} catch (URIInterfaceException e) {
				throw new JSONParserException(e);
			}
		}
		
		public String getString(String name) throws JSONParserException {
			try {
				return object.getString(name);
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}
		}

		public int getInt(String name) throws JSONParserException {
			try {
				return object.getInt(name);
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}
		}

		public boolean getBoolean(String name) throws JSONParserException {
			try {
				return object.getBoolean(name);
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}
		}
		
		public boolean has(String name) {
			return object.has(name);
		}

		public boolean isNull(String name) {
			return object.isNull(name);
		}

		public JSONIterator keys() {
			return new EnumerationIterator(object.keys());
		}
		
		public int length() {
			return object.length();
		}

		public boolean isJSONArray(String key) throws JSONParserException {
			if(object.optJSONArray(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONParserException(new NullPointerException(key));
				}
			}
		}

		public boolean isJSONObject(String key) throws JSONParserException {
			if(object.optJSONObject(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONParserException(new NullPointerException(key));
				}
			}
		}
		
		public boolean isString(String key) throws JSONParserException {
			if(object.optString(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONParserException(new NullPointerException(key));
				}
			}
		}

		public boolean isInt(String key) throws JSONParserException {
			try {
				object.getInt(key);
				return true;
			} catch(org.json.me.JSONException e) {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONParserException(e);
				}
			}
		}

		public boolean isBoolean(String key) throws JSONParserException {
			try {
				object.getBoolean(key);
				return true;
			} catch(org.json.me.JSONException e) {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONParserException(e);
				}
			}
		}
		
		public String toString() {
			try {
				return object.toString(2);
			} catch(org.json.me.JSONException e) {
				return object.toString();
			}
		}
	}
	
	private final class EnumerationIterator implements JSONIterator {
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
	
	private class JSONMEArray implements JSONArrayInterface {
		private final JSONArray array;
		private final URIInterface uri;
		//public JSONMEArray(JSONLocation location, JSONArray ary) {
		public JSONMEArray(URIInterface uri, JSONArray array) {
			this.array = array;
			this.uri = uri;
		}
		
		public JSONArrayInterface getJSONArray(int index)
				throws JSONParserException {
			try {
				//return new JSONMEArray(location.resolveFragment(index), array.getJSONArray(index));
				return new JSONMEArray(uri, array.getJSONArray(index));
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}/* catch(JSONLocationException e) {
				throw new JSONInterfaceException(e);
			}*/
		}

		public JSONObjectInterface getJSONObject(int index)
				throws JSONParserException {
			try {
				return new JSONMEObject(uri, array.getJSONObject(index));
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			} catch(URIInterfaceException e) {
				throw new JSONParserException(e);
			} catch(IOException e) {
				throw new JSONParserException(e);
			}
		}

		public String getString(int index) throws JSONParserException {
			try {
				return array.getString(index);
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}
		}

		public int getInt(int index) throws JSONParserException {
			try {
				return array.getInt(index);
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}
		}
		
		public boolean getBoolean(int index) throws JSONParserException {
			try {
				return array.getBoolean(index);
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}
		}

		public int length() {
			return array.length();
		}

		public String[] toArray() throws JSONParserException {
			try {
				String[] stringArray = new String[array.length()];
				for(int i = 0; i < array.length(); i++) {
					stringArray[i] = array.getString(i);
				}
				return stringArray;
			} catch(org.json.me.JSONException e) {
				throw new JSONParserException(e);
			}
		}
/*
		public JSONLocation getLocation() {
			return this.location;
		}
*/
		public boolean isJSONArray(int index) throws JSONParserException {
			if(array.optJSONArray(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONParserException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isJSONObject(int index) throws JSONParserException {
			if(array.optJSONObject(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONParserException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isString(int index) throws JSONParserException {
			if(array.optString(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONParserException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isInt(int index) throws JSONParserException {
			try {
				array.getInt(index);
				return true;
			} catch(org.json.me.JSONException e) {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONParserException(e);
				}
			}

		}

		public boolean isBoolean(int index) throws JSONParserException {
			try {
				array.getBoolean(index);
				return true;
			} catch(org.json.me.JSONException e) {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONParserException(e);
				}
			}
		}
		
	}

	public JSONObjectInterface generate(Hashtable map)
			throws JSONParserException {
		return new JSONMEObject(map);
	}
}
