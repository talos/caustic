package net.caustic.util;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.me.JSONException;
import org.json.me.JSONObject;

public abstract class HashtableStringMap implements StringMap {
	
	public static StringMap fromJSON(JSONObject obj) throws JSONException {
		final Hashtable table = new Hashtable();
		Enumeration keys = obj.keys();
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			table.put(key, obj.getString(key));
		}
		return new RootStringMap(table);
	}
	
	private static class RootStringMap extends HashtableStringMap {
		private Hashtable table;
		private RootStringMap(Hashtable table) {
			this.table = table;
		}
		public String get(String key) {
			return (String) table.get(key);
		}
	}
	
	/**
	 * This implementation avoids the cost of copying hashtables at the expense of traversing to root
	 * through every lookup.  Chances are that the nesting won't be too deep in the course of regular
	 * operations, but the root hashtable could be very wide -- thus this makes more sense than copying
	 * the root many times.
	 * @author talos
	 *
	 */
	private static class DerivativeStringMap extends HashtableStringMap {
		private final String key;
		private final String value;
		private final StringMap parent;
		private DerivativeStringMap(String key, String value, StringMap parent) {
			this.parent = parent;
			this.key = key;
			this.value = value;
		}
		public String get(String key) {
			return this.key.equals(key) ? value : parent.get(key);
		}
	}

	public StringMap extend(String key, String value) {
		return new DerivativeStringMap(key, value, this);
	}
}
