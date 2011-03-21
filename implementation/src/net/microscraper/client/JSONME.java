package net.microscraper.client;

import net.microscraper.client.JSONInterface;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;


public class JSONME implements JSONInterface {

	@Override
	public JSONInterfaceTokener getTokener(String jsonString) {
		return new JSONMETokener(jsonString);
	}
	
	private static class JSONMETokener implements JSONInterfaceTokener {
		private final JSONTokener tokener;
		public JSONMETokener(String JSONString) {
			tokener = new JSONTokener(JSONString);
		}
		@Override
		public JSONInterfaceObject nextValue() throws JSONInterfaceException {
			try {
				return new JSONMEObject((JSONObject) tokener.nextValue());
			} catch (JSONException e) {
				throw new JSONMEException(e);
			}
		}
	}
	
	private static class JSONMEObject implements JSONInterfaceObject {
		private final JSONObject object;
		
		public JSONMEObject(JSONObject obj) {
			object = obj;
		}
		
		@Override
		public JSONInterfaceArray getJSONArray(String name)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(object.getJSONArray(name));
			} catch(JSONException e) {
				throw new JSONMEException(e);
			}
		}

		@Override
		public JSONInterfaceObject getJSONObject(String name)
				throws JSONInterfaceException {
			try {
				return new JSONMEObject(object.getJSONObject(name));
			} catch(JSONException e) {
				throw new JSONMEException(e);
			}
		}

		@Override
		public String getString(String name) throws JSONInterfaceException {
			try {
				return object.getString(name);
			} catch(JSONException e) {
				throw new JSONMEException(e);
			}
		}

		@Override
		public int getInt(String name) throws JSONInterfaceException {
			try {
				return object.getInt(name);
			} catch(JSONException e) {
				throw new JSONMEException(e);
			}
		}

		@Override
		public boolean has(String name) {
			return object.has(name);
		}

		@Override
		public boolean isNull(String name) {
			return object.isNull(name);
		}

		@Override
		public IteratorInterface keys() {
			return new EnumerationIterator(object.keys());
		}
		
		@Override
		public int length() {
			return object.length();
		}
		
	}
	
	private static class JSONMEArray implements JSONInterfaceArray {
		private final JSONArray array;
		public JSONMEArray(JSONArray ary) {
			array = ary;
		}
		
		@Override
		public JSONInterfaceArray getJSONArray(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(array.getJSONArray(index));
			} catch(JSONException e) {
				throw new JSONMEException(e);
			}
		}

		@Override
		public JSONInterfaceObject getJSONObject(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEObject(array.getJSONObject(index));
			} catch(JSONException e) {
				throw new JSONMEException(e);
			}
		}

		@Override
		public String getString(int index) throws JSONInterfaceException {
			try {
				return array.getString(index);
			} catch(JSONException e) {
				throw new JSONMEException(e);
			}
		}

		@Override
		public int length() {
			return array.length();
		}

		@Override
		public String[] toArray() throws JSONInterfaceException {
			try {
				String[] stringArray = new String[array.length()];
				for(int i = 0; i < array.length(); i++) {
					stringArray[i] = array.getString(i);
				}
				return stringArray;
			} catch(JSONException e) {
				throw new JSONMEException(e);
			}
		}
		
	}
	
	private static class JSONMEException extends JSONInterfaceException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JSONException e;
		public JSONMEException(JSONException exception) {
			e = exception;
		}
		
		public String getMessage() { return e.getMessage(); }
		public String toString() { return e.toString(); }
		public void printStackTrace() { e.printStackTrace(); }
	}
}
