package net.caustic.util;

import java.util.Enumeration;
import java.util.Hashtable;

public class HashtableUtils {

	/**
	 * Turn a {@link Hashtable} mapping {@link String}s to {@link String}s into a form-encoded
	 * {@link String}.  Assumes that names and values are <strong>already</strong> encoded.
	 * @param hashtable A {@link String} to {@link String} {@link Hashtable}.
	 * @return A {@link HashtableDatabase}.
	 */
	public static String toFormEncoded(Hashtable hashtable) {

		StringBuffer buf = new StringBuffer();
		Enumeration keys = hashtable.keys();
		try {
			while(keys.hasMoreElements()) {
				String name = (String) keys.nextElement();
				String value = (String) hashtable.get(name);
				//result += encoder.encode(name) + '=' + encoder.encode(value) + '&';
				buf.append(name).append('=').append(value).append('&');
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Hashtable must have only String keys and values to be form encoded.");
		}
		return buf.substring(0, buf.length() -1); // trim trailing ampersand
	}
	
	/**
	 * Turn an array of {@link Hashtable}s into a single {@link Hashtable}.  Keys from
	 * later elements of <code>hashtables</code> will overwrite keys from earlier elements:
	 * <p>
	 * <code>
	 * {"foo"   : "bar",<br>
	 *  "roses" : "red"}<br>
	 *  +<br>
	 * {"foo"     : "bazzz",<br>
	 *  "violets" : "blue" }</code>
	 * <p>turns into</p>
	 * <code>
	 * {"foo"     : "bazzz",<br>
	 *  "roses"   : "red",<br>
	 *  "violets" : "blue"}
	 * <p>
	 * @param hashtables An array of {@link Hashtable}s.
	 * @return A single {@link Hashtable}.
	 */
	public static Hashtable combine(Hashtable[] hashtables) {
		Hashtable result = new Hashtable();
		for(int i = 0 ; i < hashtables.length ; i ++) {
			Hashtable hashtable = hashtables[i];
			Enumeration e = hashtable.keys();
			while(e.hasMoreElements()) {
				Object key = e.nextElement();
				Object value = hashtable.get(key);
				result.put(key, value);
			}
		}
		return result;
	}
	
	/**
	 * An empty {@link Hashtable} on demand.  Throws {@link UnsupportedOperationException}
	 * on attempted modification.
	 */
	public final static Hashtable EMPTY = new Hashtable() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1296778405412009351L;

		public Object put(Object key, Object value) {
			throw new UnsupportedOperationException();
		}
	};
	
}
