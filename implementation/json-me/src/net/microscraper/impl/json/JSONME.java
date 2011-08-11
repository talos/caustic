package net.microscraper.impl.json;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import net.microscraper.Utils;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceIterator;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.json.JSONInterfaceStringer;
import net.microscraper.interfaces.json.JSONLocation;
import net.microscraper.interfaces.json.JSONLocationException;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;
import org.json.me.JSONWriter;
import org.json.me.StringWriter;

public class JSONME implements JSONInterface {
	private final FileLoader fileLoader;
	private final Browser browser;
	
	public JSONME(FileLoader fileLoader, Browser browser) {
		this.fileLoader = fileLoader;
		this.browser = browser;
	}
	
	public JSONInterfaceStringer getStringer() throws JSONInterfaceException {
		return new JSONMEStringer();
	}
	
	public JSONInterfaceObject load(JSONLocation jsonLocation)
			throws JSONInterfaceException, IOException, JSONLocationException {
		try {
			return new JSONMEObject(jsonLocation, new JSONObject(loadRawJSONString(jsonLocation)));
		} catch(JSONException e) {
			throw new JSONInterfaceException(e);
		}
	}
	
	public JSONInterfaceObject parse(JSONLocation dummyLocation, String jsonString)
			throws JSONInterfaceException, JSONLocationException, IOException {
		try {
			return new JSONMEObject(dummyLocation, new JSONObject(jsonString));
		} catch(JSONException e) {
			throw new JSONInterfaceException(e);
		}
	}
	
	private String loadRawJSONString(JSONLocation jsonLocation)  throws IOException {
		try {
			if(jsonLocation.isFile()) {
				return fileLoader.load(jsonLocation.getSchemeSpecificPart());
			} else if(jsonLocation.isHttp()) {
				return browser.get(false, jsonLocation.toString(), null, null, null);
			} else {
				throw new IOException("JSON can only be loaded from local filesystem or HTTP.");
			}
		} catch(BrowserException e) {
			throw new IOException(e);
		}
	}
	
	private JSONObject loadJSONObject(JSONLocation jsonLocation, String jsonString) throws IOException, JSONInterfaceException {
		//String jsonString = loadRawJSONString(jsonLocation);
		try {
			JSONObject obj = (JSONObject) new JSONTokener(jsonString).nextValue();
			String[] jsonPath = jsonLocation.explodeJSONPath();
			if(jsonPath.length > 0) {
				Vector paths = new Vector();
				Utils.arrayIntoVector(jsonPath, paths);
				obj = (JSONObject) new JSONTokener(followPathWithin(obj, paths)).nextValue();
			}
			
			return obj;
		} catch(JSONException e) {
			throw new JSONInterfaceException(e);
		}
	}
	
	/**
	 * 
	 * @param obj
	 * @param remainingPaths
	 * @return
	 */
	private String followPathWithin(JSONObject obj, Vector remainingPaths) throws NullPointerException {
		if(remainingPaths.size() == 0)
			return obj.toString();
		
		String nextKey = (String) remainingPaths.remove(0);
		JSONObject possibleObject = obj.optJSONObject(nextKey);
		if(possibleObject != null) {
			return followPathWithin(possibleObject, remainingPaths);
		}
		JSONArray possibleArray = obj.optJSONArray(nextKey);
		if(possibleArray != null) {
			return followPathWithin(possibleArray, remainingPaths);
		}
		String possibleString = obj.optString(nextKey);
		System.out.println("Next key: " + nextKey);
		if(possibleString.equals("")) {
			throw new NullPointerException("Could not follow path to JSON at " + nextKey);
		} else {
			return possibleString;
		}
	}
	
	/**
	 * 
	 * @param array
	 * @param remainingPaths
	 * @return
	 */
	private String followPathWithin(JSONArray array, Vector remainingPaths) throws ArrayIndexOutOfBoundsException {
		if(remainingPaths.size() == 0)
			return array.toString();
		
		int nextIndex = Integer.parseInt((String) remainingPaths.remove(0));
		JSONObject possibleObject = array.optJSONObject(nextIndex);
		if(possibleObject != null) {
			return followPathWithin(possibleObject, remainingPaths);
		}
		JSONArray possibleArray = array.optJSONArray(nextIndex);
		if(possibleArray != null) {
			return followPathWithin(possibleArray, remainingPaths);
		}
		String possibleString = array.optString(nextIndex);
		if(possibleString.equals("")) {
			throw new ArrayIndexOutOfBoundsException("Could not follow path to JSON at " + Integer.toString(nextIndex));
		} else {
			return possibleString;
		}
	}
	
	private class JSONMEObject implements JSONInterfaceObject {
		private final JSONObject object;
		//private final JSONInterfaceObject[] extensions;
		
		private final JSONLocation location;
		
		// ensures references are always followed.
		public JSONMEObject(JSONLocation initialLocation, JSONObject object)
				throws JSONLocationException, JSONInterfaceException,
				JSONException, IOException {
			JSONLocation location = initialLocation;
			
			while(object.has(REFERENCE_KEY)) {
				location = location.resolve(object.getString(REFERENCE_KEY));
				object = loadJSONObject(location, object.toString());
			}
			
			if(object.has(EXTENDS)) {
				if(object.optJSONObject(EXTENDS) != null) {
					merge(object, new JSONMEObject(location, object.getJSONObject(EXTENDS)));
				} else if(object.optJSONArray(EXTENDS) != null) {
					JSONArray extensions = object.getJSONArray(EXTENDS);
					for(int i = 0 ; i < extensions.length() ; i ++) {
						merge(object, new JSONMEObject(location, extensions.getJSONObject(i)));
					}
				} 
			}
			
			this.object = object;
			this.location = location;
		}
		
