package net.microscraper.util;

import java.io.UnsupportedEncodingException;


/**
 * Interface for {@link String} decoding.
 * @author talos
 * @see #decode(String)
 *
 */
public interface Decoder {
	
	/**
	 * Decode a {@link String}.
	 * @param stringToDecode The {@link String} to decode.
	 * @param encoding The {@link String} encoding to use.
	 * @return A {@link String} which is <code>stringToDecode</code>
	 * decoded according to <code>encoding</code>.
	 */
	public String decode(String stringToDecode, String encoding) throws UnsupportedEncodingException;
}
