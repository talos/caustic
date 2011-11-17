package net.caustic.util;

import java.io.UnsupportedEncodingException;

/**
 * Interface for {@link String} decoding, in which the encoding is set at
 * instantiation.  Thus {@link UnsupportedEncodingException} is not possible
 * at the time of decoding.
 * @author talos
 * @see #decode(String)
 *
 */
public interface Decoder {
	
	public static final String UTF_8 = "UTF-8";

	/**
	 * Decode a {@link String}.
	 * @param stringToDecode The {@link String} to decode.
	 * @return A {@link String} which is <code>stringToDecode</code>
	 * decoded according to <code>encoding</code>.
	 */
	public String decode(String stringToDecode);
}
