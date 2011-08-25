package net.microscraper.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.database.HashtableDatabase;
import net.microscraper.database.Variables;

public class HashtableUtils {

	/**
	 * Turn a form-encoded {@link String} into {@link Hashtable}.
	 * @param decoder The {@link Decoder} to use for decoding.
	 * @param formEncodedData A {@link String} of form-encoded data to convert.  It must be 
	 * correctly formatted.
	 * @param encoding The encoding to use.  <code>UTF-8</code> recommended.
	 * @return A {@link HashtableDatabase}.
	 * @throws UnsupportedEncodingException If the encoding is not supported.
	 * @throws IOException If values could not be persisted to <code>database</code>.
	 */
	public static Hashtable fromFormEncoded(Decoder decoder, String formEncodedData, String encoding)
			throws UnsupportedEncodingException, IOException {
		String[] splitByAmpersands = StringUtils.split(formEncodedData, "&");
		Hashtable result = new Hashtable();
		for(int i = 0 ; i < splitByAmpersands.length; i++) {
			String[] pair = StringUtils.split(splitByAmpersands[i], "=");
			if(pair.length == 2) {
				result.put(decoder.decode(pair[0], encoding),
						decoder.decode(pair[1], encoding));
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
