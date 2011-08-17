package net.microscraper.util;

import java.io.UnsupportedEncodingException;

/**
 * Interface for {@link String} encoding.
 * @author talos
 * @see #encode(String)
 *
 */
public interface Encoder {
	
	/**
	 * Encode a {@link String}.
	 * @param stringToEncode The {@link String} to encode.
	 * @param encoding The {@link String} encoding to use.
	 * @return A {@link String} which is <code>stringToEncode</code>
	 * encoded according to <code>encoding</code>.
	 */
	public String encode(String stringToEncode, String encoding) throws UnsupportedEncodingException;
}
