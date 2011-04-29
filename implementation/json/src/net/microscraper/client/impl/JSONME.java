package net.microscraper.client.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.client.Interfaces;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONStringer;
import org.json.me.JSONTokener;

public class JSONME implements Interfaces.JSON {

	/**
	 * Convert a hashtable to a very simple JSON string.
	 * @throws JSONInterfaceException 
	 */
	public String toJSON(Hashtable hash) throws JSONInterfaceException {
		try {
			JSONObject obj = new JSONObject();
			
			Enumeration e = hash.keys();
			while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				obj.put(key, (String) hash.get(key));
			}
			
			return obj.toString();
		} catch(JSONException e) {
			throw new JSONInterfaceException(e);
		}
	}
	
	public Interfaces.JSON.Tokener getTokener(String jsonString) throws JSONInterfaceException {
		try {
			return new JSONMETokener(jsonString);
		} catch(Exception e) {
			throw new JSONInterfaceException("URL for scraper is not in JSON format.");
		}
	}
	
	private static class JSONMETokener implements Interfaces.JSON.Tokener {
		private final JSONTokener tokener;
		public JSONMETokener(String JSONString) {
			tokener = new JSONTokener(JSONString);
		}
		public Interfaces.JSON.Object nextValue() throws JSONInterfaceException {
			try {
				return new JSONMEObject((JSONObject) tokener.nextValue());
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			} catch (ClassCastException e) {
				throw new JSONInterfaceException("Cannot read this JSON.");
			}
		}
	}
	
	private static class JSONMEObject implements Interfaces.JSON.Object {
		private final JSONObject object;
		
		public JSONMEObject(JSONObject obj) {
			object = obj;
		}
		
		public Interfaces.JSON.Array getJSONArray(String name)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(object.getJSONArray(name));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public Interfaces.JSON.Object getJSONObject(String name)
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

		public boolean has(String name) {
			return object.has(name);
		}

		public boolean isNull(String name) {
			return object.isNull(name);
		}

		public Interfaces.JSON.Iterator keys() {
			return new EnumerationIterator(object.keys());
		}
		
		public int length() {
			return object.length();
		}
		
	}
	
	private static class JSONMEArray implements Interfaces.JSON.Array {
		private final JSONArray array;
		public JSONMEArray(JSONArray ary) {
			array = ary;
		}
		
		public Interfaces.JSON.Array getJSONArray(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(array.getJSONArray(index));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public Interfaces.JSON.Object getJSONObject(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEObject(array.getJSONObject(index));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}


		public java.lang.Object get(int index) throws JSONInterfaceException {
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
	
	private static class JSONMEStringer implements Interfaces.JSON.Stringer {
		private final JSONStringer stringer = new JSONStringer();
		public Writer array() throws JSONInterfaceException {
			try {
				stringer.array();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public Writer endArray() throws JSONInterfaceException {
			try {
				stringer.endArray();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public Writer endObject() throws JSONInterfaceException {
			try {
				stringer.endObject();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public Writer key(String s) throws JSONInterfaceException {
			try {
				stringer.key(s);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public Writer object() throws JSONInterfaceException {
			try {
				stringer.object();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public Writer value(boolean b) throws JSONInterfaceException {
			try {
				stringer.value(b);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public Writer value(double d) throws JSONInterfaceException {
			try {
				stringer.value(d);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public Writer value(long l) throws JSONInterfaceException {
			try {
				stringer.value(l);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public Writer value(String s) throws JSONInterfaceException {
			try {
				stringer.value(s);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}
		public String toString() {
			return stringer.toString();
		}
	}
	
	public Stringer getStringer() throws JSONInterfaceException {
		return new JSONMEStringer();
	}

}
