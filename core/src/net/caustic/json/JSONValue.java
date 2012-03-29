package net.caustic.json;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * A wrapper around org.json.me to encapsulate valid JSON values, in particular
 * Strings, arrays, and objects.
 * @author talos
 *
 */
public final class JSONValue {
	
	/**
	 * Whether the wrapped {@link #value} is a string.
	 */
	public final boolean isString;
	
	/**
	 * Whether the wrapped {@link #value} is an array.
	 */
	public final boolean isArray;
	
	/**
	 * Whether the wrapped {@link #value} is an object.
	 */
	public final boolean isObject;
	
	/**
	 * The wrapped value, either a {@link JSONArray}, {@link JSONObject},
	 * or {@link String}.  You should not modify it.
	 */
	public final Object value;
	
	private JSONValue(JSONArray ary, JSONObject obj, String str) {
		isArray = ary == null ? false : true;
		isObject = obj == null ? false : true;
		isString = str == null ? false : true;
		if(isArray) {
			value = ary;
		} else if(isObject) {
			value = obj;
		} else if(isString) {
			value = str;
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Returns the string representation of {@link #value}.
	 * 
	 * If {@link #value} is a {@link String}, then it will be wrapped in quotations
	 * and have quotations escaped.
	 */
	public String toString() {
		if(!isString) {
			return value.toString();
		} else {
			// use the JSON library's Array object to do our quotation lifting.
			JSONArray ary = new JSONArray().put(value);
			String strWithBrackets = ary.toString();
			return strWithBrackets.substring(1, strWithBrackets.length() - 1);
		}
	}
	
	/**
	 * Deserialize a string that may contain an array, object or string in JSON.
	 * @param str The {@link String} to deserialize.
	 * @param wrap <code>true</code> to wrap the value inside a string if it is not an object or array,
	 * <code>false</code> to look for an already wrapped string and otherwise throw an exception.
	 * @return {@link JSONValue} The wrapped value.
	 * @throws JSONException If this is not JSON containing a valid array, object, or wrapped string.
	 */
	public static JSONValue deserialize(String str, boolean wrap) throws JSONException {
		final String trimmed = str.trim();
		final JSONValue result;
		switch(trimmed.charAt(0)) {
		case '[':
			result = new JSONValue(new JSONArray(trimmed), null, null);
			break;
		case '{':
			result = new JSONValue(null, new JSONObject(trimmed), null);
			break;
		case '"':
			if(wrap) {
				result = JSONValue.wrapString(str);
			} else if(trimmed.charAt(trimmed.length() - 1) == '"') {
				result = new JSONValue(null, null, trimmed.substring(1, trimmed.length() -1));				
			} else {
				throw new JSONException("'" + str + "' has unterminated quotation.");
			}
			break;
		default:
			if(wrap) {
				result = JSONValue.wrapString(str);
			} else {
				throw new JSONException("'" + str + "' does not contain a JSON array or object.");
			}
		}
		return result;
	}

	/**
	 * Create a JSONValue that is really just a wrapper of an existing string.  Its {@link #value}
	 * will be the <code>str</code>, but its string representation will be quoted.
	 * @param str
	 * @return {@link JSONValue}
	 */
	public static JSONValue wrapString(String str) {
		return new JSONValue(null, null, str);
	}
}
