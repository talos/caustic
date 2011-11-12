package net.caustic.util;

import java.io.UnsupportedEncodingException;

/**
 * Interface for {@link String} encoding, where the encoding is set at instantiation.
 * This avoids the risk of {@link UnsupportedEncodingException} at the moment of encoding.
 * @author talos
 * @see #encode(String)
 *
 */
public interface Encoder {
	
	public static final String UTF_8 = "UTF-8";

	/**
	 * Encode a {@link String}.
	 * @param stringToEncode The {@link String} to encode.
	 * @return A {@link String} which is <code>stringToEncode</code>
	 * encoded according to the encoding of {@link Encoder}.
	 */
	public String encode(String stringToEncode);
}
