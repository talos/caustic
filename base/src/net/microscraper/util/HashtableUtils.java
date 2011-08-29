package net.microscraper.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

public class HashtableUtils {

	/**
	 * Turn a {@link Hashtable} mapping {@link String}s to {@link String}s into a form-encoded
	 * {@link String}.
	 * @param encoder The {@link Encoder} to use for encoding.
	 * @param hashtable A {@link String} to {@link String} {@link Hashtable}.
	 * @return A {@link HashtableDatabase}.
	 * @throws IOException If values could not be persisted to <code>database</code>.
	 */
	public static String toFormEncoded(Encoder encoder, Hashtable hashtable)
			throws IOException {

		String result = "";
		Enumeration keys = hashtable.keys();
		try {
			while(keys.hasMoreElements()) {
				String name = (String) keys.nextElement();
				String value = (String) hashtable.get(name);
				result += encoder.encode(name) + '=' + encoder.encode(value) + '&';
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Hashtable must have only String keys and values to be form encoded.");
		}
		return result.substring(0, result.length() -1); // trim trailing ampersand
		
	}
	
	/**
	 * Turn a form-encoded {@link String} into {@link Hashtable}.
	 * @param decoder The {@link Decoder} to use for decoding.
	 * @param formEncodedData A {@link String} of form-encoded data to convert.  It must be 
	 * correctly formatted.
	 * @return A {@link Hashtable}.
	 * @throws IOException If values could not be persisted to <code>database</code>.
	 */
	public static Hashtable fromFormEncoded(Decoder decoder, String formEncodedData)
			throws UnsupportedEncodingException, IOException {
		String[] splitByAmpersands = StringUtils.split(formEncodedData, "&");
		Hashtable result = new Hashtable();
		for(int i = 0 ; i < splitByAmpersands.length; i++) {
			String[] pair = StringUtils.split(splitByAmpersands[i], "=");
			if(pair.length == 2) {
				result.put(decoder.decode(pair[0]),
						decoder.decode(pair[1]));
			} else {
				throw new IllegalArgumentException(
						StringUtils.quote(splitByAmpersands[i]) + " is not a valid name-value pair.");
			}
		}
		return result;
	}
	
	/**
	 * Turn an array of {@link Hashtable}s into a single {@link Hashtable}.  Keys from
	 * earlier elements of <code>hashtables</code> will overwrite keys from later elements.
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
	
}