		private void merge(JSONObject objToBeMerged, JSONMEObject objToMerge) throws JSONException {
			Enumeration enum = objToMerge.object.keys();
			while(enum.hasMoreElements()) {
				String key = (String) enum.nextElement();
				Object value = objToMerge.object.get(key);
				
				objToBeMerged.put(key, value);
			}
		}
		
		public JSONInterfaceArray getJSONArray(String name)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(this.location.resolveFragment(name),
						object.getJSONArray(name));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			} catch(JSONLocationException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public JSONInterfaceObject getJSONObject(String name)
				throws JSONInterfaceException {
			try {
				return new JSONMEObject(
						this.location.resolveFragment(name),
						object.getJSONObject(name));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			} catch(JSONLocationException e) {
				throw new JSONInterfaceException(e);
			} catch(IOException e) {
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

		public boolean isJSONArray(String key) throws JSONInterfaceException {
			if(object.optJSONArray(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONInterfaceException(new NullPointerException(key));
				}
			}
		}

		public boolean isJSONObject(String key) throws JSONInterfaceException {
			if(object.optJSONObject(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONInterfaceException(new NullPointerException(key));
				}
			}
		}
		
		public boolean isString(String key) throws JSONInterfaceException {
			if(object.optString(key) != null) {
				return true;
			} else {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONInterfaceException(new NullPointerException(key));
				}
			}
		}

		public boolean isInt(String key) throws JSONInterfaceException {
			try {
				object.getInt(key);
				return true;
			} catch(JSONException e) {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONInterfaceException(e);
				}
			}
		}

		public boolean isBoolean(String key) throws JSONInterfaceException {
			try {
				object.getBoolean(key);
				return true;
			} catch(JSONException e) {
				if(object.has(key)) {
					return false;
				} else {
					throw new JSONInterfaceException(e);
				}
			}
		}
		
		public String toString() {
			try {
				return object.toString(2);
			} catch(JSONException e) {
				return object.toString();
			}
		}
	}
	
	private final class EnumerationIterator implements JSONInterfaceIterator {
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
	
	private class JSONMEArray implements JSONInterfaceArray {
		private final JSONArray array;
		private final JSONLocation location;
		public JSONMEArray(JSONLocation location, JSONArray ary) {
			array = ary;
			this.location = location;
		}
		
		public JSONInterfaceArray getJSONArray(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEArray(location.resolveFragment(index), array.getJSONArray(index));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			} catch(JSONLocationException e) {
				throw new JSONInterfaceException(e);
			}
		}

		public JSONInterfaceObject getJSONObject(int index)
				throws JSONInterfaceException {
			try {
				return new JSONMEObject(location.resolveFragment(index), array.getJSONObject(index));
			} catch(JSONException e) {
				throw new JSONInterfaceException(e);
			} catch(JSONLocationException e) {
				throw new JSONInterfaceException(e);
			} catch(IOException e) {
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

		public JSONLocation getLocation() {
			return this.location;
		}

		public boolean isJSONArray(int index) throws JSONInterfaceException {
			if(array.optJSONArray(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONInterfaceException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isJSONObject(int index) throws JSONInterfaceException {
			if(array.optJSONObject(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONInterfaceException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isString(int index) throws JSONInterfaceException {
			if(array.optString(index) != null) {
				return true;
			} else {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONInterfaceException(new ArrayIndexOutOfBoundsException(index));
				}
			}
		}

		public boolean isInt(int index) throws JSONInterfaceException {
			try {
				array.getInt(index);
				return true;
			} catch(JSONException e) {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONInterfaceException(e);
				}
			}

		}

		public boolean isBoolean(int index) throws JSONInterfaceException {
			try {
				array.getBoolean(index);
				return true;
			} catch(JSONException e) {
				if(array.length() > index) {
					return false;
				} else {
					throw new JSONInterfaceException(e);
				}
			}
		}
		
	}
	
	private static class JSONMEStringer implements JSONInterfaceStringer {
		//private final JSONStringer stringer = new JSONStringer();
		private final StringWriter sWriter = new StringWriter();
		private final JSONWriter writer = new JSONWriter(sWriter);
		/*public JSONMEStringer() {
			
		}*/
		public JSONInterfaceStringer array() throws JSONInterfaceException {
			try {
				writer.array();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceStringer endArray() throws JSONInterfaceException {
			try {
				writer.endArray();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceStringer endObject() throws JSONInterfaceException {
			try {
				writer.endObject();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceStringer key(String s) throws JSONInterfaceException {
			try {
				writer.key(s);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceStringer object() throws JSONInterfaceException {
			try {
				writer.object();
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceStringer value(boolean b) throws JSONInterfaceException {
			try {
				writer.value(b);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceStringer value(double d) throws JSONInterfaceException {
			try {
				writer.value(d);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceStringer value(long l) throws JSONInterfaceException {
			try {
				writer.value(l);
			} catch (JSONException e) {
				throw new JSONInterfaceException(e);
			}
			return this;
		}

		public JSONInterfaceStringer value(String s) throws JSONInterfaceException {
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
