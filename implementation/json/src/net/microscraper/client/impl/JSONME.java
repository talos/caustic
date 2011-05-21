package net.microscraper.client.impl;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceIterator;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceStringer;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import org.json.me.JSONWriter;
import org.json.me.StringWriter;

public class JSONME implements JSONInterface {
	public JSONInterface.JSONInterfaceTokener getTokener(String jsonString) throws JSONInterfaceException {
		try {
			return new JSONMETokener(jsonString);
		} catch(Exception e) {
			throw new JSONInterfaceException("URL for scraper is not in JSON format.");
		}
	}
	
	private static class JSONMETokener implements JSONInterface.JSONInterfaceTokener {
		private final JSONTokener tokener;
		public JSONMETokener(String JSONString) {
			tokener = new JSONTokener(JSONString);
		}
		public JSONInterfaceObject nextValue() throws JSONInterfaceException {
			try {
				return new JSONMEObject((JSONObject) tokener.nextValue());
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			} catch (ClassCastException e) {
				throw new JSONInterfaceException("Cannot read this JSON.");
			}
		}
	}
	
	private static class JSONMEObject implements JSONInterfaceObject {
		private final JSONObject object;
		
		public JSONMEObject(JSONObject obj) {
			object = obj;
		}
		
		public JSONInterfaceArray getJSONArray(String name)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(object.getJSONArray(name));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public JSONInterfaceObject getJSONObject(String name)
				throws JSONInterfaceException {
			try {
				return new JSONMEObject(object.getJSONObject(name));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public java.lang.Object get(String name) throws JSONInterfaceException {
			try {
				return object.get(name);
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}
		
		public String getString(String name) throws JSONInterfaceException {
			try {
				return object.getString(name);
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public int getInt(String name) throws JSONInterfaceException {
			try {
				return object.getInt(name);
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public boolean getBoolean(String name) throws JSONInterfaceException {
			try {
				return object.getBoolean(name);
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}
		
		public boolean has(String name) {
			return object.has(name);
		}

		public boolean isNull(String name) {
			return object.isNull(name);
		}

		public JSONInterfaceIterator keys() {
			return new EnumerationIterator(object.keys());
		}
		
		public int length() {
			return object.length();
		}
		
	}
	
	private static class JSONMEArray implements JSONInterfaceArray {
		private final JSONArray array;
		public JSONMEArray(JSONArray ary) {
			array = ary;
		}
		
		public JSONInterfaceArray getJSONArray(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(array.getJSONArray(index));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public JSONInterfaceObject getJSONObject(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEObject(array.getJSONObject(index));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}


		public Object get(int index) throws JSONInterfaceException {
			try {
				return array.get(index);
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public String getString(int index) throws JSONInterfaceException {
			try {
				return array.getString(index);
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public int getInt(int index) throws JSONInterfaceException {
			try {
				return array.getInt(index);
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}
		
		public boolean getBoolean(int index) throws JSONInterfaceException {
			try {
				return array.getBoolean(index);
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public int length() {
			return array.length();
		}

		public String[] toArray() throws JSONInterfaceException {
			try {
				String[] stringArray = new String[array.length()];
				for(int i = 0; i < array.length(); i++) {
					stringArray[i] = array.getString(i);
				}
				return stringArray;
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}
		
	}
	
	private static class JSONMEStringer implements JSONInterfaceStringer {
		//private final JSONStringer stringer = new JSONStringer();
		private final StringWriter sWriter = new StringWriter();
		private final JSONWriter writer = new JSONWriter(sWriter);
		/*public JSONMEStringer() {
			
		}*/
		public JSONInterfaceWriter array() throws JSONInterfaceException {
			try {
				writer.array();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceWriter endArray() throws JSONInterfaceException {
			try {
				writer.endArray();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceWriter endObject() throws JSONInterfaceException {
			try {
				writer.endObject();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceWriter key(String s) throws JSONInterfaceException {
			try {
				writer.key(s);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceWriter object() throws JSONInterfaceException {
			try {
				writer.object();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceWriter value(boolean b) throws JSONInterfaceException {
			try {
				writer.value(b);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceWriter value(double d) throws JSONInterfaceException {
			try {
				writer.value(d);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceWriter value(long l) throws JSONInterfaceException {
			try {
				writer.value(l);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceWriter value(String s) throws JSONInterfaceException {
			try {
				writer.value(s);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}
		public String toString() {
			return sWriter.toString();
		}
	}
	
	public JSONInterfaceStringer getStringer() throws JSONInterfaceException {
		return new JSONMEStringer();
	}
}
