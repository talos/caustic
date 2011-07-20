package net.microscraper.impl.json;

import java.io.IOException;

import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceIterator;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONInterfaceStringer;
import net.microscraper.interfaces.json.JSONLocation;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import org.json.me.JSONWriter;
import org.json.me.StringWriter;

public class JSONME implements JSONInterface {
	private final FileLoader uriLoader;
	public JSONME(FileLoader uriLoader) {
		this.uriLoader = uriLoader;
	}
	
	public JSONInterfaceStringer getStringer() throws JSONInterfaceException {
		return new JSONMEStringer();
	}
	
	public JSONInterfaceObject loadJSONObject(String jsonLocation)
			throws JSONInterfaceException, IOException {
		try {
			JSONObject jsonObject = load(jsonLocation);
			return new JSONMEObject(jsonLocation, jsonObject);
		} catch(JSONException e) {
			throw new JSONInterfaceException(e);
		}
	}
	
	private JSONObject load(JSONLocation jsonLocation) throws IOException, JSONException {
		String jsonString = uriLoader.load(jsonLocation);
		return (JSONObject) new JSONTokener(jsonString).nextValue();
	}
	
	private class JSONMEObject implements JSONInterfaceObject {
		private final JSONObject object;
		private final JSONLocation location;
		
		// ensures references are always followed.
		public JSONMEObject(String initialLocation, JSONObject initialJSONObject) throws JSONException, IOException {
			JSONObject jsonObject = initialJSONObject;
			String location = initialLocation;
			while(jsonObject.has(REFERENCE_KEY)) {
				location = location.resolve(jsonObject.getString(REFERENCE_KEY));
				jsonObject = load(location);
			}
			this.object = jsonObject;
			if(location.getFragment() == null) {
				location = location.resolve("#");
			}
			this.location = location;
		}
		public JSONInterfaceArray getJSONArray(String name)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(this.location.resolveJSONFragment(name),
						object.getJSONArray(name));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public JSONInterfaceObject getJSONObject(String name)
				throws JSONInterfaceException, IOException {
			try {
				return new JSONMEObject(
						this.location.resolveJSONFragment(name),
						object.getJSONObject(name));
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

		public JSONLocation getLocation() {
			return this.location;
		}
	}
	
	private class JSONMEArray implements JSONInterfaceArray {
		private final JSONArray array;
		private final String location;
		public JSONMEArray(String location, JSONArray ary) {
			array = ary;
			this.location = location;
		}
		
		public JSONInterfaceArray getJSONArray(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(location.resolveJSONFragment(index), array.getJSONArray(index));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public JSONInterfaceObject getJSONObject(int index)
				throws JSONInterfaceException, IOException {
			try {
				return new JSONMEObject(location.resolveJSONFragment(index), array.getJSONObject(index));
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

		public String getLocation() {
			return this.location;
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
}
